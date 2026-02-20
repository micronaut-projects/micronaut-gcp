package io.micronaut.gcp.pubsub.serdes;

import io.micronaut.core.serialize.exceptions.SerializationException;
import io.micronaut.core.type.Argument;
import io.micronaut.http.MediaType;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.xml.XmlMapper;

@Singleton
public class XmlMessageSerDes implements PubSubMessageSerDes {

    private final XmlMapper xmlMapper;

    public XmlMessageSerDes(@Named("xml") XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }

    @Override
    public Object deserialize(byte[] data, Argument<?> type) {
        try {
            return xmlMapper.readValue(data, type.getType());
        } catch (JacksonException e) {
            throw new SerializationException("Failed to deserialize PubSub message as XML", e);
        }
    }

    @Override
    public byte[] serialize(Object data) {
        try {
            return xmlMapper.writeValueAsBytes(data);
        } catch (JacksonException e) {
            throw new SerializationException("Failed to serialize PubSub message as XML", e);
        }
    }

    @Override
    public String supportedType() {
        return MediaType.APPLICATION_XML;
    }
}
