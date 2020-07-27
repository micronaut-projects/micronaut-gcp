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
package io.micronaut.gcp.pubsub.client

//tag imports[]
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.support.Animal
// end::imports[]

// tag::clazz[]
@PubSubClient // <1>
interface SimpleClient {

	@Topic("animals")
	fun send(message: PubsubMessage) // <2>

	@Topic("animals")
	fun send(data: ByteArray) // <3>

	@Topic("animals")
	fun send(animal: Animal) // <4>
}
// end::clazz[]