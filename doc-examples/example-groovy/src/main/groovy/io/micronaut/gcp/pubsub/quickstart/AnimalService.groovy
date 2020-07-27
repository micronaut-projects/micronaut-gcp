package io.micronaut.gcp.pubsub.quickstart


import io.micronaut.gcp.pubsub.support.Animal;
//tag imports[]
import javax.inject.Singleton;
// end::imports[]

// tag::clazz[]
@Singleton
class AnimalService {
    private final AnimalClient animalClient;

    AnimalService(AnimalClient animalClient) { // <1>
        this.animalClient = animalClient
    }

    void someBusinessMethod(Animal animal) {
        byte[] serializedBody = serialize(animal)
        animalClient.send(serializedBody)
    }

    private byte[] serialize(Animal animal) { // <2>
        return null
    }
}
// end::clazz[]