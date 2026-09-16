# tag::imports[]
from micronaut.context.annotation import Requires
from micronaut.gcp.pubsub.annotation import PubSubListener, Subscription
from micronaut.gcp.pubsub.support.Animal import Animal
# end::imports[]


# There are currently no tests for this class. It is disabled in the test environment
# in order to prevent clashes with other subscribers.
@Requires(notEnv="test")
# tag::clazz[]
@PubSubListener
class CustomConfigurationSubscriber:

    @Subscription(value="animals", configuration="custom")  # <1>
    def on_message(self, animal: Animal) -> None:
        pass
# end::clazz[]
