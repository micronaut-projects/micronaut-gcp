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
import io.micronaut.core.convert.ConversionService;
import io.micronaut.messaging.annotation.MessageHeader;
import jakarta.inject.Singleton;
import java.util.Map;
import java.util.Optional;

/**
 * Binds an argument annotated with {@link io.micronaut.messaging.annotation.MessageHeader} annotation. The target type
 * must be supported by the {@link io.micronaut.core.convert.ConversionService}. PubSub attributes are always of type
 * String.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Singleton
public class PubSubHeaderBinder implements PubSubAnnotatedArgumentBinder<MessageHeader> {

    private final ConversionService conversionService;

    public PubSubHeaderBinder(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Class<MessageHeader> getAnnotationType() {
        return MessageHeader.class;
    }

    @Override
    public BindingResult<Object> bind(ArgumentConversionContext<Object> context, PubSubConsumerState source) {
        String parameterName = context.getAnnotationMetadata()
                .stringValue(MessageHeader.class)
                .orElse(context.getArgument().getName());

        Map<String, String> attributes = source.getPubsubMessage().getAttributesMap();
        PubSubAttributesConvertibleValues convertibleValues = new PubSubAttributesConvertibleValues(attributes, conversionService);
        Optional<Object> header = convertibleValues.get(parameterName, context);
        return () -> header;
    }
}
