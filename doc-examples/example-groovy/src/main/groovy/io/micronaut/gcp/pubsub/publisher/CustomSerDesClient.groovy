package io.micronaut.gcp.pubsub.publisher;
//tag imports[]
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.gcp.pubsub.annotation.PubSubClient;
import io.micronaut.gcp.pubsub.annotation.Topic;
import io.micronaut.gcp.pubsub.support.Animal;
import io.micronaut.http.MediaType;
// end::imports[]

// tag::clazz[]
@PubSubClient
interface CustomSerDesClient {

    @Topic("animals") // <1>
    void send(PubsubMessage pubsubMessage)

    @Topic("animals") // <2>
    void send(byte[] data)

    @Topic(value = "animals", contentType = MediaType.IMAGE_GIF) // <3>
    void sendWithCustomType(byte[] data)

    @Topic("animals") // <4>
    void send(Animal animal)

    @Topic(value = "animals", contentType = MediaType.APPLICATION_XML) // <5>
    void sendWithCustomType(Animal animal)

}
// end::clazz[]
