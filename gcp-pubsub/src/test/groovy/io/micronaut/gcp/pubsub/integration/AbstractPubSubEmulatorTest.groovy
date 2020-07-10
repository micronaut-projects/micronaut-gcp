package io.micronaut.gcp.pubsub.integration

import com.google.api.core.SettableApiFuture
import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.api.gax.rpc.TransportChannelProvider
import com.google.cloud.pubsub.v1.Publisher
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.pubsub.v1.TopicAdminSettings
import com.google.pubsub.v1.TopicName
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.micronaut.context.annotation.Replaces
import io.micronaut.gcp.pubsub.support.PublisherFactory
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import spock.lang.Specification

@MicronautTest
abstract class AbstractPubSubEmulatorTest extends Specification {
    static GenericContainer pubSubContainer = new GenericContainer("knarz/pubsub-emulator")
            .withExposedPorts(8005)

            .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*Server started, listening on.*"))

    static {
        pubSubContainer.start()
    }

    @MockBean
    @Replaces(PublisherFactory)
    PublisherFactory publisherFactory() {
        def host = "localhost:" + pubSubContainer.getMappedPort(8005)
        ManagedChannel channel = ManagedChannelBuilder.forTarget(host).usePlaintext().build()
        TransportChannelProvider channelProvider =
                FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
        CredentialsProvider credentialsProvider = NoCredentialsProvider.create()
        TopicAdminClient topicClient =
                TopicAdminClient.create(
                        TopicAdminSettings.newBuilder()
                                .setTransportChannelProvider(channelProvider)
                                .setCredentialsProvider(credentialsProvider)
                                .build())
        TopicName topicName = TopicName.of("test-project", "test-topic")
        def factory = Mock(PublisherFactory)
        def publisher =
                Publisher.newBuilder(topicName)
                        .setChannelProvider(channelProvider)
                        .setCredentialsProvider(credentialsProvider)
                        .build()
        def future = new SettableApiFuture<String>()
        factory.createPublisher(_) >> publisher
        return factory
    }



}



