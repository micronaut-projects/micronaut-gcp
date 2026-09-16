from typing import Annotated

import java
from jakarta.inject import Named, Singleton
from micronaut.core.serialize.exceptions import SerializationException
from micronaut.core.type import Argument
from micronaut.http import MediaType
from tools.jackson.dataformat.xml import XmlMapper

# TODO(python): java.type needed because importing `io.micronaut.gcp.pubsub.serdes` collides with the Python snippet package of the same name
PubSubMessageSerDes = java.type("io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDes")


@Singleton
class XmlMessageSerDes(PubSubMessageSerDes):

    def __init__(self, xml_mapper: Annotated[XmlMapper, Named("xml")]):
        self.xml_mapper = xml_mapper

    def deserialize(self, data: bytes, type: Argument) -> object:
        try:
            return self.xml_mapper.readValue(data, type.getType())
        except Exception as e:
            raise SerializationException("Failed to deserialize PubSub message as XML", e)

    def serialize(self, data: object) -> bytes:
        try:
            return self.xml_mapper.writeValueAsBytes(data)
        except Exception as e:
            raise SerializationException("Failed to serialize PubSub message as XML", e)

    def supportedType(self) -> str:
        return MediaType.APPLICATION_XML
