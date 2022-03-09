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
package io.micronaut.gcp.pubsub.publisher

//tag::imports[]
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.support.Animal
import io.micronaut.http.MediaType
// end::imports[]

// tag::clazz[]
@PubSubClient
interface CustomSerDesClient {

	@Topic("animals") // <1>
	fun send(pubsubMessage: PubsubMessage)

	@Topic("animals")  // <2>
	fun send(data: ByteArray)

	@Topic(value = "animals", contentType = MediaType.IMAGE_GIF) // <3>
	fun sendWithCustomType(data: ByteArray)

	@Topic("animals") // <4>
	fun send(animal: Animal)

	@Topic(value = "animals", contentType = MediaType.APPLICATION_XML) // <5>
	fun sendWithCustomType(animal: Animal)

}
// end::clazz[]