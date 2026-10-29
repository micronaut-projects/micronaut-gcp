from micronaut.context.annotation import Requires
# tag::imports[]
from micronaut.gcp.pubsub.annotation import PubSubListener
# end::imports[]
# tag::pushimport[]
from micronaut.gcp.pubsub.annotation import PushSubscription
# end::pushimport[]
# tag::pullimport[]
from micronaut.gcp.pubsub.annotation import Subscription
# end::pullimport[]


# There are currently no tests for this class. It is disabled in the test environment
# in order to prevent clashes with other subscribers.
@Requires(notEnv="test")
# tag::clazz[]
@PubSubListener  # <1>
class AnimalListener:
# end::clazz[]

    # tag::pull[]
    @Subscription("animals")  # <2>
    def on_message(self, data: bytes) -> None:  # <3>
        print("Message received")
    # end::pull[]

    # tag::push[]
    @PushSubscription("animals-push")  # <2>
    def on_push_message(self, data: bytes) -> None:  # <3>
        print("Message received")
    # end::push[]
# tag::clazzend[]
# end::clazzend[]
