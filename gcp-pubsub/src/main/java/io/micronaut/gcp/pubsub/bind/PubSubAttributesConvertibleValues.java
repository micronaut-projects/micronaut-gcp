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
package io.micronaut.gcp.pubsub.bind;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Convert PubSub attributes to the requested type.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 *
 */
public class PubSubAttributesConvertibleValues implements ConvertibleValues<String> {

    private final Map<String, String> attributes;
    private final ConversionService conversionService;
    private final List<ConversionError> conversionErrors = new ArrayList<>();

    public PubSubAttributesConvertibleValues(Map<String, String> attributes, ConversionService conversionService) {
        this.attributes = attributes;
        this.conversionService = conversionService;
    }

    @Override
    public Set<String> names() {
        return attributes.keySet();
    }

    @Override
    public Collection<String> values() {
        return attributes.values();
    }

    @Override
    public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
        String value = attributes.get(name.toString());
        if (value != null) {
           Optional<T> converted = conversionService.convert(value, conversionContext);
           conversionContext.getLastError().ifPresent(conversionErrors::add);
           return converted;
        }
        return Optional.empty();
    }
}
