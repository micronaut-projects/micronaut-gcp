package io.micronaut.gcp.pubsub.client

//tag imports[]
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.support.Animal
// end::imports[]

// tag::clazz[]
@PubSubClient
interface MultipleProjectClient {

	@Topic("animals")
	fun sendUS(animal: Animal) // <1>

	@Topic("projects/eu-project/topics/animals")
	fun sendEU(animal: Animal) // <2>

}
// end::clazz[]