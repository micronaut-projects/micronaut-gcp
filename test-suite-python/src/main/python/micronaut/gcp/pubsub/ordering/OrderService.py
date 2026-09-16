# tag::imports[]
from jakarta.inject import Singleton
from micronaut.gcp.pubsub.support.Order import Order

from .OrderClient import OrderClient
# end::imports[]


# tag::clazz[]
@Singleton
class OrderService:

    def __init__(self, client: OrderClient):
        self.client = client

    def place_order(self) -> None:
        order = Order(100, "GOOG")
        self.client.send(order, order.symbol)
# end::clazz[]
