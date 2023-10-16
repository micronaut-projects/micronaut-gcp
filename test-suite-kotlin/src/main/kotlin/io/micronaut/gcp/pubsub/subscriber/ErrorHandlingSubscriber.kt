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
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverException
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverExceptionHandler
import io.micronaut.gcp.pubsub.support.Animal
// end::imports[]


@Requires(notEnv = ["test"])
// tag::clazz[]
@PubSubListener
class ErrorHandlingSubscriber : PubSubMessageReceiverExceptionHandler { // <1>

	fun onMessage(animal: Animal) {
		throw RuntimeException("error")
	}

	override fun handle(exception: PubSubMessageReceiverException) { // <2>

		val listener = exception.listener // <3>
		val state = exception.state // <4>
		val originalMessage = state.pubsubMessage
		val contentType = state.contentType
		//some logic
		state.ackReplyConsumer.ack() // <5>

	}
}
// tag::clazz[]
