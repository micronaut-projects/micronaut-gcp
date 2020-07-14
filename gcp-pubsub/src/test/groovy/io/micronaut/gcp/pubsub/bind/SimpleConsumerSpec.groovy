package io.micronaut.gcp.pubsub.bind

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
}

@PubSubClient
interface SimplePubSubClient {
    @Topic("test-topic")
    String publish(byte[] data)
}

@PubSubListener
class SimpleReceiver {
    public Object dataHolder;
    @Subscription("test-topic")
    void receive(byte[] data){
        this.dataHolder = data
    }
}