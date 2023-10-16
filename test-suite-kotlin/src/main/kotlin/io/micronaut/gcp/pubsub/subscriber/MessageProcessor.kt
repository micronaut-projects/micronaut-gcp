package io.micronaut.gcp.pubsub.subscriber

import com.google.pubsub.v1.PubsubMessage
import io.micronaut.gcp.pubsub.support.Animal
import io.micronaut.messaging.Acknowledgement
import reactor.core.publisher.Mono

open class MessageProcessor {

    open fun handleByteArrayMessage(message: ByteArray) = Mono.just(java.lang.Boolean.TRUE)

    open fun handlePubsubMessage(pubsubMessage: PubsubMessage) = Mono.just(java.lang.Boolean.TRUE)

    open fun handleAnimalMessage(message: Animal) = Mono.just(java.lang.Boolean.TRUE)

    open fun recordAcknowledgement(acknowledgement: Acknowledgement) {}
}
