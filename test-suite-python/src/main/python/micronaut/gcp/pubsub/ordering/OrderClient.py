from abc import ABC, abstractmethod
from typing import Annotated

# tag::imports[]
from micronaut.gcp.pubsub.annotation import OrderingKey, PubSubClient, Topic
from micronaut.gcp.pubsub.support.Order import Order
# end::imports[]


# tag::clazz[]
@PubSubClient
class OrderClient(ABC):

    @Topic(value="orders", endpoint="us-central1-pubsub.googleapis.com:443")  # <1>
    @abstractmethod
    def send(self, order: Order, key: Annotated[str, OrderingKey]) -> None:  # <2>
        ...
# end::clazz[]
