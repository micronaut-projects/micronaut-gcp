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

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.ProjectSubscriptionName;
import io.micronaut.gcp.pubsub.exception.PubSubListenerException;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link PushSubscriberHandler}.
 *
 * @author Jeremy Grelle
 * @since 5.4.0
 */
@Singleton
public class DefaultPushSubscriberHandler implements PushSubscriberHandler {

    private final ConcurrentHashMap<ProjectSubscriptionName, MessageReceiver> receivers = new ConcurrentHashMap<>();

    /**
     * Default handling of incoming JSON push request messages.
     *
     * @param pushRequest the incoming JSON push request message
     * @return an appropriate HTTP response, with a status of {@link io.micronaut.http.HttpStatus#OK} to indicate an ack, or
     * {@link io.micronaut.http.HttpStatus#UNPROCESSABLE_ENTITY} to indicate an explicit nack to the PubSub service. Note that
     * any other error status codes that result from general errors during HTTP processing will also be interpreted as a nack by
     * the PubSub service.
     */
    @Override
    public Mono<MutableHttpResponse<Object>> handleRequest(PushRequest pushRequest) {
        ProjectSubscriptionName subscription = ProjectSubscriptionName.parse(pushRequest.subscription());
        if (receivers.containsKey(subscription)) {
            MessageReceiver receiver = receivers.get(subscription);
            Mono<AckReply> result = Mono.create(sink -> receiver.receiveMessage(pushRequest.message().asPubsubMessage(), new AckReplyConsumer() {
                @Override
                public void ack() {
                    sink.success(AckReply.ACK);
                }

                @Override
                public void nack() {
                    sink.success(AckReply.NACK);
                }
            }));
            return result.map(reply -> switch (reply) {
                case ACK -> HttpResponse.ok();
                case NACK -> HttpResponse.unprocessableEntity();
            });
        }
        return Mono.just(HttpResponse.notFound("No subscribers were found for subscription " + pushRequest.subscription()));
    }

    @Override
    public void addSubscriber(ProjectSubscriptionName projectSubscriptionName, MessageReceiver receiver) {
        receivers.compute(projectSubscriptionName, (k, v) -> {
            if (v != null) {
                throw new PubSubListenerException("Subscription %s is already registered for another method".formatted(projectSubscriptionName.toString()));
            }
            return receiver;
        });
    }

    private enum AckReply {
        ACK,
        NACK
    }
}
