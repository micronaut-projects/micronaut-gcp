from com.google.pubsub.v1 import PubsubMessage
from jakarta.inject import Singleton
from micronaut.context.annotation import Requires
from micronaut.gcp.pubsub.support.Animal import Animal
from reactor.core.publisher import Mono

from micronaut.gcp.pubsub.subscriber.MessageProcessor import MessageProcessor


@Singleton
@Requires(property="spec.name", value="ReactivePushSubscriberTest")
class ReactivePushMockMessageProcessor(MessageProcessor):

    def __init__(self):
        self.unwrapped_result = None

    def handle_byte_array_message(self, message: bytes) -> Mono[bool]:
        self.unwrapped_result = message
        return Mono.just(True)

    def handle_pub_sub_message(self, pubsub_message: PubsubMessage) -> Mono[bool]:
        self.unwrapped_result = pubsub_message
        return Mono.just(True)

    def handle_animal_message(self, message: Animal) -> Mono[bool]:
        self.unwrapped_result = message
        return Mono.just(True)
