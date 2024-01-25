package io.micronaut.gcp.pubsub.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.context.annotation.Property;
import io.micronaut.gcp.pubsub.push.PushRequest;
import io.micronaut.gcp.pubsub.support.Animal;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.json.JsonMapper;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

@MicronautTest
@Property(name = "spec.name", value = "ReactivePushSubscriberTest")
@Property(name = "gcp.projectId", value = "test-project")
class ReactivePushSubscriberTest {

    @Inject
    @Client("/")
    HttpClient pushClient;

    @Inject
    @Named("xml")
    XmlMapper xmlMapper;

    @Inject
    JsonMapper jsonMapper;

    Object unwrappedResult;

    @BeforeEach
    void setup() {
        unwrappedResult = null;
    }

    @Test
    void testRawBytes() {
        byte[] bytesSent = "foo".getBytes(StandardCharsets.UTF_8);
        String encodedData = Base64.getEncoder().encodeToString(bytesSent);
        PushRequest request = new PushRequest("projects/test-project/subscriptions/raw-push-subscription", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"));
        HttpResponse<?> response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request));

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(unwrappedResult);
        Assertions.assertInstanceOf(byte[].class, unwrappedResult);
        byte[] convertedResult = (byte[]) unwrappedResult;
        String decodedMessage = new String(convertedResult, StandardCharsets.UTF_8);
        Assertions.assertEquals("foo", decodedMessage);
    }

    @Test
    void testNativeMessage() {
        byte[] bytesSent = "foo".getBytes(StandardCharsets.UTF_8);
        String encodedData = Base64.getEncoder().encodeToString(bytesSent);
        PushRequest request = new PushRequest("projects/test-project/subscriptions/native-push-subscription", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"));
        HttpResponse<?> response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request));

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(unwrappedResult);
        Assertions.assertInstanceOf(PubsubMessage.class, unwrappedResult);
        PubsubMessage convertedResult = (PubsubMessage) unwrappedResult;
        String decodedMessage = convertedResult.getData().toString(StandardCharsets.UTF_8);
        Assertions.assertEquals("foo", decodedMessage);
    }


    @Test
    void testJsonPojo() throws IOException {
        Animal dog = new Animal("dog");
        String encodedData = Base64.getEncoder().encodeToString(jsonMapper.writeValueAsBytes(dog));
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"));
        HttpResponse<?> response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request));

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(unwrappedResult);
        Assertions.assertInstanceOf(Animal.class, unwrappedResult);
        Animal convertedResult = (Animal) unwrappedResult;
        Assertions.assertEquals("dog", convertedResult.getName());
    }

    @Test
    void testXmlPojo() throws JsonProcessingException {
        Animal dog = new Animal("cat");
        String encodedData = Base64.getEncoder().encodeToString(xmlMapper.writeValueAsBytes(dog));
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-legacy-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"));
        HttpResponse<?> response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request));

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(unwrappedResult);
        Assertions.assertInstanceOf(Animal.class, unwrappedResult);
        Animal convertedResult = (Animal) unwrappedResult;
        Assertions.assertEquals("cat", convertedResult.getName());
    }

    @MockBean(MessageProcessor.class)
    MessageProcessor mockMessageProcessor() {
        return new MessageProcessor() {
            @Override
            public Mono<Boolean> handleByteArrayMessage(byte[] message) {
                unwrappedResult = message;
                return MessageProcessor.super.handleByteArrayMessage(message);
            }

            @Override
            public Mono<Boolean> handlePubSubMessage(PubsubMessage pubsubMessage) {
                unwrappedResult = pubsubMessage;
                return MessageProcessor.super.handlePubSubMessage(pubsubMessage);
            }

            @Override
            public Mono<Boolean> handleAnimalMessage(Animal message) {
                unwrappedResult = message;
                return MessageProcessor.super.handleAnimalMessage(message);
            }
        };
    }
}
