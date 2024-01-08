package io.micronaut.gcp.pubsub.subscriber;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.gcp.pubsub.annotation.PubSubClient;
import io.micronaut.gcp.pubsub.annotation.Topic;
import io.micronaut.gcp.pubsub.bind.DefaultPubSubAcknowledgement;
import io.micronaut.gcp.pubsub.push.PushRequest;
import io.micronaut.gcp.pubsub.support.Animal;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.json.JsonMapper;
import io.micronaut.messaging.Acknowledgement;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MicronautTest
@Property(name = "spec.name", value = "AcknowledgementPushSubscriberTest")
@Property(name = "gcp.projectId", value = "test-project")
class AcknowledgementPushSubscriberTest {

    @Inject
    @Client("/")
    HttpClient pushClient;

    @Inject
    MessageProcessor messageProcessor;

    @Inject
    AcknowledgementPushSubscriber subscriber;

    @BeforeEach
    void setup() {
        Mockito.reset(messageProcessor, subscriber);
    }

    @Test
    void testBlockingAck() throws IOException {
        when(messageProcessor.handleAnimalMessage(any())).thenReturn(Mono.just(Boolean.TRUE));

        Animal dog = new Animal("dog");
        String encodedData = Base64.getEncoder().encodeToString(JsonMapper.createDefault().writeValueAsString(dog).getBytes());
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"));
        HttpResponse<?> response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request));

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    void testBlockingNack() throws IOException {
        when(messageProcessor.handleAnimalMessage(any())).thenReturn(Mono.just(Boolean.FALSE));

        Animal cat = new Animal("cat");
        String encodedData = Base64.getEncoder().encodeToString(JsonMapper.createDefault().writeValueAsString(cat).getBytes());
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"));

        try {
            pushClient.toBlocking().exchange(HttpRequest.POST("/push", request));
            Assertions.fail();
        } catch (HttpClientResponseException ex) {
            Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getResponse().getStatus());
        }
    }

    @Test
    void testAsyncAck() throws IOException {
        when(messageProcessor.handleAnimalMessage(any())).thenReturn(Mono.just(Boolean.TRUE));

        Animal dog = new Animal("dog");
        String encodedData = Base64.getEncoder().encodeToString(JsonMapper.createDefault().writeValueAsString(dog).getBytes());
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-async-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"));
        HttpResponse<?> response = pushClient.toBlocking().exchange(HttpRequest.POST("/push", request));

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    void testAsyncNack() throws IOException {
        when(messageProcessor.handleAnimalMessage(any())).thenReturn(Mono.just(Boolean.FALSE));

        Animal cat = new Animal("cat");
        String encodedData = Base64.getEncoder().encodeToString(JsonMapper.createDefault().writeValueAsString(cat).getBytes());
        PushRequest request = new PushRequest("projects/test-project/subscriptions/animals-async-push", new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"));

        try {
            pushClient.toBlocking().exchange(HttpRequest.POST("/push", request));
            Assertions.fail();
        } catch (HttpClientResponseException ex) {
            Assertions.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getResponse().getStatus());
        }
    }

    @MockBean(MessageProcessor.class)
    MessageProcessor mockMessageProcessor() {
        return mock(MessageProcessor.class);
    }

    @Singleton
    @Requires(property = "spec.name", value = "AcknowledgementPushSubscriberTest")
    static class SubscriberCreatedListener implements BeanCreatedEventListener<AcknowledgementPushSubscriber> {
        @Override
        public AcknowledgementPushSubscriber onCreated(@NonNull BeanCreatedEvent<AcknowledgementPushSubscriber> event) {
            return spy(event.getBean());
        }
    }
}
