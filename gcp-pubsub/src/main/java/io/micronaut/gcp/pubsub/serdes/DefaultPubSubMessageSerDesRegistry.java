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

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Default implementation of a {@link PubSubMessageSerDesRegistry}.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Singleton
public class DefaultPubSubMessageSerDesRegistry implements PubSubMessageSerDesRegistry {

    private final Map<String, PubSubMessageSerDes> serDesRegistry;

    /**
     * @param serDes list of {@link PubSubMessageSerDes} to be injected
     */
    public DefaultPubSubMessageSerDesRegistry(PubSubMessageSerDes[] serDes) {
        this.serDesRegistry = Arrays.stream(serDes).collect(Collectors.toMap(PubSubMessageSerDes::supportedType, s -> s));
    }

    @Override
    public Optional<PubSubMessageSerDes> find(String type) {
        return Optional.ofNullable(serDesRegistry.get(type));
    }
}
