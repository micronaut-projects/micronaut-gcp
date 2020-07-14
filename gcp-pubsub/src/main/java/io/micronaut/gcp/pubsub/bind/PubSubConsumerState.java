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
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDes;

/**
 * Stores the context of a PubSubMessage that is received. Contains all the necessary information
 * required for the proper deserialization of the original {@link com.google.pubsub.v1.PubsubMessage}
 * payload into the corresponding {@link io.micronaut.inject.ExecutableMethod} arguments.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
public class PubSubConsumerState {

    private final PubsubMessage pubsubMessage;
    private final PubSubMessageSerDes serDes;
    private final AckReplyConsumer ackReplyConsumer;

    public PubSubConsumerState(PubsubMessage pubsubMessage, PubSubMessageSerDes serDes, AckReplyConsumer ackReplyConsumer) {
        this.pubsubMessage = pubsubMessage;
        this.serDes = serDes;
        this.ackReplyConsumer = ackReplyConsumer;
    }

    /**
     *
     * @return the original {@link PubsubMessage} attached to this context
     */
    public PubsubMessage getPubsubMessage() {
        return pubsubMessage;
    }

    /**
     *
     * @return Specific {@link PubSubMessageSerDes} that can handle serialization to the target type
     */
    public PubSubMessageSerDes getSerDes() {
        return serDes;
    }

    /**
     *
     * @return The {@link AckReplyConsumer} to be used for message ack.
     */
    public AckReplyConsumer getAckReplyConsumer() {
        return ackReplyConsumer;
    }
}
