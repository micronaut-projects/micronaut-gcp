from com.google.pubsub.v1 import PubsubMessage
from jakarta.inject import Singleton
from java.util.concurrent import TimeUnit


class MockAckReplyConsumer:
    """Python implementation of com.google.cloud.pubsub.v1.AckReplyConsumer recording the reply."""

    def __init__(self, engine, subscription: str, message):
        self.engine = engine
        self.subscription = subscription
        self.message = message

    def ack(self) -> None:
        self.engine.record(self.subscription, self.message, MockPubSubEngine.ACK)

    def nack(self) -> None:
        self.engine.record(self.subscription, self.message, MockPubSubEngine.NACK)


@Singleton
class MockPubSubEngine:
    """
    In-memory replacement of the Pub/Sub broker: a message published to a topic is delivered to the
    MessageReceiver registered for the subscription of the same name (the Java test suite runs the
    Pub/Sub emulator in Docker instead).
    """

    ACK = "ack"
    NACK = "nack"

    def __init__(self):
        self.receivers = {}
        self.acknowledgements = {}

    def publish(self, message, topic: str) -> None:
        # fake 1234 as ID for all messages for testing
        message_with_id = PubsubMessage.newBuilder(message).setMessageId("1234").build()
        receiver = self.receivers.get(topic)
        if receiver is not None:
            receiver.receiveMessage(message_with_id, MockAckReplyConsumer(self, topic, message_with_id))

    def register_receiver(self, receiver, subscription: str) -> None:
        self.receivers[subscription] = receiver

    def record(self, subscription: str, message, reply: str) -> None:
        self.acknowledgements.setdefault(subscription, []).append((message, reply))

    def replies(self, subscription: str) -> list[str]:
        return [reply for _, reply in self.acknowledgements.get(subscription, [])]

    def await_reply(self, subscription: str, count: int = 1, timeout_millis: int = 5000) -> list[str]:
        waited = 0
        while len(self.replies(subscription)) < count and waited < timeout_millis:
            TimeUnit.MILLISECONDS.sleep(50)
            waited += 50
        return self.replies(subscription)
