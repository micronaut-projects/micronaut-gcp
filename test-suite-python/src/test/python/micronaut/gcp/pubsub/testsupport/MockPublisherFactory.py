import java
from com.google.api.core import ApiFutures
from com.google.cloud.pubsub.v1 import PublisherInterface
from jakarta.inject import Singleton
from micronaut.context.annotation import Replaces

from .MockPubSubEngine import MockPubSubEngine

# TODO(python): java.type needed because importing `io.micronaut.gcp.pubsub.support` collides with the Python snippet package of the same name
PublisherFactory = java.type("io.micronaut.gcp.pubsub.support.PublisherFactory")
PublisherFactoryConfig = java.type("io.micronaut.gcp.pubsub.support.PublisherFactoryConfig")


class MockPublisher:
    """Python implementation of com.google.cloud.pubsub.v1.PublisherInterface publishing to the mock engine."""

    def __init__(self, engine: MockPubSubEngine, topic: str):
        self.engine = engine
        self.topic = topic

    def publish(self, message):
        self.engine.publish(message, self.topic)
        return ApiFutures.immediateFuture("1234")


@Singleton
@Replaces("io.micronaut.gcp.pubsub.support.DefaultPublisherFactory")
class MockPublisherFactory(PublisherFactory):

    def __init__(self, engine: MockPubSubEngine):
        self.engine = engine

    def createPublisher(self, config: PublisherFactoryConfig) -> PublisherInterface:
        return MockPublisher(self.engine, config.getTopicState().getProjectTopicName().getTopic())
