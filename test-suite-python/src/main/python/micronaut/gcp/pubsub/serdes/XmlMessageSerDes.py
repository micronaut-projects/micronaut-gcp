from typing import Annotated

from jakarta.inject import Named, Singleton
from micronaut.core.serialize.exceptions import SerializationException
from micronaut.core.type import Argument
from micronaut.gcp.pubsub.serdes import PubSubMessageSerDes
from micronaut.http import MediaType
from tools.jackson.dataformat.xml import XmlMapper


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
