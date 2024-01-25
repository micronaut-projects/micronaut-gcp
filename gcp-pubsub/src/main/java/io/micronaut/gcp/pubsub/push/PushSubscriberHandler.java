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

import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.ProjectSubscriptionName;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.MutableHttpResponse;
import org.reactivestreams.Publisher;

/**
 * Handles incoming {@link PushRequest} messages.
 *
 * @author Jeremy Grelle
 * @since 5.4.0
 */
public interface PushSubscriberHandler {

    /**
     * Handle the incoming {@link PushRequest} received via HTTP request processing. Implementations shall return
     * an appropriate HTTP status code to signal either ack or nack to the PubSub service.
     *
     * @param pushRequest the incoming JSON push request message
     * @return the HTTP response
     */
    @NonNull
    @SingleResult
    Publisher<MutableHttpResponse<Object>> handleRequest(@NonNull PushRequest pushRequest);

    /**
     * Configure a {@link MessageReceiver} for the given subscription.
     *
     * @param projectSubscriptionName the subscription name
     * @param receiver the message receiver to bind to the subscription
     */
    void addSubscriber(@NonNull ProjectSubscriptionName projectSubscriptionName, @NonNull MessageReceiver receiver);
}
