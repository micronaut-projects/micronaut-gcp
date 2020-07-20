package io.micronaut.gcp.pubsub.client;
// tag::imports[]
import io.micronaut.gcp.pubsub.annotation.PubSubClient;
import io.micronaut.gcp.pubsub.annotation.Topic;
import io.micronaut.gcp.pubsub.support.Animal;
import io.micronaut.messaging.annotation.Header;
// end::imports[]

// tag::clazz[]
@PubSubClient // <1>
public interface AnimalClient {

    @Topic("animals")
    void send(@Header String animalType, Animal animal); // <2>

    default void send(Animal animal) { // <3>
        send(animal.getClass().getSimpleName(), animal);
    }

}
// end::clazz[]