from abc import ABC, abstractmethod

# tag::imports[]
from micronaut.gcp.pubsub.annotation import PubSubClient, Topic
from micronaut.gcp.pubsub.support.Animal import Animal
# end::imports[]


# tag::clazz[]
@PubSubClient
class CustomConfigurationClient(ABC):

    @Topic(value="animals", configuration="batching")  # <1>
    @abstractmethod
    def batch_send(self, animal: Animal) -> None:
        ...

    @Topic(value="animals", configuration="immediate")  # <2>
    @abstractmethod
    def send(self, animal: Animal) -> None:
        ...
# end::clazz[]
