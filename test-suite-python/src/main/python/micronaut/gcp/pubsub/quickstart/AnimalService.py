# tag::imports[]
from jakarta.inject import Singleton
from micronaut.gcp.pubsub.support.Animal import Animal

from .AnimalClient import AnimalClient
# end::imports[]


# tag::clazz[]
@Singleton
class AnimalService:

    def __init__(self, animal_client: AnimalClient):  # <1>
        self.animal_client = animal_client

    def some_business_method(self, animal: Animal) -> None:
        serialized_body = self.serialize(animal)
        self.animal_client.send(serialized_body)

    def serialize(self, animal: Animal) -> bytes | None:  # <2>
        return None
# end::clazz[]
