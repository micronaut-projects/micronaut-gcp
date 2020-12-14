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
package io.micronaut.gcp.pubsublite.exception;

import io.micronaut.gcp.pubsublite.bind.PubSubLiteConsumerState;
import io.micronaut.messaging.exceptions.MessageListenerException;

/**
 * Based on {@link io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverException}.
 * @author Jacob Mims
 * @since 2.2.0
 */
public class PubSubLiteMessageReceiverException extends MessageListenerException {

    private final PubSubLiteConsumerState state;
    private final Object listener;

    public PubSubLiteMessageReceiverException(String message, Object bean, PubSubLiteConsumerState state) {
        super(message);
        this.state = state;
        this.listener = bean;
    }

    public PubSubLiteMessageReceiverException(String message, Throwable cause, Object bean, PubSubLiteConsumerState state) {
        super(message, cause);
        this.state = state;
        this.listener = bean;
    }

    /**
     *
     * @return the bean instance annotated with @{@link io.micronaut.gcp.pubsub.annotation.PubSubLiteListener}
     */
    public Object getListener() {
        return listener;
    }

    /**
     *
     * @return The state object associated with the lite subscription
     */
    public PubSubLiteConsumerState getState() {
        return state;
    }
}
