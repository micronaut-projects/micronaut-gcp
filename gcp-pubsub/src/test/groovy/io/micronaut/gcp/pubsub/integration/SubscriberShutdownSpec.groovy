package io.micronaut.gcp.pubsub.integration

import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.rpc.TransportChannelProvider
import com.google.cloud.pubsub.v1.AckReplyConsumer
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Subscriber
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.Subscription
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.bind.DefaultSubscriberFactory
import io.micronaut.gcp.pubsub.bind.SubscriberFactoryConfig
import io.micronaut.runtime.server.EmbeddedServer
import jakarta.annotation.PreDestroy
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.util.concurrent.PollingConditions

import java.util.concurrent.atomic.AtomicInteger

class SubscriberShutdownSpec extends IntegrationTestSpec {

    void "pending messages process to completion on shutdown by default"() {
        given:
        TopicName topicName = TopicName.of("test-project", "topic")
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of("test-project", "topic-sub")
        pubSubResourceAdmin.createTopic(topicName)
        pubSubResourceAdmin.createSubscription(topicName, subscriptionName)
        PollingConditions conditions = new PollingConditions(timeout: 10)
        EmbeddedServer subscriberServer = ApplicationContext.run(EmbeddedServer, [
                "server.name" : "ShutdownSubscriberServer",
                "gcp.projectId" : "test-project"

        ], "integration")

        def ctx = ApplicationContext.run([
                "spec.name" : "SubscriberShutdownSpec",
                "gcp.projectId" : "test-project"
        ], "integration")

        when:
        def listener = subscriberServer.getApplicationContext().getBean(MyPubSubListener.class)
        def subscriberFactory = subscriberServer.getApplicationContext().getBean(MessageTrackingSubscriberFactory)
        def publisher = ctx.getBean(PubSubShutdownClient)

        then:
        listener != null
        listener.messagesProcessed.get() == 0
        publisher != null
        subscriberServer.isRunning()
        subscriberFactory != null
        subscriberFactory.messagesReceived.get() == 0

        when:
        for(int i = 0; i<100; i++) {
            publisher.send("ping")
        }

        then:
        conditions.eventually {
            listener.messagesProcessed.intValue() > 1
        }

        when:
        subscriberServer.stop()

        then:
        conditions.eventually {
            !subscriberServer.isRunning()
        }
        subscriberFactory.messagesReceived.get() == listener.messagesProcessed.get()
    }

    void "subscribers can eagerly nack messages on shutdown when configured"() {
        given:
        TopicName topicName = TopicName.of("test-project", "topic")
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of("test-project", "topic-sub")
        pubSubResourceAdmin.createTopic(topicName)
        pubSubResourceAdmin.createSubscription(topicName, subscriptionName)
        PollingConditions conditions = new PollingConditions(timeout: 10)
        EmbeddedServer subscriberServer = ApplicationContext.run(EmbeddedServer, [
                "server.name" : "ShutdownSubscriberServer",
                "gcp.projectId" : "test-project",
                "micronaut.executors.scheduled.core-pool-size" : 5,
                "gcp.pubsub.nack-on-shutdown" : true

        ], "integration")

        def ctx = ApplicationContext.run([
                "spec.name" : "SubscriberShutdownSpec",
                "gcp.projectId" : "test-project"
        ], "integration")

        when:
        def listener = subscriberServer.getApplicationContext().getBean(MyPubSubListener.class)
        def publisher = ctx.getBean(PubSubShutdownClient);

        then:
        listener != null
        listener.messagesProcessed.get() == 0
        publisher != null
        subscriberServer.isRunning()

        when:
        for(int i = 0; i<100; i++) {
            publisher.send("ping")
        }

        then:
        conditions.eventually {
            listener.messagesProcessed.intValue() > 2
        }

        when:
        subscriberServer.stop()

        then:
        conditions.eventually {
            !subscriberServer.isRunning()
        }
        listener.messagesProcessed.get() < 100
    }
}

@PubSubClient
@Requires(property = "spec.name", value = "SubscriberShutdownSpec")
interface PubSubShutdownClient {
    @Topic(value ="topic")
    void send(String msg)
}

@Requires(property = "server.name", value = "ShutdownSubscriberServer")
@PubSubListener
class MyPubSubListener {

    private static final Logger LOG = LoggerFactory.getLogger(MyPubSubListener.class);

    AtomicInteger messagesProcessed = new AtomicInteger(0)

    @Subscription(value = "topic-sub", configuration = "test")
    public void onMessage(PubsubMessage message) {
        var messageId = message.getMessageId();
        LOG.debug("Received message with ID " + messageId + ". Invoking message processor.")
        int currentCount = messagesProcessed.get()
        try {
            Thread.sleep(1000)
            currentCount = messagesProcessed.incrementAndGet()
        } catch (InterruptedException e) {
            LOG.debug("Message processing interrupted", e)
        }
        LOG.debug("Message with ID " + messageId + " contents: " + message.getData() + ".")

        LOG.debug("Processor finished processing message with ID " + messageId + ".")
        LOG.debug("Total Messages {}", currentCount)
    }

    @PreDestroy
    void onShutdown() {
        LOG.info("PreDestroy");
    }
}

@Requires(property = "server.name", value = "ShutdownSubscriberServer")
@Primary
@Singleton
class MessageTrackingSubscriberFactory extends DefaultSubscriberFactory {

    AtomicInteger messagesReceived = new AtomicInteger(0)

    MessageTrackingSubscriberFactory(TransportChannelProvider transportChannelProvider, CredentialsProvider credentialsProvider, BeanContext beanContext) {
        super(transportChannelProvider, credentialsProvider, beanContext)
    }

    @Override
    Subscriber createSubscriber(SubscriberFactoryConfig config) {
        MessageReceiver targetReceiver = config.receiver
        MessageReceiver countingReceiver = (PubsubMessage message, AckReplyConsumer ackReplyConsumer) -> {
            int count = messagesReceived.incrementAndGet()
            targetReceiver.receiveMessage(message, ackReplyConsumer)
        }
        SubscriberFactoryConfig modifiedConfig = new SubscriberFactoryConfig(config.subscriptionName, countingReceiver, config.subscriberConfiguration, config.defaultExecutor)
        return super.createSubscriber(modifiedConfig)
    }
}
