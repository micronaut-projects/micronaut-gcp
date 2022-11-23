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
package io.micronaut.cloudevents;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * {@link CloudEventMapper} implementation backed by Micronaut Serialization.
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Singleton
public class SerdeCloudEventMapper implements CloudEventMapper {
    private static final Logger LOG = LoggerFactory.getLogger(SerdeCloudEventMapper.class);
    private final ObjectMapper objectMapper;

    public SerdeCloudEventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @NonNull
    public <T> Optional<T> map(@NonNull CloudEvent event, @NonNull Class<T> type) {
        try {
            CloudEventData data = event.getData();
            if (data == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(objectMapper.readValue(data.toBytes(), type));
        } catch (IOException e) {
            LOG.error("Could not map cloud event data to {}", type.getSimpleName(), e);
        }
        return Optional.empty();
    }
}
