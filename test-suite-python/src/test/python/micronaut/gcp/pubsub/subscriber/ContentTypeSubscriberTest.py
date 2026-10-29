from abc import ABC, abstractmethod
from typing import Annotated

import java
from com.google.pubsub.v1 import PubsubMessage
from jakarta.inject import Inject, Singleton
from java.util.concurrent import TimeUnit
from micronaut.context.annotation import Property, Requires
from micronaut.gcp.pubsub.annotation import PubSubClient, Topic
from micronaut.gcp.pubsub.support.Animal import Animal
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import BeforeEach, Test

from .ContentTypeMockMessageProcessor import ContentTypeMockMessageProcessor
from micronaut.gcp.pubsub.subscriber.MessageProcessor import MessageProcessor


@PubSubClient
@Requires(property="spec.name", value="ContentTypeSubscriberTest")
class ContentTypeTestPublisher(ABC):

    @Topic("raw-subscription")
    @abstractmethod
    def publish_raw(self, payload: bytes) -> None:
        ...

    @Topic("native-subscription")
    @abstractmethod
    def publish_native(self, payload: bytes) -> None:
        ...

    @Topic("animals")
    @abstractmethod
    def publish_animal(self, animal: Animal) -> None:
        ...

    @Topic(value="animals-legacy", contentType="application/xml")
    @abstractmethod
    def publish_animal_as_xml(self, animal: Animal) -> None:
        ...


@Property(name="spec.name", value="ContentTypeSubscriberTest")
@MicronautTest
class ContentTypeSubscriberTest:
    publisher: Annotated[ContentTypeTestPublisher, Inject]
    messageProcessor: Annotated[MessageProcessor, Inject]

    @BeforeEach
    def setup(self):
        self.messageProcessor.received = None

    def await_received(self):
        for _ in range(60):
            if self.messageProcessor.received is not None:
                return self.messageProcessor.received
            TimeUnit.MILLISECONDS.sleep(50)
        assert self.messageProcessor.received is not None, "no message received"

    @Test
    def test_raw_bytes(self):
        self.publisher.publish_raw("foo".encode())

        received = self.await_received()
        assert bytes(received).decode() == "foo"

    @Test
    def test_native_message(self):
        self.publisher.publish_native("foo".encode())

        received = self.await_received()
        assert java.instanceof(received, PubsubMessage)
        assert received.getData().toStringUtf8() == "foo"

    @Test
    def test_json_pojo(self):
        self.publisher.publish_animal(Animal("dog"))

        received = self.await_received()
        assert received.name == "dog"

    @Test
    def test_xml_pojo(self):
        self.publisher.publish_animal_as_xml(Animal("cat"))

        received = self.await_received()
        assert received.name == "cat"
