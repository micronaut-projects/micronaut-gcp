from abc import ABC, abstractmethod

# tag::imports[]
from micronaut.gcp.pubsub.annotation import PubSubClient, Topic
# end::imports[]


# tag::clazz[]
@PubSubClient  # <1>
class AnimalClient(ABC):

    @Topic("animals")  # <2>
    @abstractmethod
    def send(self, data: bytes) -> None:  # <3>
        ...
# end::clazz[]
