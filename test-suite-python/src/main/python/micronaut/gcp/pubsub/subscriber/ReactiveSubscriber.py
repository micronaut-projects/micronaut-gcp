from typing import Annotated

# tag::imports[]
from com.google.pubsub.v1 import PubsubMessage
from micronaut.context.annotation import Requires
from micronaut.gcp.pubsub.annotation import MessageId, PubSubListener, Subscription
from micronaut.gcp.pubsub.support.Animal import Animal
from reactor.core.publisher import Mono

from .MessageProcessor import MessageProcessor
# end::imports[]


@Requires(property="spec.name", value="ReactiveSubscriberTest")
# tag::clazz[]
@PubSubListener
class ReactiveSubscriber:

    def __init__(self, message_processor: MessageProcessor):
        self.message_processor = message_processor

    @Subscription("raw-subscription")  # <1>
    def receive_raw(self, data: Mono[bytes], id: Annotated[str, MessageId]) -> Mono[object]:
        return data.flatMap(self.message_processor.handle_byte_array_message)

    @Subscription("native-subscription")  # <2>
    def receive_native(self, message: Mono[PubsubMessage]) -> Mono[object]:
        return message.flatMap(self.message_processor.handle_pub_sub_message)

    @Subscription("animals")  # <3>
    def receive_pojo(self, animal: Mono[Animal], id: Annotated[str, MessageId]) -> Mono[object]:
        return animal.flatMap(self.message_processor.handle_animal_message)

    @Subscription(value="animals-legacy", contentType="application/xml")  # <4>
    def receive_xml(self, animal: Mono[Animal], id: Annotated[str, MessageId]) -> Mono[object]:
        return animal.flatMap(self.message_processor.handle_animal_message)
# end::clazz[]
