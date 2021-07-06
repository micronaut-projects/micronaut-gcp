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
// tag::imports[]
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.support.Animal
import reactor.core.publisher.Mono

// end::imports[]

// tag::clazz[]
@PubSubClient
interface CustomReturnClient {

	@Topic("animals")
	fun send(animal: Animal) // <1>

	@Topic("animals")
	fun sendWithId(animal: Animal): String // <2>

	@Topic("animals")
	fun sendReactive(animal: Animal?): Mono<String> //<3>

}