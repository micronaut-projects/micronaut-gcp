package io.micronaut.gcp.pubsub.subscriber;

import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.gcp.pubsub.annotation.PubSubClient;
import io.micronaut.gcp.pubsub.annotation.Topic;
import io.micronaut.gcp.pubsub.bind.DefaultPubSubAcknowledgement;
import io.micronaut.gcp.pubsub.support.Animal;
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

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@MicronautTest
@Property(name = "spec.name", value = "AcknowledgementSubscriberTest")
class AcknowledgementSubscriberTest {

    @Inject
    TestPublisher publisher;

    @Inject
    MessageProcessor messageProcessor;

    @Inject
    AcknowledgementSubscriber subscriber;

    @BeforeEach
    void setup() {
        Mockito.reset(messageProcessor, subscriber);
    }

    @Test
    void testBlockingAck() {
        when(messageProcessor.handleAnimalMessage(any())).thenReturn(Mono.just(Boolean.TRUE));

        Animal dog = new Animal("dog");
        publisher.publishAnimal(dog);

        ArgumentCaptor<Animal> animalArg = ArgumentCaptor.forClass(Animal.class);
        ArgumentCaptor<Acknowledgement> ackArg = ArgumentCaptor.forClass(Acknowledgement.class);

        verify(subscriber, timeout(5000)).onMessage(animalArg.capture(), ackArg.capture());
        Assertions.assertEquals("dog", animalArg.getValue().getName());
        Assertions.assertInstanceOf(DefaultPubSubAcknowledgement.class, ackArg.getValue());
        Assertions.assertTrue(((DefaultPubSubAcknowledgement)ackArg.getValue()).isClientAck());
    }

    @Test
    void testBlockingNack() {
        when(messageProcessor.handleAnimalMessage(any())).thenReturn(Mono.just(Boolean.FALSE), Mono.just(Boolean.TRUE));

        Animal cat = new Animal("cat");
        publisher.publishAnimal(cat);

        ArgumentCaptor<Animal> animalArg = ArgumentCaptor.forClass(Animal.class);
        ArgumentCaptor<Acknowledgement> ackArg = ArgumentCaptor.forClass(Acknowledgement.class);

        verify(subscriber, timeout(5000).times(2)).onMessage(animalArg.capture(), ackArg.capture());
        Assertions.assertEquals("cat", animalArg.getValue().getName());
        Assertions.assertInstanceOf(DefaultPubSubAcknowledgement.class, ackArg.getValue());
        Assertions.assertTrue(((DefaultPubSubAcknowledgement)ackArg.getValue()).isClientAck());
    }

    @Test
    void testAsyncAck() {
        when(messageProcessor.handleAnimalMessage(any())).thenReturn(Mono.just(Boolean.TRUE));

        Animal dog = new Animal("dog");
        publisher.publishAnimalAsync(dog);

        ArgumentCaptor<Mono<Animal>> animalArg = ArgumentCaptor.forClass(Mono.class);
        ArgumentCaptor<Acknowledgement> ackArg = ArgumentCaptor.forClass(Acknowledgement.class);

        verify(subscriber, timeout(3000)).onReactiveMessage(animalArg.capture(), ackArg.capture());
        Assertions.assertNotNull(animalArg.getValue());
        Assertions.assertInstanceOf(DefaultPubSubAcknowledgement.class, ackArg.getValue());
        await().atMost(Duration.ofSeconds(3)).until(() -> ((DefaultPubSubAcknowledgement)ackArg.getValue()).isClientAck());
    }

    @Test
    void testAsyncNack() {
        when(messageProcessor.handleAnimalMessage(any())).thenReturn(Mono.just(Boolean.FALSE), Mono.just(Boolean.TRUE));

        Animal cat = new Animal("cat");
        publisher.publishAnimalAsync(cat);

        ArgumentCaptor<Mono<Animal>> animalArg = ArgumentCaptor.forClass(Mono.class);
        ArgumentCaptor<Acknowledgement> ackArg = ArgumentCaptor.forClass(Acknowledgement.class);

        verify(subscriber, timeout(5000).times(2)).onReactiveMessage(animalArg.capture(), ackArg.capture());
        Assertions.assertNotNull(animalArg.getValue());
        Assertions.assertInstanceOf(DefaultPubSubAcknowledgement.class, ackArg.getValue());
        await().atMost(Duration.ofSeconds(3)).until(() -> ((DefaultPubSubAcknowledgement)ackArg.getValue()).isClientAck());
    }

    @MockBean(MessageProcessor.class)
    MessageProcessor mockMessageProcessor() {
        return mock(MessageProcessor.class);
    }

    @Singleton
    @PubSubClient
    @Requires(property = "spec.name", value = "AcknowledgementSubscriberTest")
    interface TestPublisher {
        @Topic("animals") void publishAnimal(Animal animal);
        @Topic("animals-async") void publishAnimalAsync(Animal animal);
    }

    @Singleton
    @Requires(property = "spec.name", value = "AcknowledgementSubscriberTest")
    static class SubscriberCreatedListener implements BeanCreatedEventListener<AcknowledgementSubscriber> {
        @Override
        public AcknowledgementSubscriber onCreated(@NonNull BeanCreatedEvent<AcknowledgementSubscriber> event) {
            return spy(event.getBean());
        }
    }
}
