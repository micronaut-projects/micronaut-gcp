package io.micronaut.gcp.pubsub.publisher

//tag imports[]
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