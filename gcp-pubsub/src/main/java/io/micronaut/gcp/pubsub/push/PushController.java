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

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

/**
 * A {@link Controller} implementation for handling PubSub Push JSON messages.
 *
 * <p>
 * If push message handling is enabled, and the required HTTP dependencies are available on the classpath, this controller will
 * handle all incoming push messages via a single URL path. The default path is {@code /push}. This is the path that should be
 * configured in the GCP PubSub service.
 * </p>
 *
 * <p>
 * The incoming JSON messages contain metadata about the subscription from which they originated, and they will be routed to
 * the corresponding {@link io.micronaut.gcp.pubsub.annotation.PushSubscription} method for the subscription.
 * </p>
 *
 * @author Jeremy Grelle
 * @since 5.4.0
 */
@Requires(beans = PubSubPushConfigurationProperties.class)
@Requires(classes = { Controller.class, Validated.class })
@Validated
@Controller("${" + PubSubPushConfigurationProperties.PREFIX + ".path:" + PubSubPushConfigurationProperties.DEFAULT_PATH + "}")
public class PushController {

    private final PushSubscriberHandler handler;

    /**
     * Constructor for the push controller.
     *
     * @param handler the handler that implements processing of the incoming message
     */
    public PushController(PushSubscriberHandler handler) {
        this.handler = handler;
    }

    /**
     * Handle incoming PubSub Push messages by deserializing them from their specified JSON format and forwarding the
     * deserialized message to the configured {@link PushSubscriberHandler}. Validation is applied to the incoming message
     * to ensure that it conforms to the format specified by GCP.
     *
     * @param message the incoming pub sub push request message
     * @return an HTTP response to indicate ack or nack of the message to the PubSub service
     */
    @Post(consumes = MediaType.APPLICATION_JSON)
    public Mono<MutableHttpResponse<Object>> handlePushRequest(@Valid @Body PushRequest message) {
        return handler.handleRequest(message);
    }
}
