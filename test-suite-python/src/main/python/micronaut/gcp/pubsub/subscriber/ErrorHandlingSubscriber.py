# tag::imports[]
from com.google.pubsub.v1 import PubsubMessage
from micronaut.context.annotation import Requires
from micronaut.gcp.pubsub.annotation import PubSubListener, Subscription
from micronaut.gcp.pubsub.bind import PubSubConsumerState
from micronaut.gcp.pubsub.exception import PubSubMessageReceiverException, PubSubMessageReceiverExceptionHandler
from micronaut.gcp.pubsub.support.Animal import Animal
# end::imports[]


# There are currently no tests for this class. It is disabled in the test environment
# in order to prevent clashes with other subscribers.
@Requires(notEnv="test")
# tag::clazz[]
@PubSubListener
class ErrorHandlingSubscriber(PubSubMessageReceiverExceptionHandler):  # <1>

    @Subscription("animals")
    def on_message(self, animal: Animal) -> None:
        raise RuntimeError("error")

    def handle(self, exception: PubSubMessageReceiverException) -> None:  # <2>
        listener = exception.getListener()  # <3>
        state: PubSubConsumerState = exception.getState()  # <4>
        original_message: PubsubMessage = state.getPubsubMessage()
        content_type = state.getContentType()
        # some logic
        state.getAckReplyConsumer().ack()  # <5>
# end::clazz[]
