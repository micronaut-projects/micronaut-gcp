import java
from com.google.cloud.pubsub.v1 import SubscriberInterface
from jakarta.inject import Singleton
from micronaut.context.annotation import Replaces

from .MockPubSubEngine import MockPubSubEngine

# TODO(python): java.type needed because importing `io.micronaut.gcp.pubsub.bind` collides with the Python snippet package of the same name
SubscriberFactory = java.type("io.micronaut.gcp.pubsub.bind.SubscriberFactory")
SubscriberFactoryConfig = java.type("io.micronaut.gcp.pubsub.bind.SubscriberFactoryConfig")


@Singleton
@Replaces("io.micronaut.gcp.pubsub.bind.DefaultSubscriberFactory")
class MockSubscriberFactory(SubscriberFactory):

    def __init__(self, engine: MockPubSubEngine):
        self.engine = engine

    def createSubscriber(self, config: SubscriberFactoryConfig) -> SubscriberInterface:
        self.engine.register_receiver(config.getReceiver(), config.getSubscriptionName().getSubscription())
        return None
