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
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Replaces
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.Subscription
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.support.Person
import io.micronaut.test.annotation.MicronautTest
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import spock.lang.Requires
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest
@Requires({ env.get("tests.integration") == "enabled" })
@Property(name = "gcp.projectId", value = "test-project")
@Property(name = "spec.name", value = "IntegrationTest")
class IntegrationTest extends Specification{

    static CONTAINER_PORT = -1

    @Inject
    PubSubIntegrationTestClient client

    @Inject
    PubSubIntegrationTestListener listener

    void "simple publishing integration"(){
        PollingConditions conditions = new PollingConditions(timeout: 3)

        def person = new Person()
            person.name = "alf"

        when:
            client.send(person)
        then:
            conditions.eventually {
                listener.data.name == person.name
            }

    }

    static GenericContainer pubSubContainer = new GenericContainer("google/cloud-sdk:292.0.0")
            .withCommand("gcloud", "beta", "emulators", "pubsub", "start", "--project=test-project",
                    "--host-port=0.0.0.0:8085")
            .withExposedPorts(8085)
            .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*Server started, listening on.*"))

    static {
       pubSubContainer.start()
       CONTAINER_PORT = pubSubContainer.getMappedPort(8085)
    }

}

@Factory
@io.micronaut.context.annotation.Requires(property = "spec.name", value = "IntegrationTest")
class IntegrationTestFactory {

    @Replaces(CredentialsProvider)
    @Singleton
    CredentialsProvider credentialsProvider() {
        return NoCredentialsProvider.create()
    }

    @Replaces(TransportChannelProvider)
    @Singleton
    TransportChannelProvider transportChannelProvider(CredentialsProvider credentialsProvider){
        def host = "localhost:" + IntegrationTest.CONTAINER_PORT
        ManagedChannel channel = ManagedChannelBuilder.forTarget(host).usePlaintext().build()
        TransportChannelProvider channelProvider =
                FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel))
        TopicAdminClient topicClient =
                TopicAdminClient.create(
                        TopicAdminSettings.newBuilder()
                                .setTransportChannelProvider(channelProvider)
                                .setCredentialsProvider(credentialsProvider)
                                .build())
        SubscriptionAdminClient subscriptionAdminClient =
                SubscriptionAdminClient.create(SubscriptionAdminSettings.newBuilder()
                        .setTransportChannelProvider(channelProvider)
                        .setCredentialsProvider(credentialsProvider)
                        .build())

        TopicName topicName = TopicName.of("test-project", "test-topic")
        topicClient.createTopic(topicName)
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of("test-project", "test-subscription")
        subscriptionAdminClient.createSubscription(subscriptionName, topicName, PushConfig.newBuilder().build(), 10)
        return channelProvider
    }
}

@PubSubClient
@io.micronaut.context.annotation.Requires(property = "spec.name", value = "IntegrationTest")
interface PubSubIntegrationTestClient {
    @Topic("test-topic")
    String send(Person person)
}

@PubSubListener
@io.micronaut.context.annotation.Requires(property = "spec.name", value = "IntegrationTest")
class PubSubIntegrationTestListener {

    Person data

    @Subscription("test-subscription")
    void onMessage(Person person) {
        this.data = person
    }
}


