package io.micronaut.gcp.pubsub.serdes

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.micronaut.core.serialize.exceptions.SerializationException
import io.micronaut.core.type.Argument
import io.micronaut.http.MediaType
import jakarta.inject.Named
import jakarta.inject.Singleton
import java.io.IOException

@Singleton
class XmlMessageSerDes(@param:Named("xml") private val xmlMapper: XmlMapper) : PubSubMessageSerDes {
    override fun deserialize(data: ByteArray, type: Argument<*>): Any {
        return try {
            xmlMapper.readValue(data, type.type)
        } catch (e: IOException) {
            throw SerializationException("Failed to deserialize PubSub message as XML", e)
        }
    }

    override fun serialize(data: Any): ByteArray {
        return try {
            xmlMapper.writeValueAsBytes(data)
        } catch (e: IOException) {
            throw SerializationException("Failed to serialize PubSub message as XML", e)
        }
    }

    override fun supportedType(): String {
        return MediaType.APPLICATION_XML
    }
}
