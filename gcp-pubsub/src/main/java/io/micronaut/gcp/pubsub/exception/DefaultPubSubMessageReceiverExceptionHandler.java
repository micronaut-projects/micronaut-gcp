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

import javax.inject.Singleton;

/**
 * Handles any exception on beans of type {@link io.micronaut.gcp.pubsub.annotation.PubSubListener} that do not
 * implement {@link PubSubMessageReceiverExceptionHandler} interface.
 * Only logs the error. Messages will not be acked and will be redelivered for as long the subscription configuration
 * is set for message retries.
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
        logger.error(String.format("Error processing message on bean %s listening for subscription: %", exception.getListener().getClass().getName(), exception.getState().getSubscriptionName()), exception);
    }
}
