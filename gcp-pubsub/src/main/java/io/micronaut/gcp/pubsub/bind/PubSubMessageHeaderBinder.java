/*
 * Copyright 2017-2021 original authors
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
import io.micronaut.messaging.annotation.MessageHeader;

import jakarta.inject.Singleton;

/**
 * Support the new {@link MessageHeader} annotation.
 *
 * @author graemerocher
 * @since 3.5.0
 */
@Singleton
public class PubSubMessageHeaderBinder implements PubSubAnnotatedArgumentBinder<MessageHeader> {
    private final PubSubHeaderBinder pubSubHeaderBinder;

    public PubSubMessageHeaderBinder(PubSubHeaderBinder pubSubHeaderBinder) {
        this.pubSubHeaderBinder = pubSubHeaderBinder;
    }

    @Override
    public Class<MessageHeader> getAnnotationType() {
        return MessageHeader.class;
    }

    @Override
    public BindingResult<Object> bind(ArgumentConversionContext<Object> context, PubSubConsumerState source) {
        return pubSubHeaderBinder.bind(context, source);
    }
}
