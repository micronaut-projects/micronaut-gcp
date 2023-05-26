package io.micronaut.gcp.pubsub.integration

import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.api.gax.rpc.TransportChannelProvider
import com.google.cloud.pubsub.v1.SubscriptionAdminClient
import com.google.cloud.pubsub.v1.SubscriptionAdminSettings
import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.cloud.pubsub.v1.TopicAdminSettings
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PushConfig
import com.google.pubsub.v1.TopicName
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.Modules
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import spock.lang.Specification

class IntegrationTestSpec  extends Specification {

    static CONTAINER_PORT = -1
    static CredentialsProvider CREDENTIALS_PROVIDER
    static TransportChannelProvider TRANSPORT_CHANNEL_PROVIDER
    static PubSubResourceAdmin pubSubResourceAdmin

    static GenericContainer pubSubContainer

    static {
        if (DockerClientFactory.instance().isDockerAvailable()) {
            pubSubContainer = new GenericContainer("google/cloud-sdk:292.0.0")
                    .withCommand("gcloud", "beta", "emulators", "pubsub", "start", "--project=test-project",
                            "--host-port=0.0.0.0:8085")
                    .withExposedPorts(8085)

                    .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*Server started, listening on.*"))
            pubSubContainer.start()
            CONTAINER_PORT = pubSubContainer.getMappedPort(8085)
            CREDENTIALS_PROVIDER = NoCredentialsProvider.create()
            def host = "localhost:" + IntegrationTest.CONTAINER_PORT
            ManagedChannel channel = ManagedChannelBuilder.forTarget(host).usePlaintext().build()
            TRANSPORT_CHANNEL_PROVIDER =
                    FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
            pubSubResourceAdmin = new PubSubResourceAdmin(TRANSPORT_CHANNEL_PROVIDER, CREDENTIALS_PROVIDER)
        }
    }
}

@Factory
@Requires(env = "integration")
class IntegrationTestFactory {

    @Replaces(CredentialsProvider)
    @Singleton
    @Named(Modules.PUBSUB)
    CredentialsProvider credentialsProvider() {
        return IntegrationTestSpec.CREDENTIALS_PROVIDER
    }

    @Replaces(TransportChannelProvider)
    @Singleton
    @Named(Modules.PUBSUB)
    TransportChannelProvider transportChannelProvider(CredentialsProvider credentialsProvider){
        return IntegrationTestSpec.TRANSPORT_CHANNEL_PROVIDER
    }

}

class PubSubResourceAdmin {

    private final TopicAdminClient topicAdminClient
    private final SubscriptionAdminClient subscriptionAdminClient

    PubSubResourceAdmin(TransportChannelProvider transportChannelProvider, CredentialsProvider credentialsProvider) {
        this.subscriptionAdminClient = SubscriptionAdminClient.create(SubscriptionAdminSettings.newBuilder()
               .setTransportChannelProvider(transportChannelProvider)
               .setCredentialsProvider(credentialsProvider)
               .build())
        this.topicAdminClient = TopicAdminClient.create(
                TopicAdminSettings.newBuilder()
                        .setTransportChannelProvider(transportChannelProvider)
                        .setCredentialsProvider(credentialsProvider)
                        .build())
    }

    void createTopic(TopicName topicName) {
        def topic = null
        try {
            topic = topicAdminClient.getTopic(topicName)
        } catch(Exception e){
        }
        if(topic == null){
            topicAdminClient.createTopic(topicName)
        }
    }

    void createSubscription(TopicName topicName, ProjectSubscriptionName subscriptionName) {
        def subscription = null
        try {
            subscription = subscriptionAdminClient.getSubscription(subscriptionName)
        } catch(Exception e){
        }
        if(subscription == null) {
            subscriptionAdminClient.createSubscription(subscriptionName, topicName, PushConfig.newBuilder().build(), 10)
        }
    }

}
