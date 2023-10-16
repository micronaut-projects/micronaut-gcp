package io.micronaut.gcp.pubsub.integration

import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.Subscription
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.runtime.server.EmbeddedServer
import jakarta.annotation.PreDestroy
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
        def publisher = ctx.getBean(PubSubShutdownClient);

        then:
        listener != null
        listener.count.get() == 0
        publisher != null
        subscriberServer.isRunning()

        when:
        for(int i = 0; i<100; i++) {
            publisher.send("ping")
        }

        then:
        conditions.eventually {
            listener.count.intValue() > 1
        }

        when:
        subscriberServer.stop()

        then:
        conditions.eventually {
            !subscriberServer.isRunning()
        }
        listener.count.get() == 100
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
        listener.count.get() == 0
        publisher != null
        subscriberServer.isRunning()

        when:
        for(int i = 0; i<100; i++) {
            publisher.send("ping")
        }

        then:
        conditions.eventually {
            listener.count.intValue() > 2
        }

        when:
        subscriberServer.stop()

        then:
        conditions.eventually {
            !subscriberServer.isRunning()
        }
        listener.count.get() < 100
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

    AtomicInteger count = new AtomicInteger(0)

    @Subscription(value = "topic-sub", configuration = "test")
    public void onMessage(PubsubMessage message) {
        var messageId = message.getMessageId();
        LOG.debug("Received message with ID " + messageId + ". Invoking message processor.")

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            LOG.debug("Message processing interrupted", e)
        }
        LOG.debug("Message with ID " + messageId + " contents: " + message.getData() + ".")

        int currentCount = count.incrementAndGet()
        LOG.debug("Processor finished processing message with ID " + messageId + ".")
        LOG.debug("Total Messages {}", currentCount)
    }

    @PreDestroy
    void onShutdown() {
        LOG.info("PreDestroy");
    }
}
