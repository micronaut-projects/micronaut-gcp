from typing import Annotated

from java.lang import Long

# tag::imports[]
from micronaut.gcp.pubsub.annotation import PubSubListener
from micronaut.gcp.pubsub.support.Animal import Animal

from .MessagePublishTime import MessagePublishTime
# end::imports[]


# tag::clazz[]
@PubSubListener
class CustomBindingSubscriber:

    def on_message(self, animal: Animal, publish_time: Annotated[Long, MessagePublishTime]) -> None:  # <1>
        pass
# end::clazz[]
