package io.micronaut.gcp.pubsub.subscriber

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.NonNull
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.support.Animal
import io.micronaut.pubsub.testcontainers.PubSubEmulator
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@MicronautTest
@Property(name = "spec.name", value = "AcknowledgementSubscriberSpec")
class AcknowledgementSubscriberSpec extends Specification implements TestPropertyProvider {
    @Override
    @NonNull Map<String, String> getProperties() {
        PubSubEmulator.getProperties();
    }

    @Inject
    TestPublisher publisher

    Object message
    boolean nack

    def setup() {
        message = null
        nack = false
    }

    void "blocking subscriber with manual ack"() {
        setup:
        def conditions = new PollingConditions(initialDelay: 1)
        Animal dog = new Animal("dog")

        when:
        publisher.publishAnimal(dog)

        then:
        conditions.eventually {
            assert message != null
            assert message instanceof Animal
            assert (message as Animal).name == "dog"
        }
    }

    void "blocking subscriber with manual nack"() {
        setup:
        def conditions = new PollingConditions(initialDelay: 1)
        Animal dog = new Animal("dog")
        nack = true

        when:
        publisher.publishAnimal(dog)

        then:
        conditions.eventually {
            assert message != null
            assert message instanceof Animal
            assert (message as Animal).name == "dog"
        }
    }

    void "async subscriber with manual ack"() {
        setup:
        def conditions = new PollingConditions(initialDelay: 1)
        Animal dog = new Animal("dog")

        when:
        publisher.publishAnimalAsync(dog)

        then:
        conditions.eventually {
            assert message != null
            assert message instanceof Animal
        }
    }

    void "async subscriber with manual nack"() {
        setup:
        def conditions = new PollingConditions(initialDelay: 1)
        Animal dog = new Animal("dog")
        nack = true

        when:
        publisher.publishAnimalAsync(dog)

        then:
        conditions.eventually {
            assert message != null
            assert message instanceof Animal
        }
    }

    @MockBean(MessageProcessor)
    MessageProcessor messageProcessor() {
        return new MessageProcessor() {
            @Override
            Mono<Boolean> handleAnimalMessage(Animal animal) {
                message = animal
                boolean result = !nack
                nack = false
                return Mono.just(result)
            }
        }
    }

    @Singleton
    @PubSubClient
    @Requires(property = "spec.name", value = "AcknowledgementSubscriberSpec")
    static interface TestPublisher {
        @Topic("animals") void publishAnimal(Animal animal)
        @Topic("animals-async") void publishAnimalAsync(Animal animal)
    }
}
