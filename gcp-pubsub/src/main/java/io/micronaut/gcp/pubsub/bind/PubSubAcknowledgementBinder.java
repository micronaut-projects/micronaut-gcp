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
import io.micronaut.messaging.Acknowledgement;

import java.util.Optional;
import javax.inject.Singleton;

/**
 * Binds {@link Acknowledgement} arguments.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Singleton
public class PubSubAcknowledgementBinder implements PubSubTypeArgumentBinder<Acknowledgement> {

    @Override
    public Argument<Acknowledgement> argumentType() {
        return Argument.of(Acknowledgement.class);
    }

    @Override
    public BindingResult<Acknowledgement> bind(ArgumentConversionContext<Acknowledgement> context, PubSubConsumerState source) {
        Acknowledgement acknowledgement = new DefaultPubSubAcknowledgement(source.getAckReplyConsumer());
        return () -> Optional.of(acknowledgement);
    }
}
