from typing import Annotated

# tag::imports[]
from com.google.pubsub.v1 import PubsubMessage
from micronaut.context.annotation import Requires
from micronaut.gcp.pubsub.annotation import MessageId, PubSubListener, PushSubscription
from micronaut.gcp.pubsub.support.Animal import Animal
from micronaut.scheduling import TaskExecutors
from micronaut.scheduling.annotation import ExecuteOn

from .MessageProcessor import MessageProcessor
# end::imports[]


@Requires(property="spec.name", value="ContentTypePushSubscriberTest")
# tag::clazz[]
@PubSubListener
@ExecuteOn(TaskExecutors.BLOCKING)  # <1>
class ContentTypePushSubscriber:

    def __init__(self, message_processor: MessageProcessor):
        self.message_processor = message_processor

    @PushSubscription("raw-push-subscription")  # <2>
    def receive_raw(self, data: bytes, id: Annotated[str, MessageId]) -> None:
        # process with blocking code
        self.message_processor.handle_byte_array_message(data).block()

    @PushSubscription("native-push-subscription")  # <3>
    def receive_native(self, message: PubsubMessage) -> None:
        # process with blocking code
        self.message_processor.handle_pub_sub_message(message).block()

    @PushSubscription("animals-push")  # <4>
    def receive_pojo(self, animal: Animal, id: Annotated[str, MessageId]) -> None:
        # process with blocking code
        self.message_processor.handle_animal_message(animal).block()

    @PushSubscription(value="animals-legacy-push", contentType="application/xml")  # <5>
    def receive_xml(self, animal: Animal, id: Annotated[str, MessageId]) -> None:
        # process with blocking code
        self.message_processor.handle_animal_message(animal).block()
# end::clazz[]
