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
import io.micronaut.core.type.Argument;
import io.micronaut.gcp.pubsub.annotation.MessageId;

import jakarta.inject.Singleton;
import java.util.Optional;

/**
 * Binds a PubSubMessage Id to the argument. Note that target argument must be of type {@link String} or else an exception is thrown.
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Singleton
public class PubSubMessageIdBinder implements PubSubAnnotatedArgumentBinder<MessageId> {

    @Override
    public Class<MessageId> getAnnotationType() {
        return MessageId.class;
    }

    @Override
    public BindingResult<Object> bind(ArgumentConversionContext<Object> context, PubSubConsumerState source) {
        Argument<Object> argument = context.getArgument();
        if (!argument.getType().equals(String.class)) {
            throw new IllegalArgumentException("Can't bind messageId to argument " + argument.getName() + " argument type must be a String");
        }
        return () -> Optional.ofNullable(source.getPubsubMessage().getMessageId());
    }
}
