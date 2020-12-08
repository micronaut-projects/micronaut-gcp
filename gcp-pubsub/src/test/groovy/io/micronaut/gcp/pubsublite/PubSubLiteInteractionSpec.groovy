package io.micronaut.gcp.pubsublite

import com.google.api.core.SettableApiFuture
import com.google.cloud.pubsublite.cloudpubsub.Publisher
import com.google.cloud.pubsublite.cloudpubsub.Subscriber
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.MockPubSubEngine
import io.micronaut.gcp.pubsub.annotation.MessageId
import io.micronaut.gcp.pubsub.exception.PubSubClientException
import io.micronaut.gcp.pubsublite.annotation.LiteSubscription
import io.micronaut.gcp.pubsublite.annotation.LiteTopic
import io.micronaut.gcp.pubsublite.annotation.PubSubLiteClient
import io.micronaut.gcp.pubsublite.annotation.PubSubLiteListener
import io.micronaut.gcp.pubsublite.support.*
import io.micronaut.http.annotation.Body
import io.micronaut.messaging.Acknowledgement
import io.micronaut.messaging.annotation.Header
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import javax.inject.Inject

@MicronautTest
@Property(name = "spec.name", value = "PubSubLiteInteractionSpec")
@Property(name = "gcp.projectId", value = "test-project")
class PubSubLiteInteractionSpec extends Specification {
    @Inject
    LiteSubscriberFactory liteSubscriberFactory;

    @Inject
    PubSubLiteInteractionSpecClient testClient

    @Inject
    PubSubLiteInteractionSpecListener testListener

    @Inject
    MockPubSubEngine pubSubEngine

    def "test interaction with client / listener"() {
        PollingConditions conditions = new PollingConditions(timeout: 3)

        when:
        testClient.publish("foo".getBytes())

        then:
        conditions.eventually {
            testListener.dataHolder["test-topic"] == "foo".getBytes()
        }
    }

    def "test interaction with headers"() {
        PollingConditions conditions = new PollingConditions(timeout: 3)
        def person = new PubSubLiteInteractionSpecPerson()
        person.name = "alf"

        when:
        testClient.publishPojoWithHeaders(person, 42)

        then:
        conditions.eventually {
            def map = (Map<String,Object>)testListener.dataHolder["test-headers"]
            verifyAll {
                map != null
                map.get("header") == 42
                ((PubSubLiteInteractionSpecPerson) map.get("body")).getName() == "alf"
            }
        }
    }

    def "test interaction with message id"() {
        PollingConditions conditions = new PollingConditions(timeout: 3)
        def person = new PubSubLiteInteractionSpecPerson()
        person.name = "alf"

        when:
        testClient.publishPojoMessageId(person)

        then:
        conditions.eventually {
            def map = (Map<String,Object>)testListener.dataHolder["test-with-message-id"]
            verifyAll {
                map != null
                map.get("id") == "1234"
                ((PubSubLiteInteractionSpecPerson) map.get("body")).getName() == "alf"
            }
        }
    }

    def "test interaction without content type"() {
        def person = new PubSubLiteInteractionSpecPerson()
        person.name = "alf"

        when:
        testClient.publishDataWithoutContentType(person)

        then:
        thrown(PubSubClientException)
    }

    def "test interaction with manual ack"() {
        PollingConditions conditions = new PollingConditions(timeout: 3)
        def person = new PubSubLiteInteractionSpecPerson()
        person.name = "alf"

        when:
        testClient.publishPojoForManualAck(person)

        then:
        conditions.eventually {
            def result = (PubSubLiteInteractionSpecPerson)testListener.dataHolder["test-with-manual-ack"]
            verifyAll {
                result != null
                result.getName() == "alf"
            }
        }
    }

    @Primary
    @MockBean(DefaultLitePublisherFactory)
    LitePublisherFactory litePublisherFactory() {
        def factory = Mock(LitePublisherFactory)
        def publisher = Mock(PubSubLiteInteractionSpecMockLitePublisher)
        def future = new SettableApiFuture<String>()
        future.set("1234")
        factory.createLitePublisher(_) >> { LitePublisherFactoryConfig config ->
            publisher.getTopicNameString() >> config.topicState.topicPath.name().value()
            publisher.publish(_ as PubsubMessage) >> { PubsubMessage message -> pubSubEngine.publish(message, publisher.getTopicNameString()); return future }
            return publisher
        }
        return factory
    }

    @Primary
    @MockBean(DefaultLiteSubscriberFactory)
    LiteSubscriberFactory liteSubscriberFactory(MockPubSubEngine mockPubSubEngine) {
        def factory = Mock(LiteSubscriberFactory)
        def subscriber = Mock(Subscriber)
        factory.createSubscriber(_ as LiteSubscriberFactoryConfig)  >> { LiteSubscriberFactoryConfig config ->
            pubSubEngine.registerReceiver(config.receiver, config.getSubscriptionPath().name().value())
            return subscriber
        }
        return factory
    }
}

interface PubSubLiteInteractionSpecMockLitePublisher extends Publisher {
    String getTopicNameString()
}

@PubSubLiteClient
@Requires(property = "spec.name", value = "PubSubLiteInteractionSpec")
interface PubSubLiteInteractionSpecClient {
    @LiteTopic(name = "test-topic")
    String publish(byte[] data)

    @LiteTopic(name ="test-headers")
    String publishPojoWithHeaders(PubSubLiteInteractionSpecPerson person, @Header("X-Answer-For-Everything") Integer answer)

    @LiteTopic(name ="test-with-message-id")
    String publishPojoMessageId(PubSubLiteInteractionSpecPerson person)

    @LiteTopic(name = "test-without-content-type", contentType = "")
    String publishDataWithoutContentType(PubSubLiteInteractionSpecPerson person)

    @LiteTopic(name = "test-with-manual-ack")
    String publishPojoForManualAck(PubSubLiteInteractionSpecPerson person);
}

@PubSubLiteListener
@Requires(property = "spec.name", value = "PubSubLiteInteractionSpec")
class PubSubLiteInteractionSpecListener {
    Map<String, Object> dataHolder = new HashMap<>()

    @LiteSubscription(name = "test-topic")
    void receive(byte[] message) {
        dataHolder["test-topic"] = message
    }

    @LiteSubscription(name = "test-with-message-id")
    void receiveWithMessageId(PubSubLiteInteractionSpecPerson person, @MessageId String id){
        Map<String, Object> holder = new HashMap<>()
        holder.put("body", person)
        holder.put("id", id)
        dataHolder["test-with-message-id"] = holder
    }

    @LiteSubscription(name = "test-headers")
    void receiveWithHeaders(PubSubLiteInteractionSpecPerson person, @Header("X-Answer-For-Everything") Integer answer) {
        Map<String, Object> holder = new HashMap<>()
        holder.put("body", person)
        holder.put("header", answer)
        dataHolder["test-headers"] = holder
    }

    @LiteSubscription(name = "test-with-manual-ack")
    void receiveManualAck(@Body PubSubLiteInteractionSpecPerson person, Acknowledgement ack) {
        dataHolder["test-with-manual-ack"] = person
        ack.ack()
    }
}

class PubSubLiteInteractionSpecPerson {
    String name;
}

