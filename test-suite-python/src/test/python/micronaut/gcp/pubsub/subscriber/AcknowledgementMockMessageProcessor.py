from com.google.pubsub.v1 import PubsubMessage
from jakarta.inject import Singleton
from micronaut.context.annotation import Requires
from micronaut.gcp.pubsub.support.Animal import Animal
from reactor.core.publisher import Mono

from micronaut.gcp.pubsub.subscriber.MessageProcessor import MessageProcessor


@Singleton
@Requires(property="spec.name", pattern="Acknowledgement(Push)?SubscriberTest")
class AcknowledgementMockMessageProcessor(MessageProcessor):
    """Records the received animals and answers with the configured result (ack when True, nack when False)."""

    def __init__(self):
        self.result = True
        self.received = []

    def handle_byte_array_message(self, message: bytes) -> Mono[bool]:
        return Mono.just(self.result)

    def handle_pub_sub_message(self, pubsub_message: PubsubMessage) -> Mono[bool]:
        return Mono.just(self.result)

    def handle_animal_message(self, message: Animal) -> Mono[bool]:
        self.received.append(message)
        return Mono.just(self.result)
