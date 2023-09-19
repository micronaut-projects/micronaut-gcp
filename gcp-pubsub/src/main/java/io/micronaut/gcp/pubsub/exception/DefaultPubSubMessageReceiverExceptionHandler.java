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
package io.micronaut.gcp.pubsub.exception;

import io.micronaut.context.annotation.Primary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;

/**
 * Handles any exception on beans of type {@link io.micronaut.gcp.pubsub.annotation.PubSubListener} that do not
 * implement {@link PubSubMessageReceiverExceptionHandler} interface.
 * <p>
 * Logs the error, and explicitly sends a nack signal (if the targeted {@link io.micronaut.gcp.pubsub.annotation.Subscription} has not declared an
 * {@link io.micronaut.messaging.Acknowledgement} parameter to indicate manually handled acknowledgement) so that PubSub will immediately process the
 * failure and attempt re-delivery if the subscription is so configured.
 * <p>
 * For {@link io.micronaut.gcp.pubsub.annotation.Subscription} methods that do manually handle acknowledgement, the user should provide a custom
 * implementation of {@link PubSubMessageReceiverExceptionHandler}.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Singleton
@Primary
public class DefaultPubSubMessageReceiverExceptionHandler implements PubSubMessageReceiverExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(DefaultPubSubMessageReceiverExceptionHandler.class);

    @Override
    public void handle(PubSubMessageReceiverException exception) {
        logger.error(String.format("Error processing message on bean %s listening for subscription: %s", exception.getListener().getClass().getName(), exception.getState().getSubscriptionName()), exception);
        if (exception.isAutoAcknowledge()) {
            exception.getState().getAckReplyConsumer().nack();
        }
    }
}
