/*
 * Copyright 2017-2020 original authors
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
import io.micronaut.gcp.pubsub.annotation.Subscription
import io.micronaut.gcp.pubsub.support.Animal
// end::imports[]

@Requires(property = "spec.name", value = "ContentTypeSubscriberSpec")
// tag::clazz[]
@PubSubListener
class ContentTypeSubscriber(private val messageProcessor: MessageProcessor)  {
	@Subscription("raw-subscription")
	fun	receiveRaw(data: ByteArray, @MessageId id: String) { // <1>
        messageProcessor.handleByteArrayMessage(data).block()
	}

	@Subscription("native-subscription")
	fun  receiveNative(message: PubsubMessage) { // <2>
        messageProcessor.handlePubsubMessage(message).block()
	}

	@Subscription("animals")
	fun receivePojo(animal: Animal, @MessageId id: String) { // <3>
        messageProcessor.handleAnimalMessage(animal).block()
	}

	@Subscription(value = "animals-legacy", contentType = "application/xml")
	fun  receiveXML(animal: Animal, @MessageId id: String) { // <4>
        messageProcessor.handleAnimalMessage(animal).block()
	}
}
// end::clazz[]
