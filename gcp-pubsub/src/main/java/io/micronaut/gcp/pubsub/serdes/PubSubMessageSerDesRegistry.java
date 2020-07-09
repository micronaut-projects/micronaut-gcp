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

import io.micronaut.http.MediaType;

import java.util.Optional;

/**
 * A registry of {@link PubSubMessageSerDes} instances. Returns the
 * SerDes for the given {@link io.micronaut.http.MediaType}
 *
 * @author Vinicius Carvalho
 * @since 2.0
 */
public interface PubSubMessageSerDesRegistry {
    default Optional<PubSubMessageSerDes> find(MediaType type) {
        return find(type.getType());
    }

    Optional<PubSubMessageSerDes> find(String type);
}
