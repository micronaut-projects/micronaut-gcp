package io.micronaut.gcp.pubsub.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.gcp.pubsub.push.PushRequest;
import io.micronaut.gcp.pubsub.support.Animal;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.json.JsonMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//tag::clazzBegin[]
@MicronautTest
@Property(name = "spec.name", value = "ContentTypePushSubscriberTest")
@Property(name = "gcp.projectId", value = "test-project")
class ContentTypePushSubscriberSpec {
//end::clazzBegin[]

//tag::injectClient[]
    @Inject
    @Client("/")
    HttpClient pushClient;
//end::injectClient[]
    @Inject
    ContentTypePushSubscriber subscriber;

    @BeforeEach
    void setup() {
        Mockito.reset(subscriber);
    }

    @Inject
    @Named("xml")
    XmlMapper xmlMapper;

    @Test
    void testRawBytes() {
        byte[] bytesSent = "foo".getBytes(StandardCharsets.UTF_8);
        String encodedData = Base64.getEncoder().encodeToString(bytesSent);
        PushRequest request = new PushRequest("projects/test-project/subscriptions/raw-push-subscription",
            new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"));
        HttpResponse<?> response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request));

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        ArgumentCaptor<byte[]> bytesReceived = ArgumentCaptor.forClass(byte[].class);
        verify(subscriber, timeout(3000)).receiveRaw(bytesReceived.capture(), any());
        String decodedMessage = new String(bytesReceived.getValue(), StandardCharsets.UTF_8);
        Assertions.assertEquals("foo", decodedMessage);
    }

    @Test
    void testNativeMessage() {
        byte[] bytesSent = "foo".getBytes(StandardCharsets.UTF_8);
        String encodedData = Base64.getEncoder().encodeToString(bytesSent);
        PushRequest request = new PushRequest("projects/test-project/subscriptions/native-push-subscription", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"));
        HttpResponse<?> response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request));

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        ArgumentCaptor<PubsubMessage> messageReceived = ArgumentCaptor.forClass(PubsubMessage.class);
        verify(subscriber, timeout(3000)).receiveNative(messageReceived.capture());
        String decodedMessage = messageReceived.getValue().getData().toString(StandardCharsets.UTF_8);
        Assertions.assertEquals("foo", decodedMessage);
    }

//tag::testMethodBegin[]
    @Test
    void testJsonPojo() throws IOException {
        Animal dog = new Animal("dog");

        String encodedData = Base64.getEncoder().encodeToString(JsonMapper.createDefault().writeValueAsString(dog).getBytes()); // <1>

        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-push", // <2>
            new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"));

        HttpResponse<?> response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request)); // <3>

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
//end::testMethodBegin[]
        ArgumentCaptor<Animal> messageReceived = ArgumentCaptor.forClass(Animal.class);
        verify(subscriber, timeout(3000)).receivePojo(messageReceived.capture(), any());
        Assertions.assertNotNull(messageReceived.getValue());
        Assertions.assertEquals("dog", messageReceived.getValue().getName());
//tag::testMethodEnd[]
    }
//end::testMethodEnd[]

    @Test
    void testXmlPojo() throws JsonProcessingException {
        Animal dog = new Animal("cat");
        String encodedData = Base64.getEncoder().encodeToString(xmlMapper.writeValueAsString(dog).getBytes());
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-legacy-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"));
        HttpResponse<?> response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request));

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        ArgumentCaptor<Animal> messageReceived = ArgumentCaptor.forClass(Animal.class);
        verify(subscriber, timeout(3000)).receiveXML(messageReceived.capture(), any());
        Assertions.assertNotNull(messageReceived.getValue());
        Assertions.assertEquals("cat", messageReceived.getValue().getName());
    }

    @Singleton
    @Requires(property = "spec.name", value = "ContentTypePushSubscriberTest")
    static class SubscriberCreatedListener implements BeanCreatedEventListener<ContentTypePushSubscriber> {
        @Override
        public ContentTypePushSubscriber onCreated(@NonNull BeanCreatedEvent<ContentTypePushSubscriber> event) {
            return spy(event.getBean());
        }
    }

//tag::clazzEnd[]
}
//end::clazzEnd[]
