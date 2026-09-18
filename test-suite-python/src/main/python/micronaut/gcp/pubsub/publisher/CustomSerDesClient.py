from abc import ABC, abstractmethod

# tag::imports[]
from com.google.pubsub.v1 import PubsubMessage
from micronaut.gcp.pubsub.annotation import PubSubClient, Topic
from micronaut.gcp.pubsub.support.Animal import Animal
# end::imports[]


# tag::clazz[]
@PubSubClient
class CustomSerDesClient(ABC):

    @Topic("animals")  # <1>
    @abstractmethod
    def send_message(self, pubsub_message: PubsubMessage) -> None:
        ...

    @Topic("animals")  # <2>
    @abstractmethod
    def send_bytes(self, data: bytes) -> None:
        ...

    @Topic(value="animals", contentType="image/gif")  # <3>
    @abstractmethod
    def send_bytes_with_custom_type(self, data: bytes) -> None:
        ...

    @Topic("animals")  # <4>
    @abstractmethod
    def send_animal(self, animal: Animal) -> None:
        ...

    @Topic(value="animals", contentType="application/xml")  # <5>
    @abstractmethod
    def send_animal_with_custom_type(self, animal: Animal) -> None:
        ...
# end::clazz[]
