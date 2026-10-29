from abc import ABC, abstractmethod
from typing import Annotated

# tag::imports[]
from micronaut.gcp.pubsub.annotation import PubSubClient, Topic
from micronaut.gcp.pubsub.support.Animal import Animal
from micronaut.messaging.annotation import MessageHeader
# end::imports[]


# tag::clazz[]
@PubSubClient
@MessageHeader(name="application-name", value="petclinic")  # <1>
class CustomHeadersClient(ABC):

    @MessageHeader(name="status", value="healthy")  # <2>
    @Topic("animals")
    @abstractmethod
    def send_with_static_headers(self, animal: Animal) -> None:
        ...

    @Topic("animals")
    @abstractmethod
    def send_with_dynamic_headers(self, animal: Animal, code: Annotated[int, MessageHeader(name="code")]) -> None:  # <3>
        ...
# end::clazz[]
