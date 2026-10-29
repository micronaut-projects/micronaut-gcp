from abc import ABC, abstractmethod

# tag::imports[]
from micronaut.gcp.pubsub.annotation import PubSubClient, Topic
from micronaut.gcp.pubsub.support.Animal import Animal
from reactor.core.publisher import Mono
# end::imports[]


# tag::clazz[]
@PubSubClient
class CustomReturnClient(ABC):

    @Topic("animals")
    @abstractmethod
    def send(self, animal: Animal) -> None:  # <1>
        ...

    @Topic("animals")
    @abstractmethod
    def send_with_id(self, animal: Animal) -> str:  # <2>
        ...

    @Topic("animals")
    @abstractmethod
    def send_reactive(self, animal: Animal) -> Mono[str]:  # <3>
        ...
# end::clazz[]
