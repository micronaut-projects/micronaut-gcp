package io.micronaut.gcp.pubsub.push;

import io.micronaut.gcp.pubsub.bind.PubSubConsumerState;
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverException;

public class PubSubPushMessageReceiverException extends PubSubMessageReceiverException {

    public PubSubPushMessageReceiverException(String message, Object bean, PubSubConsumerState state, boolean autoAcknowledge) {
        super(message, bean, state, autoAcknowledge);
    }

    public PubSubPushMessageReceiverException(String message, Throwable cause, Object bean, PubSubConsumerState state, boolean autoAcknowledge) {
        super(message, cause, bean, state, autoAcknowledge);
    }

    public static PubSubPushMessageReceiverException from(PubSubMessageReceiverException ex) {
        return new PubSubPushMessageReceiverException(ex.getMessage(), ex.getCause(), ex.getListener(), ex.getState(), ex.isAutoAcknowledge());
    }
}
