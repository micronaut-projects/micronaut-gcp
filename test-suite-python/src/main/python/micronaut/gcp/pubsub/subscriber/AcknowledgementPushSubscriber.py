# tag::imports[]
from micronaut.context.annotation import Requires
from micronaut.gcp.pubsub.annotation import PubSubListener, PushSubscription
from micronaut.gcp.pubsub.support.Animal import Animal
from micronaut.messaging import Acknowledgement
from micronaut.scheduling import TaskExecutors
from micronaut.scheduling.annotation import ExecuteOn
from reactor.core.publisher import Mono

from .MessageProcessor import MessageProcessor
# end::imports[]


@Requires(property="spec.name", value="AcknowledgementPushSubscriberTest")
# tag::clazz[]
@PubSubListener
class AcknowledgementPushSubscriber:

    def __init__(self, message_processor: MessageProcessor):
        self.message_processor = message_processor

    @ExecuteOn(TaskExecutors.BLOCKING)
    @PushSubscription("animals-push")
    def on_message(self, animal: Animal, acknowledgement: Acknowledgement) -> None:
        if self.message_processor.handle_animal_message(animal).block():
            acknowledgement.ack()
        else:
            acknowledgement.nack()

    @PushSubscription("animals-async-push")
    def on_reactive_message(self, animal: Mono[Animal], acknowledgement: Acknowledgement) -> Mono[bool]:
        def acknowledge(result):
            if result:
                acknowledgement.ack()
            else:
                acknowledgement.nack()

        return animal.flatMap(self.message_processor.handle_animal_message).doOnNext(acknowledge)
# end::clazz[]
