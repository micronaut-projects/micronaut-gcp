from abc import ABC, abstractmethod

from com.google.pubsub.v1 import PubsubMessage
from micronaut.gcp.pubsub.support.Animal import Animal
from reactor.core.publisher import Mono


class MessageProcessor(ABC):

    @abstractmethod
    def handle_byte_array_message(self, message: bytes) -> Mono[bool]:
        ...

    @abstractmethod
    def handle_pub_sub_message(self, pubsub_message: PubsubMessage) -> Mono[bool]:
        ...

    @abstractmethod
    def handle_animal_message(self, message: Animal) -> Mono[bool]:
        ...
