/*
 * Copyright 2017-2023 original authors
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
import io.micronaut.gcp.pubsub.annotation.MessageId
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.Subscription
import io.micronaut.gcp.pubsub.support.Animal
import reactor.core.publisher.Mono

// end::imports[]

// tag::clazz[]
@PubSubListener
class ReactiveSubscriber(private val messageProcessor: MessageProcessor) {

    @Subscription("raw-subscription")
    fun receiveRaw(data: Mono<ByteArray>, @MessageId id: String): Mono<Any> { // <1>
        return data.flatMap { payload ->
            messageProcessor.handleByteArrayMessage(payload)
        }
    }

    @Subscription("native-subscription")
    fun receiveNative(message: Mono<PubsubMessage>): Mono<Any> { // <2>
        return message.flatMap { payload ->
            messageProcessor.handlePubsubMessage(payload)
        }
	}

	@Subscription("animals")
	fun receivePojo(message: Mono<Animal>, @MessageId id: String): Mono<Any> { // <3>
        return message.flatMap { animal ->
            messageProcessor.handleAnimalMessage(animal)
        }
	}

	@Subscription(value = "animals-legacy", contentType = "application/xml")
	fun  receiveXML(message: Mono<Animal>, @MessageId id: String): Mono<Any> { // <4>
        return message.flatMap { animal ->
            messageProcessor.handleAnimalMessage(animal)
        }
	}
}
// end::clazz[]

