/*
 * Copyright 2017-2023 original authors
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

import io.micronaut.core.serialize.exceptions.SerializationException;
import io.micronaut.core.type.Argument;
import io.micronaut.http.MediaType;
import io.micronaut.json.JsonMapper;
import jakarta.inject.Singleton;

import java.io.IOException;

/**
 * A {@link PubSubMessageSerDes} implementation that uses a {@link io.micronaut.json.JsonMapper} to convert
 * application/json mime types.
 *
 * @author Vinicius Carvalho
 * @author Dean Wette
 * @since 2.0.0
 */
@Singleton
public class JsonPubSubMessageSerDes implements PubSubMessageSerDes {

    private final JsonMapper jsonMapper;

    /**
     * Default constructor.
     * @param jsonMapper Json ObjectMapper
     */
    public JsonPubSubMessageSerDes(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Object deserialize(byte[] data, Argument<?> type) {
        try {
            return jsonMapper.readValue(data, type);
        } catch (IOException e) {
            throw new SerializationException("Error decoding JSON stream for type [" + type.getName() + "]: " + e.getMessage());
        }
    }

    @Override
    public byte[] serialize(Object data) {
        try {
            return jsonMapper.writeValueAsBytes(data);
        } catch (IOException e) {
            throw new SerializationException("Error encoding object [" + data + "] to JSON: " + e.getMessage());
        }
    }

    @Override
    public String supportedType() {
        return MediaType.APPLICATION_JSON;
    }
}
