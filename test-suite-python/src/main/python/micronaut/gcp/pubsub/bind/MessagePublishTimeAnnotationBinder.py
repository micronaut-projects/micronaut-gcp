# tag::imports[]
import java
from com.google.protobuf.util import Timestamps
from jakarta.inject import Singleton
from micronaut.core.bind import ArgumentBinder
from micronaut.core.convert import ArgumentConversionContext, ConversionService

from .MessagePublishTime import MessagePublishTime

# TODO(python): java.type needed because importing `io.micronaut.gcp.pubsub.bind` collides with the Python snippet package of the same name
PubSubAnnotatedArgumentBinder = java.type("io.micronaut.gcp.pubsub.bind.PubSubAnnotatedArgumentBinder")
PubSubConsumerState = java.type("io.micronaut.gcp.pubsub.bind.PubSubConsumerState")
# end::imports[]

# TODO(python): java.type needed because `getAnnotationType()` must return the generated Java annotation class, and the Python decorator function is not a `Class`
MessagePublishTimeClass = java.type("micronaut.gcp.pubsub.bind.MessagePublishTime")


# tag::clazz[]
@Singleton  # <1>
class MessagePublishTimeAnnotationBinder(PubSubAnnotatedArgumentBinder):  # <2>

    def __init__(self, conversion_service: ConversionService):  # <3>
        self.conversion_service = conversion_service

    def getAnnotationType(self):
        return MessagePublishTimeClass

    def bind(self, context: ArgumentConversionContext, source: PubSubConsumerState) -> ArgumentBinder.BindingResult[object]:
        epoch_millis = Timestamps.toMillis(source.getPubsubMessage().getPublishTime())  # <4>
        return lambda: self.conversion_service.convert(epoch_millis, context)  # <5>
# end::clazz[]
