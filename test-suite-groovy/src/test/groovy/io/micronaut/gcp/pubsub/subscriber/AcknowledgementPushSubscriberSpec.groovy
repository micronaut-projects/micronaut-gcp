package io.micronaut.gcp.pubsub.subscriber

import io.micronaut.context.annotation.Property
import io.micronaut.gcp.pubsub.bind.DefaultPubSubAcknowledgement
import io.micronaut.gcp.pubsub.push.PushRequest
import io.micronaut.gcp.pubsub.support.Animal
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.json.JsonMapper
import io.micronaut.messaging.Acknowledgement
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import reactor.core.publisher.Mono
import spock.lang.Specification

@MicronautTest
@Property(name = "spec.name", value = "AcknowledgementPushSubscriberSpec")
@Property(name = "gcp.projectId", value = "test-project")
class AcknowledgementPushSubscriberSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient pushClient

    MessageProcessor messageProcessor

    @Inject
    AcknowledgementPushSubscriber subscriber

    @Inject
    JsonMapper jsonMapper

    Object receivedMessage

    Acknowledgement acknowledgement

    def setup() {
        receivedMessage = null
        acknowledgement = null
    }

    void "blocking subscriber with manual ack"() {
        setup:
        messageProcessor.handleAnimalMessage(_ as Animal) >> Mono.just(Boolean.TRUE)
        Animal dog = new Animal("dog")
        String encodedData = Base64.getEncoder().encodeToString(jsonMapper.writeValueAsBytes(dog))
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status == HttpStatus.OK
        assert receivedMessage != null
        assert receivedMessage instanceof Animal
        assert (receivedMessage as Animal).name == "dog"
        assert acknowledgement instanceof DefaultPubSubAcknowledgement
        assert (acknowledgement as DefaultPubSubAcknowledgement).isClientAck()
    }

    void "blocking subscriber with manual nack"() {
        setup:
        messageProcessor.handleAnimalMessage(_ as Animal) >> Mono.just(Boolean.FALSE)
        Animal dog = new Animal("dog")
        String encodedData = Base64.getEncoder().encodeToString(jsonMapper.writeValueAsBytes(dog))
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        HttpClientResponseException ex = thrown()
        ex.response.status() == HttpStatus.UNPROCESSABLE_ENTITY
        assert receivedMessage != null
        assert receivedMessage instanceof Animal
        assert (receivedMessage as Animal).name == "dog"
        assert acknowledgement instanceof DefaultPubSubAcknowledgement
        assert (acknowledgement as DefaultPubSubAcknowledgement).isClientAck()
    }

    void "async subscriber with manual ack"() {
        setup:
        messageProcessor.handleAnimalMessage(_ as Animal) >> Mono.just(Boolean.TRUE)
        Animal dog = new Animal("dog")
        String encodedData = Base64.getEncoder().encodeToString(jsonMapper.writeValueAsBytes(dog))
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-async-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status == HttpStatus.OK
        assert receivedMessage != null
        assert receivedMessage instanceof Mono<Animal>
        assert acknowledgement instanceof DefaultPubSubAcknowledgement
        assert (acknowledgement as DefaultPubSubAcknowledgement).isClientAck()
    }

    void "async subscriber with manual nack"() {
        setup:
        messageProcessor.handleAnimalMessage(_ as Animal) >> Mono.just(Boolean.FALSE)
        Animal dog = new Animal("dog")
        String encodedData = Base64.getEncoder().encodeToString(jsonMapper.writeValueAsBytes(dog))
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-async-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        HttpClientResponseException ex = thrown()
        ex.response.status() == HttpStatus.UNPROCESSABLE_ENTITY
        assert receivedMessage != null
        assert receivedMessage instanceof Mono<Animal>
        assert acknowledgement instanceof DefaultPubSubAcknowledgement
        assert (acknowledgement as DefaultPubSubAcknowledgement).isClientAck()
    }

    @MockBean(AcknowledgementPushSubscriber)
    AcknowledgementPushSubscriber subscriberForTest() {
        messageProcessor = Mock(MessageProcessor)
        return new TestAcknowledgementPushSubscriber(messageProcessor)
    }

    class TestAcknowledgementPushSubscriber extends AcknowledgementPushSubscriber {

        TestAcknowledgementPushSubscriber(MessageProcessor messageProcessor) {
            super(messageProcessor)
        }

        @Override
        void onMessage(Animal animal, Acknowledgement ack) {
            receivedMessage = animal
            acknowledgement = ack
            super.onMessage(animal, ack)
        }

        @Override
        Mono<Boolean> onReactiveMessage(Mono<Animal> animal, Acknowledgement ack) {
            receivedMessage = animal
            acknowledgement = ack
            return super.onReactiveMessage(animal, ack)
        }
    }
}
