package io.micronaut.gcp.pubsub.quickstart;
//tag imports[]
import io.micronaut.gcp.pubsub.annotation.PubSubListener;
import io.micronaut.gcp.pubsub.annotation.Subscription;
// end::imports[]

// tag::clazz[]
@PubSubListener // <1>
public class AnimalListener {

    @Subscription("animals") // <2>
    public void onMessage(byte[] data) { // <3>
        System.out.println("Message received");
    }

}
// end::clazz[]
