package io.micronaut.gcp.pubsub.bind

import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.PushSubscription
import io.micronaut.gcp.pubsub.exception.DefaultPubSubMessageReceiverExceptionHandler
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverException
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverExceptionHandler
import io.micronaut.gcp.pubsub.push.PushRequest
import io.micronaut.gcp.pubsub.support.Person
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.messaging.Acknowledgement
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@MicronautTest
@Property(name = "spec.name", value = "PushAcknowledgementSpec")
@Property(name = "gcp.projectId", value = "test-project")
class PushAcknowledgementSpec extends Specification {

    @Inject
    DefaultPubSubMessageReceiverExceptionHandlerPushWrapper exceptionHandler

    @Inject
    @Client("/")
    HttpClient pushClient

    @Inject
    PushAcknowledgementListener listener

    @Inject
    PushAcknowledgementListenerWithHandler listenerWithHandler

    PollingConditions conditions = new PollingConditions(timeout: 3)

    def setup() {
        listener.msg = null
        listener.manuallyProcessed = false
        exceptionHandler.ex = null
        listenerWithHandler.ex = null
    }

    void "automatically ack successfully processed message"() {
        given:
        byte[] payload = "success".getBytes()
        String encodedData = Base64.getEncoder().encodeToString(payload)
        PushRequest request = new PushRequest("projects/test-project/subscriptions/test-topic-with-message-auto-ack",
                new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status() == HttpStatus.OK
        listener.msg
        listener.msg.getData().toByteArray() == payload
    }

    void "manually ack successfully processed message"() {
        given:
        byte[] payload = "manual-success".getBytes()
        String encodedData = Base64.getEncoder().encodeToString(payload)
        PushRequest request = new PushRequest("projects/test-project/subscriptions/test-topic-with-message-manually-acknowledged",
                new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status() == HttpStatus.OK
        listener.msg
        listener.msg.getData().toByteArray() == payload
        listener.manuallyProcessed
    }

    void "automatically nack message with default exception handler"() {
        given:
        byte[] invalidPayload = "foo".getBytes()
        String encodedData = Base64.getEncoder().encodeToString(invalidPayload)
        PushRequest request = new PushRequest("projects/test-project/subscriptions/test-topic-no-handler",
                new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        HttpClientResponseException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        exceptionHandler instanceof DefaultPubSubMessageReceiverExceptionHandler
        exceptionHandler.ex != null
        exceptionHandler.ex.state.pubsubMessage.data
        exceptionHandler.ex.state.pubsubMessage.data.toByteArray() == invalidPayload
    }

    void "automatically nack message when subscription method indicates manual acknowledgement but message binding fails"() {
        given:
        byte[] invalidPayload = "baz".getBytes()
        String encodedData = Base64.getEncoder().encodeToString(invalidPayload)
        PushRequest request = new PushRequest("projects/test-project/subscriptions/test-topic-no-handler-manually-acknowledged",
                new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        HttpClientResponseException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        exceptionHandler instanceof DefaultPubSubMessageReceiverExceptionHandler
        exceptionHandler.ex != null
        exceptionHandler.ex.state.pubsubMessage.data
        exceptionHandler.ex.state.pubsubMessage.data.toByteArray() == invalidPayload
    }

    void "nack message from a custom exception handler"() {
        given:
        byte[] invalidPayload = "bar".getBytes()
        String encodedData = Base64.getEncoder().encodeToString(invalidPayload)
        PushRequest request = new PushRequest("projects/test-project/subscriptions/test-topic-handler",
                new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        HttpClientResponseException ex = thrown()
        ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        exceptionHandler.ex == null
        listenerWithHandler.ex
        listenerWithHandler.ex.state.pubsubMessage.data
        listenerWithHandler.ex.state.pubsubMessage.data.toByteArray() == invalidPayload
    }

}

@PubSubListener
@Requires(property = "spec.name", value = "PushAcknowledgementSpec")
class PushAcknowledgementListener {
    PubsubMessage msg
    boolean manuallyProcessed
    @PushSubscription("test-topic-with-message-auto-ack") void onMessage(PubsubMessage msg) { this.msg = msg }
    @PushSubscription("test-topic-with-message-manually-acknowledged") void onMessageManualAck(PubsubMessage msg, Acknowledgement acknowledgement) {
        this.msg = msg
        this.manuallyProcessed = true
        acknowledgement.ack()
    }
    @PushSubscription("test-topic-no-handler") void onPerson(Person person) {}
    @PushSubscription("test-topic-no-handler-manually-acknowledged") void onPersonManualAck(Person person, Acknowledgement acknowledgement) {}
}

@PubSubListener
@Requires(property = "spec.name", value = "PushAcknowledgementSpec")
class PushAcknowledgementListenerWithHandler implements PubSubMessageReceiverExceptionHandler {
    PubSubMessageReceiverException ex
    @PushSubscription("test-topic-handler") void onMessage(Person person) {}
    @Override void handle(PubSubMessageReceiverException exception) {
        ex = exception
        ex.state.ackReplyConsumer.nack()
    }
}

@Singleton
@Primary
@Replaces(DefaultPubSubMessageReceiverExceptionHandler)
@Requires(property = "spec.name", value = "PushAcknowledgementSpec")
class DefaultPubSubMessageReceiverExceptionHandlerPushWrapper extends DefaultPubSubMessageReceiverExceptionHandler {

    public PubSubMessageReceiverException ex

    @Override
    void handle(PubSubMessageReceiverException exception) {
        this.ex = exception
        super.handle(ex);
    }
}
