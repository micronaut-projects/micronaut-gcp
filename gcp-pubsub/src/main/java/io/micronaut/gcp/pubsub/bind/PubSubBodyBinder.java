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

import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.gcp.pubsub.exception.PubSubListenerException;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDes;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDesRegistry;

import io.micronaut.messaging.annotation.MessageBody;
import jakarta.inject.Singleton;
import java.util.Optional;

/**
 * Binds arguments annotated with {@link io.micronaut.messaging.annotation.MessageBody} and uses the appropriate
 * {@link io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDes} to deserialize the contents of the PubSubMessage data.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Singleton
public class PubSubBodyBinder implements PubSubAnnotatedArgumentBinder<MessageBody> {

    private final PubSubMessageSerDesRegistry serDesRegistry;

    public PubSubBodyBinder(PubSubMessageSerDesRegistry serDesRegistry) {
        this.serDesRegistry = serDesRegistry;
    }

    @Override
    public Class<MessageBody> getAnnotationType() {
        return MessageBody.class;
    }

    @Override
    public BindingResult<Object> bind(ArgumentConversionContext<Object> context, PubSubConsumerState state) {
        Argument<Object> bodyType = context.getArgument();
        Object result = null;
        if (bodyType.getType().equals(byte[].class)) {
            result = state.getPubsubMessage().getData().toByteArray();
        } else if (bodyType.getType().equals(PubsubMessage.class)) {
            result = state.getPubsubMessage();
        } else {
            if (StringUtils.isEmpty(state.getContentType()) && !state.getPubsubMessage().containsAttributes("Content-Type")) {
                throw  new PubSubListenerException("Could not detect Content-Type header at message and no Content-Type specified on method.");
            }
            PubSubMessageSerDes serDes = serDesRegistry.find(state.getContentType())
                    .orElseThrow(() -> new PubSubListenerException("Could not locate a valid SerDes implementation for type: " + state.getContentType()));
            result = serDes.deserialize(state.getPubsubMessage().getData().toByteArray(), bodyType);
        }
        Object finalResult = result;
        return () -> Optional.ofNullable(finalResult);
    }
}
