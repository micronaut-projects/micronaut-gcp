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

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings;
import com.google.cloud.pubsub.v1.TopicAdminClient;
import com.google.cloud.pubsub.v1.TopicAdminSettings;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * The Pub/Sub emulator of the test suites, with admin clients that create the topics and subscriptions of a test.
 */
final class PubSubEmulator implements AutoCloseable {

    static final String PROJECT = "dev-project";

    private static final String IMAGE_NAME = "thekevjames/gcloud-pubsub-emulator:446.0.0";
    private static final int PORT = 8681;

    private final GenericContainer<?> container;
    private final ManagedChannel channel;
    private final TopicAdminClient topics;
    private final SubscriptionAdminClient subscriptions;

    PubSubEmulator() throws IOException {
        container = new GenericContainer<>(DockerImageName.parse(IMAGE_NAME))
            .withExposedPorts(PORT)
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.ofMinutes(10));
        container.start();
        channel = ManagedChannelBuilder.forTarget(host()).usePlaintext().build();
        FixedTransportChannelProvider channelProvider = FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel));
        topics = TopicAdminClient.create(TopicAdminSettings.newBuilder()
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(NoCredentialsProvider.create())
            .build());
        subscriptions = SubscriptionAdminClient.create(SubscriptionAdminSettings.newBuilder()
            .setTransportChannelProvider(channelProvider)
            .setCredentialsProvider(NoCredentialsProvider.create())
            .build());
    }

    /**
     * @return The host and port of the emulator, for {@code pubsub.emulator.host}
     */
    String host() {
        return container.getHost() + ":" + container.getMappedPort(PORT);
    }

    /**
     * Creates a topic and a subscription to it, of names of their own.
     *
     * @param prefix The prefix of the names
     * @return The names
     */
    Names create(String prefix) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Names names = new Names(prefix + "-topic-" + suffix, prefix + "-subscription-" + suffix);
        TopicName topic = TopicName.of(PROJECT, names.topic());
        topics.createTopic(topic);
        subscriptions.createSubscription(ProjectSubscriptionName.of(PROJECT, names.subscription()), topic, PushConfig.getDefaultInstance(), 10);
        return names;
    }

    @Override
    public void close() throws InterruptedException {
        topics.close();
        subscriptions.close();
        channel.shutdownNow().awaitTermination(10, TimeUnit.SECONDS);
        container.stop();
    }

    /**
     * The names of a topic and of its subscription.
     *
     * @param topic The topic
     * @param subscription The subscription
     */
    record Names(String topic, String subscription) {
    }
}
