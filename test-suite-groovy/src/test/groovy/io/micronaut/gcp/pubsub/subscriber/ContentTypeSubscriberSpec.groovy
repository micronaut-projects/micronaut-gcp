package io.micronaut.gcp.pubsub.subscriber

import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.support.Animal
import io.micronaut.http.MediaType
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.nio.charset.StandardCharsets

@MicronautTest
@Property(name = "spec.name", value = "ContentTypeSubscriberSpec")
class ContentTypeSubscriberSpec extends Specification {

    @Inject
    TestPublisher publisher

    @Inject
    ContentTypeSubscriber subscriber

    Object receivedMessage

    def setup() {
        receivedMessage = null
    }

    void "receive raw bytes"() {
        given:
        def conditions = new PollingConditions(initialDelay: 1)
        byte[] bytesSent = "foo".getBytes(StandardCharsets.UTF_8)

        when:
        publisher.publishRaw(bytesSent)

        then:
        conditions.eventually {
            assert receivedMessage != null
            assert receivedMessage instanceof byte[]
            String decodedMessage = new String((receivedMessage as byte[]), StandardCharsets.UTF_8)
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
            assert receivedMessage != null
            assert receivedMessage instanceof PubsubMessage
            String decodedMessage = (receivedMessage as PubsubMessage).getData().toString(StandardCharsets.UTF_8)
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
            assert receivedMessage != null
            assert receivedMessage instanceof Animal
            assert "dog" == (receivedMessage as Animal).getName()
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
            assert receivedMessage != null
            assert receivedMessage instanceof Animal
            assert "dog" == (receivedMessage as Animal).getName()
        }
    }

    @MockBean(ContentTypeSubscriber)
    ContentTypeSubscriber testSubscriber() {
        return new TestContentTypeSubscriber()
    }

    class TestContentTypeSubscriber extends ContentTypeSubscriber {
        @Override
        void receiveRaw(byte[] data, String id) {
            receivedMessage = data
            super.receiveRaw(data, id)
        }

        @Override
        void receiveNative(PubsubMessage pubsubMessage) {
            receivedMessage = pubsubMessage
            super.receiveNative(pubsubMessage)
        }

        @Override
        void receivePojo(Animal animal, String id) {
            receivedMessage = animal
            super.receivePojo(animal, id)
        }

        @Override
        void receiveXML(Animal animal, String id) {
            receivedMessage = animal
            super.receiveXML(animal, id)
        }
    }

    @Singleton
    @PubSubClient
    @Requires(property = "spec.name", value = "ContentTypeSubscriberSpec")
    static interface TestPublisher {
        @Topic("raw-subscription") void publishRaw(byte[] payload)
        @Topic("native-subscription") void publishNative(byte[] payload)
        @Topic("animals") void publishAnimal(Animal animal)
        @Topic(value = "animals-legacy", contentType = MediaType.APPLICATION_XML) void publishAnimalAsXml(Animal animal)
    }
}
