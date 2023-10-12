package io.micronaut.gcp.pubsub.subscriber

import com.google.pubsub.v1.PubsubMessage
import io.micronaut.gcp.pubsub.support.Animal
import reactor.core.publisher.Mono

class MessageProcessor {

    fun handleByteArrayMessage(message: ByteArray) = Mono.just(java.lang.Boolean.TRUE)

    fun handlePubsubMessage(pubsubMessage: PubsubMessage) = Mono.just(java.lang.Boolean.TRUE)

    fun handleAnimalMessage(message: Animal) = Mono.just(java.lang.Boolean.TRUE)
}
