# tag::imports[]
import java
from com.google.protobuf.util import Timestamps
from jakarta.inject import Singleton
from micronaut.core.bind import ArgumentBinder
from micronaut.core.convert import ArgumentConversionContext, ConversionService
from micronaut.gcp.pubsub.bind import PubSubAnnotatedArgumentBinder, PubSubConsumerState

from .MessagePublishTime import MessagePublishTime
# end::imports[]

# TODO(python): java.type needed because `getAnnotationType()` must return the generated Java annotation class; a Python-defined
# annotation is a decorator function at runtime and is not converted to a `Class` (imported Java annotations are)
MessagePublishTimeClass = java.type("micronaut.gcp.pubsub.bind.MessagePublishTime")


# tag::clazz[]
@Singleton  # <1>
class MessagePublishTimeAnnotationBinder(PubSubAnnotatedArgumentBinder[MessagePublishTime]):  # <2>

    def __init__(self, conversion_service: ConversionService):  # <3>
        self.conversion_service = conversion_service

    def getAnnotationType(self):
        return MessagePublishTimeClass

    def bind(self, context: ArgumentConversionContext, source: PubSubConsumerState) -> ArgumentBinder.BindingResult[object]:
        epoch_millis = Timestamps.toMillis(source.getPubsubMessage().getPublishTime())  # <4>
        return lambda: self.conversion_service.convert(epoch_millis, context)  # <5>
# end::clazz[]
