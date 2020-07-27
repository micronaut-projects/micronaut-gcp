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