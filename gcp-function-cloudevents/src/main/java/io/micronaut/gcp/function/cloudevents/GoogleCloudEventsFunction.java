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
package io.micronaut.gcp.function.cloudevents;

import com.google.cloud.functions.CloudEventsFunction;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventContext;
import io.cloudevents.CloudEventData;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.gcp.function.GoogleFunctionInitializer;
import io.micronaut.serde.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * A super class that extends {@link GoogleFunctionInitializer} and can be used to map cloud events payloads. To be used with CloudEvents POJOs issued by Google.
 * @see <a href="https://github.com/googleapis/google-cloudevents-java">Google Cloud Events</a>.
 *
 * @author Guillermo Calvo
 * @since 4.7.0
 * @param <T> Google Cloud Events Type
 */
public abstract class GoogleCloudEventsFunction<T> extends GoogleFunctionInitializer implements CloudEventsFunction {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleCloudEventsFunction.class);
    private final ObjectMapper objectMapper;
    private final Class<T> type = initTypeArgument(0);

    protected GoogleCloudEventsFunction() {
        objectMapper = applicationContext.getBean(ObjectMapper.class);
    }

    @Override
    public void accept(CloudEvent event) throws Exception {
        accept(event, map(event, getType()).orElse(null));
    }

    /**
     * Called to service an incoming event.
     * If this method throws any exception (including any {@link Error}) then the HTTP response will have a 500 status code.
     *
     * @param context Cloud Event context
     * @param data Google Cloud Event
     * @throws Exception to produce a 500 status code in the HTTP response.
     */
    protected abstract void accept(@NonNull CloudEventContext context, @Nullable T data) throws Exception;

    /**
     *
     * @return Google Cloud Event Type.
     */
    @NonNull
    protected Class<T> getType() {
        return this.type;
    }

    /**
     * Maps a cloud event to the target type.
     *
     * @param event The Cloud Event
     * @param type The target mapping type.
     * @return The bound type if possible, or an empty optional in case of error.
     */
    @NonNull
    protected Optional<T> map(@NonNull CloudEvent event, @NonNull Class<T> type) {
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

    private Class initTypeArgument(int index) {
        final Class[] args = GenericTypeUtils.resolveSuperTypeGenericArguments(
            getClass(),
            GoogleCloudEventsFunction.class
        );
        if (ArrayUtils.isNotEmpty(args) && args.length > index) {
            return args[index];
        } else {
            return Object.class;
        }
    }
}
