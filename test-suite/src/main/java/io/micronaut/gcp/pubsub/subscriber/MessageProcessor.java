package io.micronaut.gcp.pubsub.subscriber;

import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.gcp.pubsub.support.Animal;
import reactor.core.publisher.Mono;

public interface MessageProcessor {
    default Mono<Boolean> handleByteArrayMessage(byte[] message) {
        return Mono.just(Boolean.TRUE);
    }

    default Mono<Boolean> handlePubSubMessage(PubsubMessage pubsubMessage) {
        return Mono.just(Boolean.TRUE);
    }

    default Mono<Boolean> handleAnimalMessage(Animal message) {
        return Mono.just(Boolean.TRUE);
    }
}
