from abc import ABC, abstractmethod

# tag::imports[]
from micronaut.gcp.pubsub.annotation import PubSubClient, Topic
from micronaut.gcp.pubsub.support.Animal import Animal
# end::imports[]


# tag::clazz[]
@PubSubClient
class LocationClient(ABC):

    @Topic(value="animals", endpoint="europe-west1-pubsub.googleapis.com:443")  # <1>
    @abstractmethod
    def send(self, animal: Animal) -> None:
        ...
# end::clazz[]
