package subscriber

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Property
import io.micronaut.gcp.pubsub.push.PushRequest
import io.micronaut.gcp.pubsub.subscriber.ContentTypePushSubscriber
import io.micronaut.gcp.pubsub.subscriber.MessageProcessor
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
import org.jetbrains.annotations.NotNull
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.nio.charset.StandardCharsets

@MicronautTest
@Property(name = "spec.name", value = "ContentTypePushSubscriberSpec")
@Property(name = "gcp.projectId", value = "test-project")
class ContentTypePushSubscriberSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient pushClient

    @Inject
    ContentTypePushSubscriber subscriber

    @Inject
    @Named("xml")
    XmlMapper xmlMapper

    @Inject
    JsonMapper jsonMapper

    Object receivedMessage

    def setup() {
        receivedMessage = null
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
        assert receivedMessage != null
        assert receivedMessage instanceof byte[]
        String decodedMessage = new String((receivedMessage as byte[]), StandardCharsets.UTF_8)
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
        assert receivedMessage != null
        assert receivedMessage instanceof PubsubMessage
        String decodedMessage = (receivedMessage as PubsubMessage).getData().toString(StandardCharsets.UTF_8)
        assert "foo" == decodedMessage
    }

    void "receive pojo message from json"() {
        given:
        Animal dog = new Animal("dog")
        String encodedData = Base64.getEncoder().encodeToString(jsonMapper.writeValueAsBytes(dog))
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status == HttpStatus.OK
        assert receivedMessage != null
        assert receivedMessage instanceof Animal
        assert "dog" == (receivedMessage as Animal).getName()
    }

    void "receive pojo message from xml"() {
        given:
        Animal dog = new Animal("dog")
        String encodedData = Base64.getEncoder().encodeToString(xmlMapper.writeValueAsBytes(dog));
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-legacy-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status == HttpStatus.OK
        assert receivedMessage != null
        assert receivedMessage instanceof Animal
        assert "dog" == (receivedMessage as Animal).getName()
    }

    @MockBean(MessageProcessor)
    MessageProcessor subscriberForTest() {
        return new MessageProcessor() {

            @Override
            Mono<Boolean> handleByteArrayMessage(@NotNull byte[] message) {
                receivedMessage = message
                return Mono.just(Boolean.TRUE)
            }

            @Override
            Mono<Boolean> handlePubsubMessage(@NotNull PubsubMessage pubsubMessage) {
                receivedMessage = pubsubMessage
                return Mono.just(Boolean.TRUE)
            }

            @Override
            Mono<Boolean> handleAnimalMessage(@NotNull Animal message) {
                receivedMessage = message
                return Mono.just(Boolean.TRUE)
            }
        }
    }
}
