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
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;

import java.util.Optional;

/**
 * Maps {@link CloudEvent} data to different types.
 *
 * @author Guillermo Calvo
 * @since XXXX
 * @param <T> The target mapping type.
 */
@Internal
@FunctionalInterface
public interface CloudEventMapper<T> {

    /**
     * Maps a cloud event to the target type.
     *
     * @param event The Cloud Event
     * @return The bound type if possible, or an empty optional in case of error.
     */
    @NonNull
    Optional<T> map(@NonNull CloudEvent event);
}
