package subscriber

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.bind.DefaultPubSubAcknowledgement
import io.micronaut.gcp.pubsub.subscriber.MessageProcessor
import io.micronaut.gcp.pubsub.support.Animal
import io.micronaut.messaging.Acknowledgement
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jetbrains.annotations.NotNull
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.atomic.AtomicInteger

@MicronautTest
@Property(name = "spec.name", value = "AcknowledgementSubscriberSpec")
class AcknowledgementSubscriberSpec extends Specification {

    @Inject
    TestPublisher publisher

    Animal receivedMessage

    Acknowledgement recordedAcknowledgement

    AtomicInteger messageCount

    boolean isNackTest

    def setup() {
        receivedMessage = null
        recordedAcknowledgement = null
        messageCount = new AtomicInteger(0)
        isNackTest = false
    }

    void "blocking subscriber with manual ack"() {
        setup:
        def conditions = new PollingConditions(initialDelay: 1)
        Animal dog = new Animal("dog")

        when:
        publisher.publishAnimal(dog)

        then:
        conditions.eventually {
            assert receivedMessage != null
            assert receivedMessage.name == "dog"
            assert messageCount.get() == 1
            assert recordedAcknowledgement instanceof DefaultPubSubAcknowledgement
            assert (recordedAcknowledgement as DefaultPubSubAcknowledgement).isClientAck()
        }
    }

    void "blocking subscriber with manual nack"() {
        setup:
        def conditions = new PollingConditions(initialDelay: 1)
        Animal dog = new Animal("dog")
        isNackTest = true

        when:
        publisher.publishAnimal(dog)

        then:
        conditions.eventually {
            assert receivedMessage != null
            assert receivedMessage.name == "dog"
            assert messageCount.get() == 2
            assert recordedAcknowledgement instanceof DefaultPubSubAcknowledgement
            assert (recordedAcknowledgement as DefaultPubSubAcknowledgement).isClientAck()
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
            assert receivedMessage != null
            assert receivedMessage.name == "dog"
            assert messageCount.get() == 1
            assert recordedAcknowledgement instanceof DefaultPubSubAcknowledgement
            assert (recordedAcknowledgement as DefaultPubSubAcknowledgement).isClientAck()
        }
    }

    void "async subscriber with manual nack"() {
        setup:
        def conditions = new PollingConditions(initialDelay: 1)
        Animal dog = new Animal("dog")
        isNackTest = true

        when:
        publisher.publishAnimalAsync(dog)

        then:
        conditions.eventually {
            assert receivedMessage != null
            assert receivedMessage.name == "dog"
            assert messageCount.get() == 2
            assert recordedAcknowledgement instanceof DefaultPubSubAcknowledgement
            assert (recordedAcknowledgement as DefaultPubSubAcknowledgement).isClientAck()
        }
    }

    @MockBean(MessageProcessor)
    MessageProcessor subscriberForTest() {
        return new MessageProcessor() {
            @Override
            Mono<Boolean> handleAnimalMessage(@NotNull Animal message) {
                receivedMessage = message
                if (messageCount.getAndIncrement() == 0 && isNackTest) {
                    return Mono.just(Boolean.FALSE)
                }
                return Mono.just(Boolean.TRUE)
            }

            @Override
            void recordAcknowledgement(@NotNull Acknowledgement acknowledgement) {
                recordedAcknowledgement = acknowledgement
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
