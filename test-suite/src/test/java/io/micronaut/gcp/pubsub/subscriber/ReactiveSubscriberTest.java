package io.micronaut.gcp.pubsub.subscriber;

import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.gcp.pubsub.annotation.PubSubClient;
import io.micronaut.gcp.pubsub.annotation.Topic;
import io.micronaut.gcp.pubsub.support.Animal;
import io.micronaut.http.MediaType;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.awaitility.Awaitility.await;

@MicronautTest
@Property(name = "spec.name", value = "ReactiveSubscriberTest")
public class ReactiveSubscriberTest {

    @Inject
    TestPublisher publisher;

    Object unwrappedResult;

    @BeforeEach
    void setup() {
        unwrappedResult = null;
    }

    @Test
    void testRawBytes() {
        byte[] bytesSent = "foo".getBytes(StandardCharsets.UTF_8);
        publisher.publishRaw(bytesSent);

        await().atMost(Duration.ofSeconds(2)).until(() -> unwrappedResult != null);
        Assertions.assertInstanceOf(byte[].class, unwrappedResult);
        byte[] convertedResult = (byte[]) unwrappedResult;
        String decodedMessage = new String(convertedResult, StandardCharsets.UTF_8);
        Assertions.assertEquals("foo", decodedMessage);
    }

    @Test
    void testNativeMessage() {
        byte[] bytesSent = "foo".getBytes(StandardCharsets.UTF_8);
        publisher.publishNative(bytesSent);

        await().atMost(Duration.ofSeconds(2)).until(() -> unwrappedResult != null);
        Assertions.assertInstanceOf(PubsubMessage.class, unwrappedResult);
        PubsubMessage convertedResult = (PubsubMessage) unwrappedResult;
        String decodedMessage = convertedResult.getData().toString(StandardCharsets.UTF_8);
        Assertions.assertEquals("foo", decodedMessage);
    }

    @Test
    void testJsonPojo() {
        Animal dog = new Animal("dog");
        publisher.publishAnimal(dog);

        await().atMost(Duration.ofSeconds(2)).until(() -> unwrappedResult != null);
        Assertions.assertInstanceOf(Animal.class, unwrappedResult);
        Animal convertedResult = (Animal) unwrappedResult;
        Assertions.assertEquals("dog", convertedResult.getName());
    }

    @Test
    void testXmlPojo() {
        Animal dog = new Animal("cat");
        publisher.publishAnimalAsXml(dog);

        await().atMost(Duration.ofSeconds(2)).until(() -> unwrappedResult != null);
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

    @Singleton
    @PubSubClient
    @Requires(property = "spec.name", value = "ReactiveSubscriberTest")
    interface TestPublisher {
        @Topic("raw-subscription") void publishRaw(byte[] payload);
        @Topic("native-subscription") void publishNative(byte[] payload);
        @Topic("animals") void publishAnimal(Animal animal);
        @Topic(value = "animals-legacy", contentType = MediaType.APPLICATION_XML) void publishAnimalAsXml(Animal animal);
    }
}
