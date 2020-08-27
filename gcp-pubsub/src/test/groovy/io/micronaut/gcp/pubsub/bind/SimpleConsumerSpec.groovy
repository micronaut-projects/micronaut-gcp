package io.micronaut.gcp.pubsub.bind

import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.AbstractConsumerSpec
import io.micronaut.gcp.pubsub.annotation.MessageId
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.Subscription
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.support.Person
import io.micronaut.messaging.annotation.Header
import io.micronaut.test.annotation.MicronautTest
import spock.util.concurrent.PollingConditions
import javax.inject.Inject

@MicronautTest
@Property(name = "spec.name", value = "SimpleConsumerSpec")
@Property(name = "gcp.projectId", value = "test-project")
class SimpleConsumerSpec extends AbstractConsumerSpec {

    @Inject
    SimplePubSubClient pubSubClient

    @Inject
    SimpleReceiver receiver

    void "simple consumer spec"() {
        PollingConditions conditions = new PollingConditions(timeout: 3)
        when:
            pubSubClient.publish("foo".getBytes())
        then:
            conditions.eventually {
                receiver.dataHolder["test-topic"] == "foo".getBytes()
            }
    }

    void "send and receive PubSubMessage"() {
        PollingConditions conditions = new PollingConditions(timeout: 3)
        def message = PubsubMessage
                .newBuilder()
                .setData(ByteString.copyFrom("foo".getBytes()))
                .build()
        when:
            pubSubClient.publish(message)
        then:
            conditions.eventually {
                receiver.dataHolder["test-pubsubmessage"].data == message.data
            }
    }

    void "send and receive with headers"() {
        PollingConditions conditions = new PollingConditions(timeout: 3)
        def person = new Person()
        person.name = "alf"
        when:
            pubSubClient.publishPojo(person, 42)
        then:
            conditions.eventually {
                def map = (Map<String,Object>)receiver.dataHolder["test-headers"]
                map != null
            }
    }

    void "receive messageId"() {
        PollingConditions conditions = new PollingConditions(timeout: 3)
        def person = new Person()
        person.name = "alf"
        when:
            pubSubClient.publishPojoMessageId(person)
        then:
            conditions.eventually {
                def map = (Map<String,Object>)receiver.dataHolder["test-with-message-id"]
                map != null && map.get("id") == "1234"
            }
    }
}

@PubSubClient
@Requires(property = "spec.name", value = "SimpleConsumerSpec")
interface SimplePubSubClient {
    @Topic("test-topic")
    String publish(byte[] data)

    @Topic("test-pubsubmessage")
    String publish(PubsubMessage message)

    @Topic("test-headers")
    String publishPojo(Person person, @Header("X-Answer-For-Everything") Integer answer)

    @Topic("test-with-message-id")
    String publishPojoMessageId(Person person)

}

@PubSubListener
@Requires(property = "spec.name", value = "SimpleConsumerSpec")
class SimpleReceiver {
    public Map<String, Object> dataHolder = new HashMap<>()

    @Subscription("test-topic")
    void receive(byte[] data){
        this.dataHolder["test-topic"] = data
    }

    @Subscription("test-pubsubmessage")
    void receive(PubsubMessage message){
        this.dataHolder["test-pubsubmessage"] = message
    }

    @Subscription("test-with-message-id")
    void receive(Person person, @MessageId String id){
        Map<String, Object> holder = new HashMap<>()
        holder.put("body", person)
        holder.put("id", id)
        dataHolder["test-with-message-id"] = holder
    }

    @Subscription("test-headers")
    void receiveWithHeaders(Person person, @Header("X-Answer-For-Everything") Integer answer) {
        Map<String, Object> holder = new HashMap<>()
        holder.put("body", person)
        holder.put("header", answer)
        dataHolder["test-headers"] = holder
    }
}