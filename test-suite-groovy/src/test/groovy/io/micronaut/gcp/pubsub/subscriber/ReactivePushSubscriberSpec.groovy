package io.micronaut.gcp.pubsub.subscriber

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Property
import io.micronaut.gcp.pubsub.push.PushRequest
import io.micronaut.gcp.pubsub.support.Animal
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.json.JsonMapper
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Named
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.nio.charset.StandardCharsets

@MicronautTest
@Property(name = "spec.name", value = "ReactivePushSubscriberSpec")
@Property(name = "gcp.projectId", value = "test-project")
class ReactivePushSubscriberSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient pushClient

    @Inject
    @Named("xml")
    XmlMapper xmlMapper

    Object unwrappedResult

    def setup() {
        unwrappedResult = null
    }

    void "receive raw bytes"() {
        given:
        byte[] bytesSent = "foo".getBytes(StandardCharsets.UTF_8)
        String encodedData = Base64.getEncoder().encodeToString(bytesSent)
        PushRequest request = new PushRequest("projects/test-project/subscriptions/raw-push-subscription", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status == HttpStatus.OK
        assert unwrappedResult != null
        assert unwrappedResult instanceof byte[]
        String decodedMessage = new String((unwrappedResult as byte[]), StandardCharsets.UTF_8)
        assert "foo" == decodedMessage

    }

    void "receive native message"() {
        given:
        byte[] bytesSent = "foo".getBytes(StandardCharsets.UTF_8)
        String encodedData = Base64.getEncoder().encodeToString(bytesSent)
        PushRequest request = new PushRequest("projects/test-project/subscriptions/native-push-subscription", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status == HttpStatus.OK
        assert unwrappedResult != null
        assert unwrappedResult instanceof PubsubMessage
        String decodedMessage = (unwrappedResult as PubsubMessage).getData().toString(StandardCharsets.UTF_8)
        assert "foo" == decodedMessage
    }

    void "receive pojo message from json"() {
        given:
        Animal dog = new Animal("dog")
        String encodedData = Base64.getEncoder().encodeToString(JsonMapper.createDefault().writeValueAsString(dog).getBytes())
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status == HttpStatus.OK
        assert unwrappedResult != null
        assert unwrappedResult instanceof Animal
        assert "dog" == (unwrappedResult as Animal).getName()
    }

    void "receive pojo message from xml"() {
        given:
        Animal dog = new Animal("dog")
        String encodedData = Base64.getEncoder().encodeToString(xmlMapper.writeValueAsString(dog).getBytes());
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-legacy-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status == HttpStatus.OK
        assert unwrappedResult != null
        assert unwrappedResult instanceof Animal
        assert "dog" == (unwrappedResult as Animal).getName()
    }

    @MockBean(MessageProcessor.class)
    MessageProcessor mockMessageProcessor() {
        return new MessageProcessor() {
            @Override
            Mono<Boolean> handleByteArrayMessage(byte[] message) {
                unwrappedResult = message
                return MessageProcessor.super.handleByteArrayMessage(message)
            }

            @Override
            Mono<Boolean> handlePubSubMessage(PubsubMessage pubsubMessage) {
                unwrappedResult = pubsubMessage
                return MessageProcessor.super.handlePubSubMessage(pubsubMessage)
            }

            @Override
            Mono<Boolean> handleAnimalMessage(Animal message) {
                unwrappedResult = message
                return MessageProcessor.super.handleAnimalMessage(message)
            }
        }
    }
}
