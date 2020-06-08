package example;

import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
class PetService {
    private final Map<String, Pet> pets = new ConcurrentHashMap<>();

    public PetService() {
        pets.put("Dino", new Pet("Dino", 12));
        pets.put("Bobcat", new Pet("Bobcat", 8));
    }

    Collection<Pet> allPets() {
        return pets.values();
    }

    Optional<Pet> findPet(@NotBlank String name) {
        return Optional.ofNullable(pets.get(name));
    }

    Pet savePet(@NotNull @Valid Pet pet) {
        pets.put(pet.getName(), pet);
        return pet;
    }

    void deletePet(@NotBlank String name) {
        pets.remove(name);
    }
}
