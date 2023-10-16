package io.micronaut.gcp.pubsub.subscriber

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.bind.DefaultPubSubAcknowledgement
import io.micronaut.gcp.pubsub.support.Animal
import io.micronaut.messaging.Acknowledgement
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@MicronautTest
@Property(name = "spec.name", value = "AcknowledgementSubscriberSpec")
class AcknowledgementSubscriberSpec extends Specification {

    @Inject
    TestPublisher publisher

    MessageProcessor messageProcessor

    @Inject
    AcknowledgementSubscriber subscriber

    Object message

    Acknowledgement acknowledgement

    def setup() {
        message = null
        acknowledgement = null
    }

    void "blocking subscriber with manual ack"() {
        setup:
        def conditions = new PollingConditions(initialDelay: 1)
        Animal dog = new Animal("dog")
        messageProcessor.handleAnimalMessage(_ as Animal) >> Mono.just(Boolean.TRUE)

        when:
        publisher.publishAnimal(dog)

        then:
        conditions.eventually {
            assert message != null
            assert message instanceof Animal
            assert (message as Animal).name == "dog"
            assert acknowledgement instanceof DefaultPubSubAcknowledgement
            assert (acknowledgement as DefaultPubSubAcknowledgement).isClientAck()
        }
    }

    void "blocking subscriber with manual nack"() {
        setup:
        def conditions = new PollingConditions(initialDelay: 1)
        Animal dog = new Animal("dog")
        messageProcessor.handleAnimalMessage(_ as Animal) >>> [Mono.just(Boolean.FALSE), Mono.just(Boolean.TRUE)]

        when:
        publisher.publishAnimal(dog)

        then:
        conditions.eventually {
            assert message != null
            assert message instanceof Animal
            assert (message as Animal).name == "dog"
            assert acknowledgement instanceof DefaultPubSubAcknowledgement
            assert (acknowledgement as DefaultPubSubAcknowledgement).isClientAck()
        }
    }

    void "async subscriber with manual ack"() {
        setup:
        def conditions = new PollingConditions(initialDelay: 1)
        Animal dog = new Animal("dog")
        messageProcessor.handleAnimalMessage(_ as Animal) >> Mono.just(Boolean.TRUE)

        when:
        publisher.publishAnimalAsync(dog)

        then:
        conditions.eventually {
            assert message != null
            assert message instanceof Mono<Animal>
            assert acknowledgement instanceof DefaultPubSubAcknowledgement
            assert (acknowledgement as DefaultPubSubAcknowledgement).isClientAck()
        }
    }

    void "async subscriber with manual nack"() {
        setup:
        def conditions = new PollingConditions(initialDelay: 1)
        Animal dog = new Animal("dog")
        messageProcessor.handleAnimalMessage(_ as Animal) >>> [Mono.just(Boolean.FALSE), Mono.just(Boolean.TRUE)]

        when:
        publisher.publishAnimalAsync(dog)

        then:
        conditions.eventually {
            assert message != null
            assert message instanceof Mono<Animal>
            assert acknowledgement instanceof DefaultPubSubAcknowledgement
            assert (acknowledgement as DefaultPubSubAcknowledgement).isClientAck()
        }
    }

    @MockBean(AcknowledgementSubscriber)
    AcknowledgementSubscriber subscriberForTest() {
        messageProcessor = Mock(MessageProcessor)
        return new TestAcknowledgementSubscriber(messageProcessor)
    }

    class TestAcknowledgementSubscriber extends AcknowledgementSubscriber {

        TestAcknowledgementSubscriber(MessageProcessor messageProcessor) {
            super(messageProcessor)
        }

        @Override
        void onMessage(Animal animal, Acknowledgement ack) {
            message = animal
            acknowledgement = ack
            super.onMessage(animal, ack)
        }

        @Override
        Mono<Boolean> onReactiveMessage(Mono<Animal> animal, Acknowledgement ack) {
            message = animal
            acknowledgement = ack
            return super.onReactiveMessage(animal, ack)
        }
    }

    @Singleton
    @PubSubClient
    @Requires(property = "spec.name", value = "AcknowledgementSubscriberSpec")
    static interface TestPublisher {
        @Topic("animals") void publishAnimal(Animal animal);
        @Topic("animals-async") void publishAnimalAsync(Animal animal);
    }
}
