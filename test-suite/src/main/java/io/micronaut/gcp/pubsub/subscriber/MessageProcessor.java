package io.micronaut.gcp.pubsub.subscriber;

import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.gcp.pubsub.support.Animal;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

@Singleton
public class MessageProcessor {
    public Mono<Boolean> handleByteArrayMessage(byte[] message) {
        return Mono.just(Boolean.TRUE);
    }

    public Mono<Boolean> handlePubSubMessage(PubsubMessage pubsubMessage) {
        return Mono.just(Boolean.TRUE);
    }

    public Mono<Boolean> handleAnimalMessage(Animal message) {
        return Mono.just(Boolean.TRUE);
    }
}
