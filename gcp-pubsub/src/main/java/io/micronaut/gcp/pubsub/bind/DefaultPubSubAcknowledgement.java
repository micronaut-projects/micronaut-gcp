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

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import io.micronaut.messaging.Acknowledgement;
import io.micronaut.messaging.exceptions.MessageAcknowledgementException;

/**
 * Defaul implementation of {@link io.micronaut.messaging.Acknowledgement} contract.
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
public class DefaultPubSubAcknowledgement implements Acknowledgement {

    private volatile boolean clientAck = false;
    private final AckReplyConsumer ackReplyConsumer;

    public DefaultPubSubAcknowledgement(AckReplyConsumer ackReplyConsumer) {
        this.ackReplyConsumer = ackReplyConsumer;
    }

    @Override
    public void ack() throws MessageAcknowledgementException {
        this.ackReplyConsumer.ack();
        this.clientAck = true;
    }

    @Override
    public void nack() throws MessageAcknowledgementException {
        this.ackReplyConsumer.nack();
        this.clientAck = true;
    }

    /**
     *
     * @return if the client has ack/nack the message.
     */
    public boolean isClientAck() {
        return clientAck;
    }
}
