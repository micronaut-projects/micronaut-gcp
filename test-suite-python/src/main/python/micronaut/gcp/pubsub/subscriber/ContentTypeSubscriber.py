from typing import Annotated

# tag::imports[]
from com.google.pubsub.v1 import PubsubMessage
from micronaut.context.annotation import Requires
from micronaut.gcp.pubsub.annotation import MessageId, PubSubListener, Subscription
from micronaut.gcp.pubsub.support.Animal import Animal

from .MessageProcessor import MessageProcessor
# end::imports[]


@Requires(property="spec.name", value="ContentTypeSubscriberTest")
# tag::clazz[]
@PubSubListener
class ContentTypeSubscriber:

    def __init__(self, message_processor: MessageProcessor):
        self.message_processor = message_processor

    @Subscription("raw-subscription")  # <1>
    def receive_raw(self, data: bytes, id: Annotated[str, MessageId]) -> None:
        self.message_processor.handle_byte_array_message(data).block()

    @Subscription("native-subscription")  # <2>
    def receive_native(self, message: PubsubMessage) -> None:
        self.message_processor.handle_pub_sub_message(message).block()

    @Subscription("animals")  # <3>
    def receive_pojo(self, animal: Animal, id: Annotated[str, MessageId]) -> None:
        self.message_processor.handle_animal_message(animal).block()

    @Subscription(value="animals-legacy", contentType="application/xml")  # <4>
    def receive_xml(self, animal: Animal, id: Annotated[str, MessageId]) -> None:
        self.message_processor.handle_animal_message(animal).block()
# end::clazz[]
