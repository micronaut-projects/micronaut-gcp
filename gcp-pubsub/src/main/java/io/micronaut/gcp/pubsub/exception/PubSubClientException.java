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

import io.micronaut.messaging.exceptions.MessagingClientException;

/**
 * Represents an error when publishing messages.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
public class PubSubClientException extends MessagingClientException {

    /**
     * Creates a new exception.
     *
     * @param message The message
     */
    public PubSubClientException(String message) {
        super(message);
    }

    /**
     * Creates a new exception.
     *
     * @param message The message
     * @param cause The cause
     */
    public PubSubClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
