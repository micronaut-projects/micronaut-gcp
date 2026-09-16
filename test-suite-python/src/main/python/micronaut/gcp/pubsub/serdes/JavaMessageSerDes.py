# tag::imports[]
import java
from jakarta.inject import Singleton
from java.io import ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream
from micronaut.core.serialize.exceptions import SerializationException
from micronaut.core.type import Argument

# TODO(python): java.type needed because importing `io.micronaut.gcp.pubsub.serdes` collides with the Python snippet package of the same name
PubSubMessageSerDes = java.type("io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDes")
# end::imports[]


# tag::clazz[]
@Singleton  # <1>
class JavaMessageSerDes(PubSubMessageSerDes):

    def supportedType(self) -> str:  # <2>
        return "application/x.java"

    def deserialize(self, data: bytes, type: Argument) -> object:
        bin = ByteArrayInputStream(data)
        try:
            reader = ObjectInputStream(bin)
            return reader.readObject()
        except Exception as e:
            raise SerializationException("Failed to deserialize object", e)

    def serialize(self, data: object) -> bytes:
        baos = ByteArrayOutputStream()
        try:
            writer = ObjectOutputStream(baos)
            writer.writeObject(data)
        except Exception as e:
            raise SerializationException("Failed to serialize object", e)
        return baos.toByteArray()
# end::clazz[]
