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
