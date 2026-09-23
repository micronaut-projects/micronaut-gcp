from abc import ABC, abstractmethod
from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property, Requires
from micronaut.gcp.pubsub.annotation import PubSubClient, Topic
from micronaut.gcp.pubsub.support.Animal import Animal
from micronaut.gcp.pubsub.testsupport.MockPubSubEngine import MockPubSubEngine
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import BeforeEach, Test

from micronaut.gcp.pubsub.subscriber.MessageProcessor import MessageProcessor


@PubSubClient
@Requires(property="spec.name", value="AcknowledgementSubscriberTest")
class AcknowledgementTestPublisher(ABC):

    @Topic("animals")
    @abstractmethod
    def publish_animal(self, animal: Animal) -> None:
        ...

    @Topic("animals-async")
    @abstractmethod
    def publish_animal_async(self, animal: Animal) -> None:
        ...


@MicronautTest
@Property(name="spec.name", value="AcknowledgementSubscriberTest")
class AcknowledgementSubscriberTest:
    publisher: Annotated[AcknowledgementTestPublisher, Inject]
    messageProcessor: Annotated[MessageProcessor, Inject]
    engine: Annotated[MockPubSubEngine, Inject]

    @BeforeEach
    def setup(self):
        self.messageProcessor.result = True
        self.messageProcessor.received = []
        self.engine.acknowledgements.clear()

    @Test
    def test_blocking_ack(self):
        self.messageProcessor.result = True

        self.publisher.publish_animal(Animal("dog"))

        assert self.engine.await_reply("animals") == [MockPubSubEngine.ACK]
        assert self.messageProcessor.received[0].name == "dog"

    @Test
    def test_blocking_nack(self):
        self.messageProcessor.result = False

        self.publisher.publish_animal(Animal("cat"))

        assert self.engine.await_reply("animals") == [MockPubSubEngine.NACK]
        assert self.messageProcessor.received[0].name == "cat"

    @Test
    def test_async_ack(self):
        self.messageProcessor.result = True

        self.publisher.publish_animal_async(Animal("dog"))

        assert self.engine.await_reply("animals-async") == [MockPubSubEngine.ACK]
        assert self.messageProcessor.received[0].name == "dog"

    @Test
    def test_async_nack(self):
        self.messageProcessor.result = False

        self.publisher.publish_animal_async(Animal("cat"))

        assert self.engine.await_reply("animals-async") == [MockPubSubEngine.NACK]
        assert self.messageProcessor.received[0].name == "cat"
