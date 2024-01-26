/*
 * Copyright 2017-2024 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.gcp.pubsub.subscriber
//tag::imports[]
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.annotation.MessageId
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.PushSubscription
import io.micronaut.gcp.pubsub.support.Animal
import reactor.core.publisher.Mono

// end::imports[]

@Requires(property = "spec.name", value = "ReactivePushSubscriberSpec")
// tag::clazz[]
@PubSubListener
class ReactivePushSubscriber(private val messageProcessor: MessageProcessor) {

    @PushSubscription("raw-push-subscription")
    fun receiveRaw(data: Mono<ByteArray>, @MessageId id: String): Mono<Any> { // <1>
        return data.flatMap { payload ->
            messageProcessor.handleByteArrayMessage(payload)
        }
    }

    @PushSubscription("native-push-subscription")
    fun receiveNative(message: Mono<PubsubMessage>): Mono<Any> { // <2>
        return message.flatMap { payload ->
            messageProcessor.handlePubsubMessage(payload)
        }
    }

    @PushSubscription("animals-push")
    fun receivePojo(message: Mono<Animal>, @MessageId id: String): Mono<Any> { // <3>
        return message.flatMap { animal ->
            messageProcessor.handleAnimalMessage(animal)
        }
    }

    @PushSubscription(value = "animals-legacy-push", contentType = "application/xml")
    fun receiveXML(message: Mono<Animal>, @MessageId id: String): Mono<Any> { // <4>
        return message.flatMap { animal ->
            messageProcessor.handleAnimalMessage(animal)
        }
    }
}
// end::clazz[]

