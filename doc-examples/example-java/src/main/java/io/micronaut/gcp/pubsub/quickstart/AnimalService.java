package io.micronaut.gcp.pubsub.quickstart;
//tag imports[]
import io.micronaut.gcp.pubsub.support.Animal;
import javax.inject.Singleton;
// end::imports[]

// tag::clazz[]
@Singleton
public class AnimalService {
    private final AnimalClient animalClient;

    public AnimalService(AnimalClient animalClient) { // <1>
        this.animalClient = animalClient;
    }

    public void someBusinessMethod(Animal animal) {
        byte[] serializedBody = serialize(animal);
        animalClient.send(serializedBody);
    }

    private byte[] serialize(Animal animal) { // <2>
        return null;
    }
}
// end::clazz[]
