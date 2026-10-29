from abc import ABC, abstractmethod

# tag::imports[]
from micronaut.gcp.pubsub.annotation import PubSubClient, Topic
from micronaut.gcp.pubsub.support.Animal import Animal
# end::imports[]


# tag::clazz[]
@PubSubClient
class MultipleProjectClient(ABC):

    @Topic("animals")
    @abstractmethod
    def send_us(self, animal: Animal) -> None:  # <1>
        ...

    @Topic("projects/eu-project/topics/animals")
    @abstractmethod
    def send_eu(self, animal: Animal) -> None:  # <2>
        ...
# end::clazz[]
