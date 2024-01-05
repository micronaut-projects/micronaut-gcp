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
import io.micronaut.gcp.pubsub.configuration.PubSubConfigurationProperties;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.validation.Validated;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@Requires(beans = PubSubConfigurationProperties.class)
@Requires(classes = { Controller.class, Validated.class })
@Validated
@Controller("${" + PubSubPushConfigurationProperties.PREFIX + ".path:" + PubSubPushConfigurationProperties.DEFAULT_PATH + "}")
public class PushController {

    private final PushSubscriberHandler handler;

    public PushController(PushSubscriberHandler handler) {
        this.handler = handler;
    }

    @Post(consumes = MediaType.APPLICATION_JSON)
    public Mono<MutableHttpResponse<Object>> handlePushRequest(@Valid @Body PushRequest message) {
        return handler.handleRequest(message);
    }
}
