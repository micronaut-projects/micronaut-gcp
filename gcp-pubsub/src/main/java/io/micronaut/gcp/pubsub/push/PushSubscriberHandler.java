package io.micronaut.gcp.pubsub.push;

import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.ProjectSubscriptionName;
import io.micronaut.http.MutableHttpResponse;
import reactor.core.publisher.Mono;

public interface PushSubscriberHandler {

    Mono<MutableHttpResponse<Object>> handleRequest(PushRequest pushRequest);

    void addSubscriber(ProjectSubscriptionName projectSubscriptionName, MessageReceiver receiver);
}
