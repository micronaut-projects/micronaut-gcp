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
package io.micronaut.gcp.pubsublite.bind;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.gcp.pubsub.bind.DefaultPubSubAcknowledgement;
import io.micronaut.messaging.Acknowledgement;

import javax.inject.Singleton;
import java.util.Optional;

/**
 * Binds {@link Acknowledgement} arguments.
 *
 * Based on {@link io.micronaut.gcp.pubsub.bind.PubSubAcknowledgementBinder}
 *
 * @author Jacob Mims
 * @since 2.2.0
 */
@Singleton
public class PubSubLiteAcknowledgementBinder implements PubSubLiteTypeArgumentBinder<Acknowledgement> {

    @Override
    public Argument<Acknowledgement> argumentType() {
        return Argument.of(Acknowledgement.class);
    }

    @Override
    public BindingResult<Acknowledgement> bind(ArgumentConversionContext<Acknowledgement> context, PubSubLiteConsumerState source) {
        Acknowledgement acknowledgement = new DefaultPubSubAcknowledgement(source.getAckReplyConsumer());
        return () -> Optional.of(acknowledgement);
    }
}
