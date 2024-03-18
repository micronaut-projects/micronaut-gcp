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

import io.micronaut.gcp.pubsub.bind.PubSubConsumerState;
import io.micronaut.messaging.exceptions.MessageListenerException;

/**
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
public class PubSubMessageReceiverException extends MessageListenerException {

    private final PubSubConsumerState state;
    private final Object listener;

    private final boolean autoAcknowledge;

    /**
     * Constructor for PubSubMessageReceiverException.
     *
     * <p>This exception is expected to only ever be created internally by the framework.
     *
     * @param message the exception message
     * @param bean the bean with the targeted {@link io.micronaut.gcp.pubsub.annotation.Subscription} method
     * @param state the consumer state at the time of message reception
     * @param autoAcknowledge whether the targeted {@link io.micronaut.gcp.pubsub.annotation.Subscription} method is set for auto acknowledgement.
     *                        NOTE - custom exception handlers are responsible for handling acknowledgement on their own, regardless of the value
     *                        of this property
     */
    public PubSubMessageReceiverException(String message, Object bean, PubSubConsumerState state, boolean autoAcknowledge) {
        super(message);
        this.state = state;
        this.listener = bean;
        this.autoAcknowledge = autoAcknowledge;
    }

    /**
     * Constructor for PubSubMessageReceiverException.
     *
     * <p>This exception is expected to only ever be created internally by the framework.
     *
     * @param message the exception message
     * @param cause the exception cause
     * @param bean the bean with the targeted {@link io.micronaut.gcp.pubsub.annotation.Subscription} method
     * @param state the consumer state at the time of message reception
     * @param autoAcknowledge whether the targeted {@link io.micronaut.gcp.pubsub.annotation.Subscription} method is set for auto acknowledgement.
     *                        NOTE - custom exception handlers are responsible for handling acknowledgement on their own, regardless of the value
     *                        of this property
     */
    public PubSubMessageReceiverException(String message, Throwable cause, Object bean, PubSubConsumerState state, boolean autoAcknowledge) {
        super(message, cause);
        this.state = state;
        this.listener = bean;
        this.autoAcknowledge = autoAcknowledge;
    }

    /**
     *
     * @return the bean instance annotated with @{@link io.micronaut.gcp.pubsub.annotation.PubSubListener}
     */
    public Object getListener() {
        return listener;
    }

    /**
     *
     * @return The state object associated with the subcription
     */
    public PubSubConsumerState getState() {
        return state;
    }

    /**
     *
     * @return whether the subscription is set to automatically acknowledge messages after processing
     */
    public boolean isAutoAcknowledge() {
        return autoAcknowledge;
    }
}
