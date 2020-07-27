package io.micronaut.gcp.pubsub.client;

//tag imports[]
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.support.Animal

// end::imports[]

// tag::clazz[]
@PubSubClient // <1>
interface SimpleClient {

    @Topic("animals")
    void send(PubsubMessage message) // <2>

    @Topic("animals")
    void send(byte[] data) // <3>

    @Topic("animals")
    void send(Animal animal) // <4>
}
// end::clazz[]
