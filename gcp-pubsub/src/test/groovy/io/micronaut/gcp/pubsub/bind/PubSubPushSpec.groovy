package io.micronaut.gcp.pubsub.bind

import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.PushSubscription
import io.micronaut.gcp.pubsub.bind.PubSubPushSpec.Book
import io.micronaut.gcp.pubsub.push.PushRequest
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.json.JsonMapper
import io.micronaut.messaging.annotation.MessageHeader
import io.micronaut.serde.annotation.Serdeable
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(rebuildContext = true)
@Property(name = "spec.name", value = "PubSubPushSpec")
@Property(name = "gcp.projectId", value = "test-project")
class PubSubPushSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient pushClient

    @Inject
    PushConsumer consumer

    void setup() {
        consumer.msg = null
    }

    void "a simple push message can be received"() {
        given:
        String data = JsonMapper.createDefault().writeValueAsString(Map.of("key", "success"))
        String encodedData = Base64.getEncoder().encodeToString(data.getBytes())
        PushRequest request = new PushRequest("projects/test-project/subscriptions/foo", new PushRequest.PushMessage(Map.of("foo", "bar"), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status() == HttpStatus.OK
        consumer.msg

        when:
        Map<String, String> received = consumer.msg as Map<String, String>

        then:
        received.get("key") == "success"
    }

    void "a simple push message with no attributes but non-empty data can be received"() {
        given:
        String data = JsonMapper.createDefault().writeValueAsString(Map.of("key", "success"))
        String encodedData = Base64.getEncoder().encodeToString(data.getBytes())
        PushRequest request = new PushRequest("projects/test-project/subscriptions/foo", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status() == HttpStatus.OK
        consumer.msg

        when:
        Map<String, String> received = consumer.msg as Map<String, String>

        then:
        received.get("key") == "success"
    }

    void "a simple push message with no data but non-empty attributes can be received"() {
        given:
        PushRequest request = new PushRequest("projects/test-project/subscriptions/bar", new PushRequest.PushMessage(Map.of("foo", "bar"), null, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status() == HttpStatus.OK
        consumer.msg == "bar"
    }

    void "an invalid push message is rejected"() {
        given:
        String jsonMessage = JsonMapper.createDefault().writeValueAsString([
                "subscription" : "projects/test-project/subscriptions/foo",
                "message" : [
                        "attributes" : new HashMap<>(),
                        "data" : "",
                        "messageId" : "1",
                        "publishTime" : "2021-02-26T19:13:55.749Z"
                ]
        ])

        when:
        pushClient.toBlocking().exchange(HttpRequest.POST("/push", jsonMessage))

        then:
        HttpClientResponseException ex = thrown()
        ex.status == HttpStatus.BAD_REQUEST
    }

    void "a push message with POJO data can be received"() {
        given:
        Book book = new Book()
        book.title = "Fight Club"
        book.author = "Chuck Palahniuk"
        String data = JsonMapper.createDefault().writeValueAsString(book)
        String encodedData = Base64.getEncoder().encodeToString(data.getBytes())
        PushRequest request = new PushRequest("projects/test-project/subscriptions/books", new PushRequest.PushMessage(null, encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status() == HttpStatus.OK
        consumer.msg

        when:
        Book received = consumer.msg as Book

        then:
        received.title == book.title
        received.author == book.author
    }

    void "a push message with POJO data can be received as a PubsubMessage"() {
        given:
        Book book = new Book()
        book.title = "Fight Club"
        book.author = "Chuck Palahniuk"
        String data = JsonMapper.createDefault().writeValueAsString(book)
        String encodedData = Base64.getEncoder().encodeToString(data.getBytes())
        PushRequest request = new PushRequest("projects/test-project/subscriptions/library", new PushRequest.PushMessage(null, encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status() == HttpStatus.OK
        consumer.msg

        when:
        PubsubMessage receivedMessage = consumer.msg as PubsubMessage

        then:
        receivedMessage
        receivedMessage.messageId == "1"
        receivedMessage.publishTime.seconds == 1614366835L
        receivedMessage.publishTime.nanos == 749000000L
        receivedMessage.data

        when:
        Book receivedBook = JsonMapper.createDefault().readValue(receivedMessage.data.toByteArray(), Book.class)

        then:
        receivedBook
        receivedBook.title == book.title
        receivedBook.author == book.author
    }

    void "a push message with POJO data can be received as a raw byte array"() {
        given:
        Book book = new Book()
        book.title = "Fight Club"
        book.author = "Chuck Palahniuk"
        String data = JsonMapper.createDefault().writeValueAsString(book)
        String encodedData = Base64.getEncoder().encodeToString(data.getBytes())
        PushRequest request = new PushRequest("projects/test-project/subscriptions/raw", new PushRequest.PushMessage(null, encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        response.status() == HttpStatus.OK
        consumer.msg

        when:
        byte[] received = consumer.msg as byte[]
        Book receivedBook = JsonMapper.createDefault().readValue(received, Book.class)

        then:
        receivedBook
        receivedBook.title == book.title
        receivedBook.author == book.author
    }

    @Property(name = "gcp.pubsub.push.path", value = "/custom-push-path")
    void "a custom path may be used for push messages"() {
        given:
        String data = JsonMapper.createDefault().writeValueAsString(Map.of("key", "success"))
        String encodedData = Base64.getEncoder().encodeToString(data.getBytes())
        PushRequest request = new PushRequest("projects/test-project/subscriptions/foo", new PushRequest.PushMessage(Map.of("foo", "bar"), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/custom-push-path", request))

        then:
        response.status() == HttpStatus.OK
        consumer.msg

        when:
        Map<String, String> received = consumer.msg as Map<String, String>

        then:
        received.get("key") == "success"
    }

    @Property(name = "gcp.pubsub.push.enabled", value = StringUtils.FALSE)
    void "push messaging can be disabled"() {
        given:
        String data = JsonMapper.createDefault().writeValueAsString(Map.of("key", "success"))
        String encodedData = Base64.getEncoder().encodeToString(data.getBytes())
        PushRequest request = new PushRequest("projects/test-project/subscriptions/foo", new PushRequest.PushMessage(Map.of("foo", "bar"), encodedData, "1", "2021-02-26T19:13:55.749Z"))

        when:
        HttpResponse response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        then:
        then:
        HttpClientResponseException ex = thrown()
        ex.status == HttpStatus.NOT_FOUND
    }

    @Serdeable
    static class Book {
        String title
        String author
    }
}

@Requires(property = "spec.name", value = "PubSubPushSpec")
@PubSubListener
class PushConsumer {

    Object msg

    @PushSubscription("foo")
    void message1(Map<String, String> data) {
        this.msg = data
    }

    @PushSubscription("bar")
    void message2(@MessageHeader("foo") String header) {
        this.msg = header
    }

    @PushSubscription("books")
    void message3(Book book) {
        this.msg = book
    }

    @PushSubscription("library")
    void message4(PubsubMessage message) {
        this.msg = message
    }

    @PushSubscription("raw")
    void message5(byte[] data) {
        this.msg = data
    }
}
