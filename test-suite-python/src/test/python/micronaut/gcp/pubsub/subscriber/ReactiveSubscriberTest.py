from abc import ABC, abstractmethod
from typing import Annotated

import java
from com.google.pubsub.v1 import PubsubMessage
from jakarta.inject import Inject
from java.util.concurrent import TimeUnit
from micronaut.context.annotation import Property, Requires
from micronaut.gcp.pubsub.annotation import PubSubClient, Topic
from micronaut.gcp.pubsub.support.Animal import Animal
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import BeforeEach, Disabled, Test

from micronaut.gcp.pubsub.subscriber.MessageProcessor import MessageProcessor


@PubSubClient
@Requires(property="spec.name", value="ReactiveSubscriberTest")
class ReactiveTestPublisher(ABC):

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


@Property(name="spec.name", value="ReactiveSubscriberTest")
@MicronautTest
class ReactiveSubscriberTest:
    publisher: Annotated[ReactiveTestPublisher, Inject]
    messageProcessor: Annotated[MessageProcessor, Inject]

    @BeforeEach
    def setup(self):
        self.messageProcessor.unwrapped_result = None

    def await_result(self):
        for _ in range(40):
            if self.messageProcessor.unwrapped_result is not None:
                return self.messageProcessor.unwrapped_result
            TimeUnit.MILLISECONDS.sleep(50)
        assert self.messageProcessor.unwrapped_result is not None, "no message received"

    @Disabled("TODO(python): bytes as a generic type argument (Mono[bytes]) is compiled to Mono<Byte>, so the message body binder cannot resolve byte[] as the body type")
    @Test
    def test_raw_bytes(self):
        self.publisher.publish_raw("foo".encode())

        result = self.await_result()
        assert bytes(result).decode() == "foo"

    @Test
    def test_native_message(self):
        self.publisher.publish_native("foo".encode())

        result = self.await_result()
        assert java.instanceof(result, PubsubMessage)
        assert result.getData().toStringUtf8() == "foo"

    @Test
    def test_json_pojo(self):
        self.publisher.publish_animal(Animal("dog"))

        result = self.await_result()
        assert result.name == "dog"

    @Test
    def test_xml_pojo(self):
        self.publisher.publish_animal_as_xml(Animal("cat"))

        result = self.await_result()
        assert result.name == "cat"
