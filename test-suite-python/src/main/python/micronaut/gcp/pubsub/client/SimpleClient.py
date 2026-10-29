from abc import ABC, abstractmethod

# tag::imports[]
from com.google.pubsub.v1 import PubsubMessage
from micronaut.gcp.pubsub.annotation import PubSubClient, Topic
from micronaut.gcp.pubsub.support.Animal import Animal
# end::imports[]


# tag::clazz[]
@PubSubClient  # <1>
class SimpleClient(ABC):

    @Topic("animals")
    @abstractmethod
    def send_message(self, message: PubsubMessage) -> None:  # <2>
        ...

    @Topic("animals")
    @abstractmethod
    def send_bytes(self, data: bytes) -> None:  # <3>
        ...

    @Topic("animals")
    @abstractmethod
    def send_animal(self, animal: Animal) -> None:  # <4>
        ...
# end::clazz[]
