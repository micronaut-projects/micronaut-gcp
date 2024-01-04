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
