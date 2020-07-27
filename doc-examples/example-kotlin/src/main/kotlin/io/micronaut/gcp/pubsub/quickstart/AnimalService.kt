package io.micronaut.gcp.pubsub.quickstart

//tag imports[]
import io.micronaut.gcp.pubsub.support.Animal
import javax.inject.Singleton
// end::imports[]

// tag::clazz[]
@Singleton
class AnimalService(private val animalClient: AnimalClient)  { // <1>

	fun someBusinessMethod(animal: Animal) {
		val serializedBody = serialize(animal)
		animalClient.send(serializedBody)
	}

	private fun serialize(animal: Animal): ByteArray { // <2>
		return ByteArray(0)
	}

}
// end::clazz[]