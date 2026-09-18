from typing import Annotated

# tag::imports[]
from micronaut.context.annotation import Requires
from micronaut.gcp.pubsub.annotation import PubSubListener, Subscription
from micronaut.gcp.pubsub.support.Animal import Animal
from micronaut.messaging.annotation import MessageHeader
# end::imports[]


# There are currently no tests for this class. It is disabled in the test environment
# in order to prevent clashes with other subscribers.
@Requires(notEnv="test")
# tag::clazz[]
@PubSubListener
class CustomHeaderSubscriber:

    @Subscription("animals")
    def on_message(self, animal: Animal, content_type: Annotated[str, MessageHeader("Content-Type")], code: Annotated[int, MessageHeader("code")]) -> None:  # <1>
        pass
# end::clazz[]
