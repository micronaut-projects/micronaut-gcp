/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.gcp.pubsub.dev;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.PublisherInterface;
import io.grpc.ManagedChannel;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.reload.ClassChange;
import io.micronaut.context.reload.ClassChangeEvent;
import io.micronaut.context.reload.ReloadStrategy;
import io.micronaut.dev.tck.ReloadHarness;
import io.micronaut.dev.tck.ReloadTck;
import io.micronaut.gcp.Modules;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.TaskExecutors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs an application with a Pub/Sub listener and a Pub/Sub client through the development runtime, against the
 * Pub/Sub emulator, and restarts it. Development mode keeps the channel provider, with the channel of the emulator,
 * the credentials provider, the executor services the publishers run on and the publishers across the restart; the
 * subscribers of the stopped generation are removed as it stops, and the next generation subscribes again on the same
 * channel, so that it receives each message once and the stopped one none. A change under {@code gcp} releases what
 * was kept.
 */
class PubSubReloadTest {

    private static final String RETAINED_PUBLISHERS = "io.micronaut.gcp.pubsub.support.RetainedPublishers";

    private static final String LISTENER = """
        package example;

        import io.micronaut.gcp.pubsub.annotation.PubSubListener;
        import io.micronaut.gcp.pubsub.annotation.Subscription;

        import java.nio.charset.StandardCharsets;
        import java.util.List;
        import java.util.concurrent.CopyOnWriteArrayList;

        @PubSubListener
        public class Listener {
            private final List<String> received = new CopyOnWriteArrayList<>();

            @Subscription("%s")
            void receive(byte[] data) {
                received.add("%s " + new String(data, StandardCharsets.UTF_8));
            }

            public List<String> received() {
                return received;
            }
        }
        """;

    private static final String CLIENT = """
        package example;

        import io.micronaut.gcp.pubsub.annotation.PubSubClient;
        import io.micronaut.gcp.pubsub.annotation.Topic;

        @PubSubClient
        public interface Client {
            @Topic("%s")
            String send(byte[] data);
        }
        """;

    private static PubSubEmulator emulator;

    @TempDir
    Path project;

    @BeforeAll
    static void startEmulator() throws Exception {
        emulator = new PubSubEmulator();
    }

    @AfterAll
    static void stopEmulator() throws Exception {
        if (emulator != null) {
            emulator.close();
        }
    }

    @Test
    void aRestartKeepsTheChannelExecutorsAndPublishersAndTheNextGenerationReceivesEachMessageOnce() throws Exception {
        PubSubEmulator.Names names = emulator.create("restart");
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            properties(harness);
            harness.source("example.Listener", LISTENER.formatted(names.subscription(), "first"));
            harness.source("example.Client", CLIENT.formatted(names.topic()));
            harness.start();
            assertReloaderPresent(harness.context());

            send(harness.context(), "one");
            awaitTrue("the first generation receives", () -> received(harness.context()).contains("first one"));

            ApplicationContext first = harness.context();
            TransportChannelProvider channelProvider = first.getBean(TransportChannelProvider.class, Qualifiers.byName(Modules.PUBSUB));
            ManagedChannel channel = channel(channelProvider);
            CredentialsProvider credentialsProvider = first.getBean(CredentialsProvider.class, Qualifiers.byName(Modules.PUBSUB));
            ExecutorService scheduled = unwrap(first.getBean(ExecutorService.class, Qualifiers.byName(TaskExecutors.SCHEDULED)));
            Object retainedPublishers = first.getBean(type(first, RETAINED_PUBLISHERS));
            List<PublisherInterface> publishers = publishers(retainedPublishers);
            assertEquals(1, publishers.size());
            PublisherInterface publisher = publishers.get(0);
            List<String> firstReceived = received(first);
            first = null;

            harness.source("example.Listener", LISTENER.formatted(names.subscription(), "second"));
            long reloadStarted = System.nanoTime();
            harness.reload();
            long reloaded = System.nanoTime();
            assertEquals(2, harness.generation());
            ApplicationContext second = harness.context();
            assertReloaderPresent(second);

            // the channel provider with its channel, the credentials provider, the executor service the publisher runs
            // on and the publishers are those of the first generation
            for (Object retained : List.of(channelProvider, credentialsProvider, retainedPublishers)) {
                ReloadTck.assertRetained(harness, retained);
            }
            assertSame(channelProvider, second.getBean(TransportChannelProvider.class, Qualifiers.byName(Modules.PUBSUB)));
            assertSame(channel, channel(second.getBean(TransportChannelProvider.class, Qualifiers.byName(Modules.PUBSUB))));
            assertFalse(channel.isShutdown());
            // the adopted executor service is instrumented again, as the listeners of a retained bean run again: the
            // executor service it instruments is the retained one
            assertSame(scheduled, unwrap(second.getBean(ExecutorService.class, Qualifiers.byName(TaskExecutors.SCHEDULED))));
            assertFalse(scheduled.isShutdown());

            // the next generation publishes on the publisher of the first one, and receives each message once
            send(second, "two");
            awaitTrue("the second generation receives", () -> received(harness.context()).contains("second two"));
            long received = System.nanoTime();
            System.out.printf("Reloaded in %d ms; the second generation received its first message %d ms after it started%n",
                TimeUnit.NANOSECONDS.toMillis(reloaded - reloadStarted), TimeUnit.NANOSECONDS.toMillis(received - reloaded));
            assertEquals(List.of(publisher), publishers(retainedPublishers));
            // a message received twice would have arrived with the first copy
            Thread.sleep(1000);
            assertEquals(List.of("second two"), received(harness.context()));
            assertEquals(List.of("first one"), firstReceived, "the stopped generation received nothing more");
            second = null;
            channelProvider = null;
            credentialsProvider = null;
            scheduled = null;
            retainedPublishers = null;
            publishers = null;
            publisher = null;

            // neither the subscribers of the first generation, removed as it stopped, nor what development mode kept,
            // keep it reachable
            ReloadTck.assertRetiredGenerationsCollected(harness);
        }
    }

    @Test
    void aChangeUnderTheGcpPrefixReleasesTheChannelAndThePublishers() throws Exception {
        PubSubEmulator.Names names = emulator.create("release");
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            properties(harness);
            harness.source("example.Listener", LISTENER.formatted(names.subscription(), "first"));
            harness.source("example.Client", CLIENT.formatted(names.topic()));
            harness.start();
            send(harness.context(), "one");
            awaitTrue("the first generation receives", () -> received(harness.context()).contains("first one"));
            TransportChannelProvider[] channelProvider = {harness.context().getBean(TransportChannelProvider.class, Qualifiers.byName(Modules.PUBSUB))};
            ManagedChannel[] channel = {channel(channelProvider[0])};
            Object[] retainedPublishers = {harness.context().getBean(type(harness.context(), RETAINED_PUBLISHERS))};
            PublisherInterface[] publisher = {publishers(retainedPublishers[0]).get(0)};

            // the configuration changes, together with a class, so the application restarts
            StringBuilder changed = new StringBuilder();
            configuration().forEach((key, value) -> changed.append(key).append('=').append(value).append('\n'));
            changed.append("gcp.pubsub.keep-alive-interval-minutes=7\n");
            harness.resource("application.properties", changed.toString());
            harness.source("example.Listener", LISTENER.formatted(names.subscription(), "second"));
            harness.reload();
            assertEquals(2, harness.generation());

            ApplicationContext second = harness.context();
            assertNotSame(channelProvider[0], second.getBean(TransportChannelProvider.class, Qualifiers.byName(Modules.PUBSUB)));
            assertNotSame(retainedPublishers[0], second.getBean(type(second, RETAINED_PUBLISHERS)));
            assertTrue(channel[0].isShutdown(), "the released channel of the emulator is shut down");
            assertTrue(((Publisher) publisher[0]).awaitTermination(10, TimeUnit.SECONDS), "the released publisher is shut down");
            channelProvider[0] = null;
            channel[0] = null;
            retainedPublishers[0] = null;
            publisher[0] = null;

            send(second, "two");
            second = null;
            awaitTrue("the second generation receives on its channel", () -> received(harness.context()).equals(List.of("second two")));
            ReloadTck.assertRetiredGenerationsCollected(harness);
        }
    }

    @Test
    void aListenerChangedInPlaceSubscribesAgainOnTheSameChannel() throws Exception {
        PubSubEmulator.Names names = emulator.create("in-place");
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            properties(harness);
            harness.source("example.Listener", LISTENER.formatted(names.subscription(), "first"));
            harness.source("example.Client", CLIENT.formatted(names.topic()));
            harness.start();
            send(harness.context(), "one");
            awaitTrue("the listener receives", () -> received(harness.context()).contains("first one"));
            Object listener = listener(harness.context());
            TransportChannelProvider channelProvider = harness.context().getBean(TransportChannelProvider.class, Qualifiers.byName(Modules.PUBSUB));
            List<PublisherInterface> publishers = publishers(harness.context().getBean(type(harness.context(), RETAINED_PUBLISHERS)));

            changedInPlace(harness, "example.Listener");

            assertEquals(1, harness.generation(), "the application did not restart");
            assertNotSame(listener, listener(harness.context()), "the listener bean is recreated");
            assertSame(channelProvider, harness.context().getBean(TransportChannelProvider.class, Qualifiers.byName(Modules.PUBSUB)));
            send(harness.context(), "two");
            awaitTrue("the recreated listener receives", () -> received(harness.context()).contains("first two"));
            // a subscriber left by the previous processor would deliver it to the replaced listener, or a second time
            Thread.sleep(1000);
            assertEquals(List.of("first two"), received(harness.context()));
            assertEquals(List.of("first one"), received(listener), "the replaced listener received nothing more");
            assertEquals(publishers, publishers(harness.context().getBean(type(harness.context(), RETAINED_PUBLISHERS))),
                "a listener change leaves the publishers");
        }
    }

    private static Map<String, String> configuration() {
        return Map.of(
            "gcp.project-id", PubSubEmulator.PROJECT,
            "pubsub.emulator.host", emulator.host()
        );
    }

    private static void properties(ReloadHarness harness) {
        configuration().forEach(harness::property);
    }

    /**
     * The executor service an instrumented one runs on.
     */
    private static ExecutorService unwrap(ExecutorService executorService) throws Exception {
        for (Class<?> type = executorService.getClass(); type != null; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                if (method.getName().equals("getTarget") && method.getParameterCount() == 0) {
                    return unwrap((ExecutorService) method.invoke(executorService));
                }
            }
        }
        return executorService;
    }

    private static ManagedChannel channel(TransportChannelProvider channelProvider) throws Exception {
        return (ManagedChannel) ((GrpcTransportChannel) ((FixedTransportChannelProvider) channelProvider).getTransportChannel()).getChannel();
    }

    private static void send(ApplicationContext context, String message) throws Exception {
        Class<?> clientType = type(context, "example.Client");
        Object client = context.getBean(clientType);
        clientType.getMethod("send", byte[].class).invoke(client, (Object) message.getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("unchecked")
    private static List<PublisherInterface> publishers(Object retainedPublishers) throws Exception {
        Method publishers = retainedPublishers.getClass().getDeclaredMethod("publishers");
        publishers.setAccessible(true);
        return new ArrayList<>((Collection<PublisherInterface>) publishers.invoke(retainedPublishers));
    }

    private static void awaitTrue(String what, BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(60).toNanos();
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("Timed out waiting until " + what);
            }
            Thread.sleep(50);
        }
    }

    /**
     * Tells the running generation that a class was redefined in place, as the development runtime does after it
     * redefined the class. The context is not kept: a reference to it would keep the generation reachable.
     */
    private static void changedInPlace(ReloadHarness harness, String className) {
        ApplicationContext context = harness.context();
        context.publishEvent(new ClassChangeEvent(PubSubReloadTest.class, Set.of(), context.getClassLoader(),
            List.of(new ClassChange(className, ClassChange.Kind.MODIFIED)), ReloadStrategy.RELOAD));
    }

    private static void assertReloaderPresent(ApplicationContext context) {
        // the bean that follows the changes exists in development mode only
        String reloader = "io.micronaut.gcp.pubsub.intercept.DevelopmentPubSubReloader";
        assertTrue(context.containsBean(type(context, reloader)), reloader);
    }

    private static Object listener(ApplicationContext context) {
        return context.getBean(type(context, "example.Listener"));
    }

    private static List<String> received(ApplicationContext context) {
        return received(listener(context));
    }

    @SuppressWarnings("unchecked")
    private static List<String> received(Object bean) {
        try {
            return (List<String>) bean.getClass().getMethod("received").invoke(bean);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Cannot read what " + bean + " received", e);
        }
    }

    private static Class<?> type(ApplicationContext context, String className) {
        try {
            return Class.forName(className, true, context.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(className + " is not in the application", e);
        }
    }
}
