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
package io.micronaut.gcp.pubsub.support;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.PublisherInterface;
import io.micronaut.context.BeanDependencyResolver;
import io.micronaut.context.annotation.Retain;
import io.micronaut.context.env.DevelopmentActive;
import io.micronaut.context.exceptions.NoSuchBeanException;
import io.micronaut.core.annotation.Internal;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.Modules;
import io.micronaut.gcp.pubsub.configuration.PubSubConfigurationProperties;
import io.micronaut.gcp.pubsub.configuration.PublisherConfigurationProperties;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.scheduling.executor.ExecutorConfiguration;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * Holds the publishers of the {@code PubSubClient} methods across a restart of the application in development mode,
 * so that the next generation publishes on the publishers, and their channels, of the previous one. It exists only in
 * development mode: outside it each publisher belongs to the client advice that created it, as before.
 *
 * <p>A publisher holds the topic it publishes to, the settings of its configuration, the channel provider, the
 * credentials provider and the executor service of the {@link DefaultPublisherFactory}, and no class of the application:
 * a message is serialized by the client advice of each generation before it reaches the publisher. As it is created,
 * this bean resolves the executor services the publishers run on, those of {@code gcp.pubsub.publishing-executor} and
 * of each {@code gcp.pubsub.publisher.*.executor}, with the channel and credentials providers, so that they are
 * retained with it. A change under {@value GoogleCloudConfiguration#PREFIX}, of the emulator, or of the executors
 * releases it, and it shuts its publishers down as it is destroyed.</p>
 *
 * <p>A publisher is held by a key of the client method and of what the publisher is built from. One that no client
 * advice took during the lifetime of a whole advice, such as that of a method removed or moved to another topic, is
 * shut down as that advice is closed.</p>
 *
 * @author graemerocher
 * @since 6.3.0
 */
@Internal
@Singleton
@DevelopmentActive
@Retain(invalidatedBy = {GoogleCloudConfiguration.PREFIX, PubSubConfigurationFactory.EMULATOR_PREFIX, ExecutorConfiguration.PREFIX})
public final class RetainedPublishers implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(RetainedPublishers.class);

    /**
     * A class of the Pub/Sub client that keeps an exception in a static field, created as the class is initialized:
     * initialized on a stack of the application, as the first publication of a generation would, its stack trace would
     * keep that generation reachable for as long as the client is loaded.
     */
    private static final String CALLBACK_EXECUTOR = "com.google.cloud.pubsub.v1.SequentialExecutorService$CallbackExecutor";

    private final Map<String, Held> publishers = new ConcurrentHashMap<>();
    private final DevelopmentThreads threads;
    private int lifetime;
    private boolean closed;

    /**
     * Resolves what the publishers are built on, so that it is retained with this bean. Nothing of it is held.
     *
     * @param dependencies Resolves the executor services the configuration names, as dependencies of this bean
     * @param configuration The Pub/Sub configuration, which names the default publishing executor
     * @param publisherConfigurations The publisher configurations, each of which names an executor
     * @param threads Creates the publishers on a thread whose stack holds no class of the application
     */
    RetainedPublishers(BeanDependencyResolver dependencies,
                       PubSubConfigurationProperties configuration,
                       List<PublisherConfigurationProperties> publisherConfigurations,
                       DevelopmentThreads threads) {
        threads.initialize(CALLBACK_EXECUTOR, Publisher.class.getClassLoader());
        this.threads = threads;
        dependencies.getBean(TransportChannelProvider.class, Qualifiers.byName(Modules.PUBSUB));
        dependencies.getBean(CredentialsProvider.class, Qualifiers.byName(Modules.PUBSUB));
        Set<String> executors = new LinkedHashSet<>();
        executors.add(configuration.getPublishingExecutor());
        for (PublisherConfigurationProperties publisherConfiguration : publisherConfigurations) {
            executors.add(publisherConfiguration.getExecutor());
        }
        for (String executor : executors) {
            if (executor == null) {
                continue;
            }
            try {
                dependencies.getBean(ExecutorService.class, Qualifiers.byName(executor));
            } catch (NoSuchBeanException e) {
                // the publisher that names it fails as it is created, as it does outside development mode
                LOG.debug("No executor service named {} for the Pub/Sub publishers", executor);
            }
        }
    }

    /**
     * The publisher held for a key, or a new one, which is then held.
     *
     * @param key The key of the client method and of what the publisher is built from
     * @param factory Creates the publisher
     * @return The publisher
     */
    public PublisherInterface publisher(String key, Supplier<? extends PublisherInterface> factory) {
        synchronized (this) {
            if (closed) {
                throw new IllegalStateException("The Pub/Sub publishers are shut down");
            }
            Held held = publishers.get(key);
            if (held == null) {
                held = new Held(threads.outsideTheApplication("publisher", factory));
                publishers.put(key, held);
                LOG.debug("Holding a new Pub/Sub publisher for {}", key);
            }
            held.lifetime = lifetime;
            return held.publisher;
        }
    }

    /**
     * A client advice is closed: the publishers no advice took during its lifetime, nor during that of this one, are
     * shut down. Those it took are kept for the next one.
     */
    public void adviceClosed() {
        List<PublisherInterface> unused = new ArrayList<>();
        synchronized (this) {
            publishers.values().removeIf(held -> {
                if (held.lifetime < lifetime) {
                    unused.add(held.publisher);
                    return true;
                }
                return false;
            });
            lifetime++;
        }
        shutdown(unused);
    }

    /**
     * @return The publishers held
     */
    Collection<PublisherInterface> publishers() {
        synchronized (this) {
            return publishers.values().stream().map(held -> held.publisher).toList();
        }
    }

    /**
     * Shuts the publishers down, as the bean is destroyed: as the context stops, unless it is retained, or as a change
     * releases it.
     */
    @PreDestroy
    @Override
    public void close() {
        List<PublisherInterface> all = new ArrayList<>();
        synchronized (this) {
            closed = true;
            publishers.values().forEach(held -> all.add(held.publisher));
            publishers.clear();
        }
        shutdown(all);
    }

    private static void shutdown(List<PublisherInterface> publishers) {
        for (PublisherInterface held : publishers) {
            if (!(held instanceof Publisher publisher)) {
                continue;
            }
            try {
                publisher.shutdown();
            } catch (RuntimeException e) {
                LOG.warn("Failed to shut down the Pub/Sub publisher of {}", publisher.getTopicNameString(), e);
            }
        }
    }

    private static final class Held {
        private final PublisherInterface publisher;
        private int lifetime;

        private Held(PublisherInterface publisher) {
            this.publisher = publisher;
        }
    }
}
