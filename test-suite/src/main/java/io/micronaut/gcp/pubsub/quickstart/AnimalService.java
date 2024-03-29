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
package io.micronaut.gcp.pubsub.quickstart;
//tag::imports[]
import io.micronaut.gcp.pubsub.support.Animal;
import jakarta.inject.Singleton;
// end::imports[]

// tag::clazz[]
@Singleton
public final class AnimalService {
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
