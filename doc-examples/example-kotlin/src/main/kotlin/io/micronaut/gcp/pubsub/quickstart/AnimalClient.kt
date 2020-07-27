package io.micronaut.gcp.pubsub.quickstart

//tag imports[]
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
// end::imports[]

// tag::clazz[]
@PubSubClient // <1>
interface AnimalClient {

	@Topic("animals") // <2>
	fun send(data: ByteArray) // <3>

}
// end::clazz[]