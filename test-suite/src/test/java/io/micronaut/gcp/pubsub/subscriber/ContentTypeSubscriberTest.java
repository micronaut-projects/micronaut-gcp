package io.micronaut.gcp.pubsub.subscriber;

import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.gcp.pubsub.annotation.PubSubClient;
import io.micronaut.gcp.pubsub.annotation.Topic;
import io.micronaut.gcp.pubsub.support.Animal;
import io.micronaut.http.MediaType;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@MicronautTest
@Property(name = "spec.name", value = "ContentTypeSubscriberTest")
public class ContentTypeSubscriberTest {

    @Inject
    TestPublisher publisher;

    @Inject
    ContentTypeSubscriber subscriber;

    @BeforeEach
    void setup() {
        Mockito.reset(subscriber);
    }

    @Test
    void testRawBytes() {
        byte[] bytesSent = "foo".getBytes(StandardCharsets.UTF_8);
        publisher.publishRaw(bytesSent);

        ArgumentCaptor<byte[]> bytesReceived = ArgumentCaptor.forClass(byte[].class);
        verify(subscriber, timeout(3000)).receiveRaw(bytesReceived.capture(), any());
        String decodedMessage = new String(bytesReceived.getValue(), StandardCharsets.UTF_8);
        Assertions.assertEquals("foo", decodedMessage);
    }

    @Test
    void testNativeMessage() {
        byte[] bytesSent = "foo".getBytes(StandardCharsets.UTF_8);
        publisher.publishNative(bytesSent);

        ArgumentCaptor<PubsubMessage> messageReceived = ArgumentCaptor.forClass(PubsubMessage.class);
        verify(subscriber, timeout(3000)).receiveNative(messageReceived.capture());
        String decodedMessage = messageReceived.getValue().getData().toString(StandardCharsets.UTF_8);
        Assertions.assertEquals("foo", decodedMessage);
    }

    @Test
    void testJsonPojo() {
        Animal dog = new Animal("dog");
        publisher.publishAnimal(dog);

        ArgumentCaptor<Animal> messageReceived = ArgumentCaptor.forClass(Animal.class);
        verify(subscriber, timeout(3000)).receivePojo(messageReceived.capture(), any());
        Assertions.assertNotNull(messageReceived.getValue());
        Assertions.assertEquals("dog", messageReceived.getValue().getName());
    }

    @Test
    void testXmlPojo() {
        Animal dog = new Animal("cat");
        publisher.publishAnimalAsXml(dog);

        ArgumentCaptor<Animal> messageReceived = ArgumentCaptor.forClass(Animal.class);
        verify(subscriber, timeout(3000)).receiveXML(messageReceived.capture(), any());
        Assertions.assertNotNull(messageReceived.getValue());
        Assertions.assertEquals("cat", messageReceived.getValue().getName());
    }

    @Singleton
    @PubSubClient
    @Requires(property = "spec.name", value = "ContentTypeSubscriberTest")
    interface TestPublisher {
        @Topic("raw-subscription") void publishRaw(byte[] payload);
        @Topic("native-subscription") void publishNative(byte[] payload);
        @Topic("animals") void publishAnimal(Animal animal);
        @Topic(value = "animals-legacy", contentType = MediaType.APPLICATION_XML) void publishAnimalAsXml(Animal animal);
    }

    @Singleton
    @Requires(property = "spec.name", value = "ContentTypeSubscriberTest")
    static class SubscriberCreatedListener implements BeanCreatedEventListener<ContentTypeSubscriber> {
        @Override
        public ContentTypeSubscriber onCreated(@NonNull BeanCreatedEvent<ContentTypeSubscriber> event) {
            return spy(event.getBean());
        }
    }
}
