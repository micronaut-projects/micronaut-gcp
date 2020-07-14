package io.micronaut.gcp.pubsub.bind

import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.gcp.pubsub.AbstractConsumerSpec
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.Subscription
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.test.annotation.MicronautTest
import spock.util.concurrent.PollingConditions
import javax.inject.Inject

@MicronautTest
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
            receiver.dataHolder == "foo".getBytes()
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
            receiver.dataHolder == message
        }
    }
}

@PubSubClient
interface SimplePubSubClient {
    @Topic("test-topic")
    String publish(byte[] data)

    @Topic("test-pubsubmessage")
    String publish(PubsubMessage message)


}

@PubSubListener
class SimpleReceiver {
    public Object dataHolder;

    @Subscription("test-topic")
    void receive(byte[] data){
        this.dataHolder = data
    }

    @Subscription("test-pubsubmessage")
    void receive(PubsubMessage message){
        this.dataHolder = message
    }
}