package io.micronaut.gcp.pubsub.serdes;

import io.micronaut.core.type.Argument;

/**
 * Responsible for converstion to/from bytes into domain logic.
 * SerDes are selected based on the MimeType they support.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
public interface PubSubMessageSerDes {

    /**
     * Deserializes data into a target type.
     * @param data byte data to deserialize
     * @param type target type
     * @return
     */
    Object deserialize(byte[] data, Argument<?> type);

    /**
     * Serializes the message.
     * @param data data to serialize
     * @return byte array of serialized data
     */
    byte[] serialize(Object data);

    /**
     *
     * @return The supported mime type this SerDes is capable of hanlding
     */
    String supportedType();
}
