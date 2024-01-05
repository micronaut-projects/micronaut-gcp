/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.gcp.pubsub.push;

import io.micronaut.gcp.pubsub.bind.PubSubConsumerState;
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverException;

/**
 * An exception thrown if there is an error during PubSub push message processing.
 *
 * @author Jeremy Grelle
 * @since 5.4.0
 */
public class PubSubPushMessageReceiverException extends PubSubMessageReceiverException {

    private PubSubPushMessageReceiverException(String message, Throwable cause, Object bean, PubSubConsumerState state, boolean autoAcknowledge) {
        super(message, cause, bean, state, autoAcknowledge);
    }

    /**
     * Creates a {@code PubSubPushMessageReceiverException} from a general {@link PubSubMessageReceiverException} to indicate the error occurred
     * while processing a push message.
     *
     * @param ex the original exception
     * @return a {@code PubSubPushMessageReceiverException}
     */
    public static PubSubPushMessageReceiverException from(PubSubMessageReceiverException ex) {
        return new PubSubPushMessageReceiverException(ex.getMessage(), ex.getCause(), ex.getListener(), ex.getState(), ex.isAutoAcknowledge());
    }
}
