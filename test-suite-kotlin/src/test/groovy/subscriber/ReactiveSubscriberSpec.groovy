package subscriber

import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.subscriber.MessageProcessor
import io.micronaut.gcp.pubsub.support.Animal
import io.micronaut.http.MediaType
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import reactor.core.publisher.Mono
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.nio.charset.StandardCharsets

@MicronautTest
@Property(name = "spec.name", value = "ReactiveSubscriberSpec")
class ReactiveSubscriberSpec extends Specification {

    @Inject
    TestPublisher publisher

    Object unwrappedResult

    def setup() {
        unwrappedResult = null
    }

    void "receive raw bytes"() {
        given:
        def conditions = new PollingConditions(timeout: 1)
        byte[] bytesSent = "foo".getBytes(StandardCharsets.UTF_8)

        when:
        publisher.publishRaw(bytesSent)

        then:
        conditions.eventually {
            assert unwrappedResult != null
            assert unwrappedResult instanceof byte[]
            String decodedMessage = new String((unwrappedResult as byte[]), StandardCharsets.UTF_8)
            assert "foo" == decodedMessage
        }
    }

    void "receive native message"() {
        given:
        def conditions = new PollingConditions(initialDelay: 1)
        byte[] bytesSent = "foo".getBytes(StandardCharsets.UTF_8)

        when:
        publisher.publishNative(bytesSent)

        then:
        conditions.eventually {
            assert unwrappedResult != null
            assert unwrappedResult instanceof PubsubMessage
            String decodedMessage = (unwrappedResult as PubsubMessage).getData().toString(StandardCharsets.UTF_8)
            assert "foo" == decodedMessage
        }
    }

    void "receive pojo message from json"() {
        given:
        def conditions = new PollingConditions(initialDelay: 1)
        Animal dog = new Animal("dog")

        when:
        publisher.publishAnimal(dog)

        then:
        conditions.eventually {
            assert unwrappedResult != null
            assert unwrappedResult instanceof Animal
            assert "dog" == (unwrappedResult as Animal).getName()
        }
    }

    void "receive pojo message from xml"() {
        given:
        def conditions = new PollingConditions(initialDelay: 1)
        Animal dog = new Animal("dog")

        when:
        publisher.publishAnimalAsXml(dog)

        then:
        conditions.eventually {
            assert unwrappedResult != null
            assert unwrappedResult instanceof Animal
            assert "dog" == (unwrappedResult as Animal).getName()
        }
    }

    @MockBean(MessageProcessor.class)
    MessageProcessor mockMessageProcessor() {
        return new MessageProcessor() {
            @Override
            Mono<Boolean> handleByteArrayMessage(byte[] message) {
                unwrappedResult = message
                return super.handleByteArrayMessage(message)
            }

            @Override
            Mono<Boolean> handlePubsubMessage(PubsubMessage pubsubMessage) {
                unwrappedResult = pubsubMessage
                return super.handlePubsubMessage(pubsubMessage)
            }

            @Override
            Mono<Boolean> handleAnimalMessage(Animal message) {
                unwrappedResult = message
                return super.handleAnimalMessage(message)
            }
        }
    }

    @Singleton
    @PubSubClient
    @Requires(property = "spec.name", value = "ReactiveSubscriberSpec")
    static interface TestPublisher {
        @Topic("raw-subscription") void publishRaw(byte[] payload)
        @Topic("native-subscription") void publishNative(byte[] payload)
        @Topic("animals") void publishAnimal(Animal animal)
        @Topic(value = "animals-legacy", contentType = MediaType.APPLICATION_XML) void publishAnimalAsXml(Animal animal)
    }
}
