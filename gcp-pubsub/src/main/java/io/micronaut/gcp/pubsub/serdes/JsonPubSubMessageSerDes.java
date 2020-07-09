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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.micronaut.core.serialize.exceptions.SerializationException;
import io.micronaut.core.type.Argument;
import io.micronaut.http.MediaType;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A {@link PubSubMessageSerDes} implementation that uses Jackson {@link com.fasterxml.jackson.databind.ObjectMapper} to convert
 * application/json mime types.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Singleton
public class JsonPubSubMessageSerDes implements PubSubMessageSerDes {

    private final ObjectMapper mapper;

    /**
     * Default constructor.
     * @param mapper Jackson ObjectMapper
     */
    public JsonPubSubMessageSerDes(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Object deserialize(byte[] data, Argument<?> type) {
        try {
            if (type.hasTypeVariables()) {
                JavaType javaType = constructJavaType(type);
                return mapper.readValue(data, javaType);
            } else {
                return mapper.readValue(data, type.getType());
            }
        } catch (IOException e) {
            throw new SerializationException("Error decoding JSON stream for type [" + type.getName() + "]: " + e.getMessage());
        }
    }

    @Override
    public byte[] serialize(Object data) {
        try {
            return mapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Error encoding object [" + data + "] to JSON: " + e.getMessage());
        }
    }

    @Override
    public String supportedType() {
        return MediaType.APPLICATION_JSON;
    }

    private <T> JavaType constructJavaType(Argument<T> type) {
        Map<String, Argument<?>> typeVariables = type.getTypeVariables();
        TypeFactory typeFactory = mapper.getTypeFactory();
        JavaType[] objects = toJavaTypeArray(typeFactory, typeVariables);
        return typeFactory.constructParametricType(
                type.getType(),
                objects
        );
    }

    private JavaType[] toJavaTypeArray(TypeFactory typeFactory, Map<String, Argument<?>> typeVariables) {
        List<JavaType> javaTypes = new ArrayList<>();
        for (Argument<?> argument : typeVariables.values()) {
            if (argument.hasTypeVariables()) {
                javaTypes.add(typeFactory.constructParametricType(argument.getType(), toJavaTypeArray(typeFactory, argument.getTypeVariables())));
            } else {
                javaTypes.add(typeFactory.constructType(argument.getType()));
            }
        }
        return javaTypes.toArray(new JavaType[0]);
    }
}
