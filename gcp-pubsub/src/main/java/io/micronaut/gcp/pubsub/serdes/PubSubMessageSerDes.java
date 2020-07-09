/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
