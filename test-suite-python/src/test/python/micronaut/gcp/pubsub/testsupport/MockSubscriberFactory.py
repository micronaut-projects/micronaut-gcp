from com.google.cloud.pubsub.v1 import SubscriberInterface
from jakarta.inject import Singleton
from micronaut.context.annotation import Replaces
from micronaut.gcp.pubsub.bind import SubscriberFactory, SubscriberFactoryConfig

from .MockPubSubEngine import MockPubSubEngine


@Singleton
@Replaces("io.micronaut.gcp.pubsub.bind.DefaultSubscriberFactory")
class MockSubscriberFactory(SubscriberFactory):

    def __init__(self, engine: MockPubSubEngine):
        self.engine = engine

    def createSubscriber(self, config: SubscriberFactoryConfig) -> SubscriberInterface:
        self.engine.register_receiver(config.getReceiver(), config.getSubscriptionName().getSubscription())
        return None
