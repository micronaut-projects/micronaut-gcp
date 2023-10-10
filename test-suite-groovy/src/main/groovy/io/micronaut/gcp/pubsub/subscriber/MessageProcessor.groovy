package io.micronaut.gcp.pubsub.subscriber

import com.google.pubsub.v1.PubsubMessage
import io.micronaut.gcp.pubsub.support.Animal
import reactor.core.publisher.Mono

class MessageProcessor {
    Mono<Boolean> handleByteArrayMessage(byte[] message) {
        return Mono.just(Boolean.TRUE);
    }

    Mono<Boolean> handlePubSubMessage(PubsubMessage pubsubMessage) {
        return Mono.just(Boolean.TRUE);
    }

    Mono<Boolean> handleAnimalMessage(Animal message) {
        return Mono.just(Boolean.TRUE);
    }
}
