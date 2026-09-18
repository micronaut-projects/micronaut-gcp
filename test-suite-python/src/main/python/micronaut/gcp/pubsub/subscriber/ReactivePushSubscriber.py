from typing import Annotated

# tag::imports[]
from com.google.pubsub.v1 import PubsubMessage
from micronaut.context.annotation import Requires
from micronaut.gcp.pubsub.annotation import MessageId, PubSubListener, PushSubscription
from micronaut.gcp.pubsub.support.Animal import Animal
from org.reactivestreams import Publisher
from reactor.core.publisher import Mono

from .MessageProcessor import MessageProcessor
# end::imports[]


@Requires(property="spec.name", value="ReactivePushSubscriberTest")
# tag::clazz[]
@PubSubListener
class ReactivePushSubscriber:

    def __init__(self, message_processor: MessageProcessor):
        self.message_processor = message_processor

    @PushSubscription("raw-push-subscription")  # <1>
    def receive_raw(self, data: Mono[bytes], id: Annotated[str, MessageId]) -> Publisher[object]:
        return data.flatMap(self.message_processor.handle_byte_array_message)

    @PushSubscription("native-push-subscription")  # <2>
    def receive_native(self, message: Mono[PubsubMessage]) -> Publisher[object]:
        return message.flatMap(self.message_processor.handle_pub_sub_message)

    @PushSubscription("animals-push")  # <3>
    def receive_pojo(self, animal: Mono[Animal], id: Annotated[str, MessageId]) -> Publisher[object]:
        return animal.flatMap(self.message_processor.handle_animal_message)

    @PushSubscription(value="animals-legacy-push", contentType="application/xml")  # <4>
    def receive_xml(self, animal: Mono[Animal], id: Annotated[str, MessageId]) -> Publisher[object]:
        return animal.flatMap(self.message_processor.handle_animal_message)
# end::clazz[]
