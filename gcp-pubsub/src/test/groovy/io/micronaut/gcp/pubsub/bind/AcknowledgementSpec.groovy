package io.micronaut.gcp.pubsub.bind

import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.AbstractConsumerSpec
import io.micronaut.gcp.pubsub.MockPubSubEngine
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.Subscription
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.exception.DefaultPubSubMessageReceiverExceptionHandler
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverException
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverExceptionHandler
import io.micronaut.gcp.pubsub.support.Person
import io.micronaut.messaging.Acknowledgement
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.util.concurrent.PollingConditions

@MicronautTest
@Property(name = "spec.name", value = "AcknowledgementSpec")
@Property(name = "gcp.projectId", value = "test-project")
class AcknowledgementSpec extends AbstractConsumerSpec {

    @Inject
    DefaultPubSubMessageReceiverExceptionHandlerWrapper exceptionHandler

    @Inject
    AcknowledgementClient client

    @Inject
    AcknowledgementListener listener

    @Inject
    AcknowledgementListenerWithHandler listenerWithHandler

    @Inject
    MockPubSubEngine mockPubSubEngine

    PollingConditions conditions = new PollingConditions(timeout: 3)

    def setup() {
        listener.msg = null
        listener.manuallyProcessed = false
        listenerWithHandler.ex = null;
    }

    void "automatically ack successfully processed message"() {
        given:
        byte[] payload = "success".getBytes()

        when:
        client.publishTopicAutoAck(payload)

        then:
        conditions.eventually {
            listener.msg != null
            def msg = listener.msg
            mockPubSubEngine.acknowledgements.containsKey(msg)
            mockPubSubEngine.acknowledgements.get(msg) == MockPubSubEngine.ACK
        }
    }

    void "do not automatically ack a message when subscription method indicates manual acknowledgement"() {
        given:
        byte[] payload = "manual-success".getBytes()

        when:
        client.publishTopicManualAck(payload)

        then:
        conditions.eventually {
            listener.manuallyProcessed
            def msg = listener.msg
            !mockPubSubEngine.acknowledgements.containsKey(msg)
        }
    }

    void "automatically nack message with default exception handler"() {
        given:
        byte[] invalidPayload = "foo".getBytes()

        when:
        client.publishTopicNoHandler(invalidPayload)

        then:
        exceptionHandler instanceof DefaultPubSubMessageReceiverExceptionHandler
        conditions.eventually {
            exceptionHandler.ex != null
            def msg = exceptionHandler.ex.state.pubsubMessage
            mockPubSubEngine.acknowledgements.containsKey(msg)
            mockPubSubEngine.acknowledgements.get(msg) == MockPubSubEngine.NACK
        }
    }

    void "do not automatically nack message when subscription method indicates manual acknowledgement"() {
        given:
        byte[] invalidPayload = "baz".getBytes()

        when:
        client.publishTopicNoHandlerManuallyAcknowledged(invalidPayload)

        then:
        exceptionHandler instanceof DefaultPubSubMessageReceiverExceptionHandler
        conditions.eventually {
            exceptionHandler.ex != null
            def msg = exceptionHandler.ex.state.pubsubMessage
            !mockPubSubEngine.acknowledgements.containsKey(msg)
        }
    }

    void "do not automatically nack message when a custom exception handler is provided"() {
        given:
        byte[] invalidPayload = "bar".getBytes()

        when:
        client.publishTopicHandler(invalidPayload)

        then:
        exceptionHandler instanceof DefaultPubSubMessageReceiverExceptionHandler
        conditions.eventually {
            listenerWithHandler.ex != null
            def msg = listenerWithHandler.ex.state.pubsubMessage
            !mockPubSubEngine.acknowledgements.containsKey(msg)
        }
    }

    void "backwards-compatible behavior of auto acknowledge is retained for user tests"() {
        when:
        PubSubMessageReceiverException ex1 = new PubSubMessageReceiverException("test message 1", listener, null)
        PubSubMessageReceiverException ex2 = new PubSubMessageReceiverException("test message 2", new IllegalStateException(), listener, null)

        then:
        !ex1.isAutoAcknowledge()
        !ex2.isAutoAcknowledge()
    }
}

@PubSubClient
@Requires(property = "spec.name", value = "AcknowledgementSpec")
interface AcknowledgementClient {
    @Topic("test-topic-with-message-auto-ack") void publishTopicAutoAck(byte[] data)
    @Topic("test-topic-with-message-manually-acknowledged") void publishTopicManualAck(byte[] data)
    @Topic("test-topic-no-handler") void publishTopicNoHandler(byte[] data)
    @Topic("test-topic-no-handler-manually-acknowledged") void publishTopicNoHandlerManuallyAcknowledged(byte[] data)
    @Topic("test-topic-handler") void publishTopicHandler(byte[] data)
}

@PubSubListener
@Requires(property = "spec.name", value = "AcknowledgementSpec")
class AcknowledgementListener {
    PubsubMessage msg
    boolean manuallyProcessed
    @Subscription("test-topic-with-message-auto-ack") void onMessage(PubsubMessage msg) { this.msg = msg }
    @Subscription("test-topic-with-message-manually-acknowledged") void onMessageManualAck(PubsubMessage msg, Acknowledgement acknowledgement) {
        this.msg = msg
        this.manuallyProcessed = true
    }
    @Subscription("test-topic-no-handler") void onPerson(Person person) {}
    @Subscription("test-topic-no-handler-manually-acknowledged") void onPersonManualAck(Person person, Acknowledgement acknowledgement) {}
}

@PubSubListener
@Requires(property = "spec.name", value = "AcknowledgementSpec")
class AcknowledgementListenerWithHandler implements PubSubMessageReceiverExceptionHandler {
    PubSubMessageReceiverException ex
    @Subscription("test-topic-handler") void onMessage(Person person) {}
    @Override void handle(PubSubMessageReceiverException exception) { ex = exception }
}

@Singleton
@Primary
@Replaces(DefaultPubSubMessageReceiverExceptionHandler)
@Requires(property = "spec.name", value = "AcknowledgementSpec")
class DefaultPubSubMessageReceiverExceptionHandlerWrapper extends DefaultPubSubMessageReceiverExceptionHandler {

    public PubSubMessageReceiverException ex

    @Override
    void handle(PubSubMessageReceiverException exception) {
        this.ex = exception
        super.handle(ex);
    }
}
