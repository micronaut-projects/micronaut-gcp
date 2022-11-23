/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.gcp.function.storage.cloudevents;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A super class that can be used to map {@link CloudEvent} objects to a different type through a delegate {@link ObjectMapper}.
 *
 * @param <T> The target mapping type.
 */
@Internal
abstract class AbstractCloudEventMapper<T> implements CloudEventMapper<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCloudEventMapper.class);

    protected final Class<T> clazz;
    protected final ObjectMapper mapper;

    /**
     * Initializes target mapping type and delegate object mapper.
     *
     * @param clazz        The target mapping type.
     * @param objectMapper The delegate object mapper.
     */
    public AbstractCloudEventMapper(Class<T> clazz, ObjectMapper objectMapper) {
        this.clazz = clazz;
        this.mapper = objectMapper;
    }

    @Override
    @NonNull
    public Optional<T> map(@NonNull CloudEvent event) {
        return Optional.ofNullable(event.getData())
            .map(CloudEventData::toBytes)
            .map(this::readValue);
    }

    @NonNull
    private T readValue(@NonNull byte[] bytes) {
        try {
            return this.mapper.readValue(bytes, this.clazz);
        } catch (IOException e) {
            LOG.error("Could not map cloud event to {}", this.clazz.getSimpleName(), e);
            return null;
        }
    }
}
