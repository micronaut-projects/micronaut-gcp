package io.micronaut.gcp.pubsub.subscriber

import io.micronaut.context.annotation.Property
import io.micronaut.gcp.pubsub.push.PushRequest
import io.micronaut.gcp.pubsub.support.Animal
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.json.JsonMapper
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import org.jspecify.annotations.NonNull
import reactor.core.publisher.Mono
import spock.lang.Specification
import io.micronaut.pubsub.testcontainers.PubSubEmulator

@MicronautTest
@Property(name = "spec.name", value = "AcknowledgementPushSubscriberSpec")
@Property(name = "gcp.projectId", value = "test-project")
class AcknowledgementPushSubscriberSpec extends Specification implements TestPropertyProvider {
    @Override
    @NonNull Map<String, String> getProperties() {
        return PubSubEmulator.getProperties();
    }

    @Inject
    @Client("/")
    HttpClient pushClient

    @Inject
    JsonMapper jsonMapper

    Object receivedMessage

    boolean nack

    def setup() {
        receivedMessage = null
        nack = false
    }

    void "blocking subscriber with manual ack"() {
        setup:
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
    }

    void "blocking subscriber with manual nack"() {
        setup:
        nack = true
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
    }

    void "async subscriber with manual ack"() {
        setup:
        Animal dog = new Animal("dog")
        String encodedData = Base64.getEncoder().encodeToString(jsonMapper.writeValueAsBytes(dog))
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-async-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status == HttpStatus.OK
        assert receivedMessage != null
        assert receivedMessage instanceof Animal
    }

    void "async subscriber with manual nack"() {
        setup:
        nack = true
        Animal dog = new Animal("dog")
        String encodedData = Base64.getEncoder().encodeToString(jsonMapper.writeValueAsBytes(dog))
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-async-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        HttpClientResponseException ex = thrown()
        ex.response.status() == HttpStatus.UNPROCESSABLE_ENTITY
        assert receivedMessage != null
        assert receivedMessage instanceof Animal
    }

    @MockBean(MessageProcessor)
    MessageProcessor messageProcessor() {
        return new MessageProcessor() {
            @Override
            Mono<Boolean> handleAnimalMessage(Animal animal) {
                receivedMessage = animal
                boolean result = !nack
                nack = false
                return Mono.just(result)
            }
        }
    }
}
