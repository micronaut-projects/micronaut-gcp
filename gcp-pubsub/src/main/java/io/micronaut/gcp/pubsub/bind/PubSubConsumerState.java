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
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;

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
    private final AckReplyConsumer ackReplyConsumer;
    private final ProjectSubscriptionName subscriptionName;
    private final String contentType;

    public PubSubConsumerState(PubsubMessage pubsubMessage, AckReplyConsumer ackReplyConsumer,
                               ProjectSubscriptionName subscriptionName, String contentType) {
        this.pubsubMessage = pubsubMessage;
        this.ackReplyConsumer = ackReplyConsumer;
        this.subscriptionName = subscriptionName;
        this.contentType = contentType;
    }

    /**
     *
     * @return Content-Type associated to this context
     */
    public String getContentType() {
        return contentType;
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
     * @return The {@link AckReplyConsumer} to be used for message ack.
     */
    public AckReplyConsumer getAckReplyConsumer() {
        return ackReplyConsumer;
    }

    /**
     *
     * @return Subscription name.
     */
    public ProjectSubscriptionName getSubscriptionName() {
        return subscriptionName;
    }
}
