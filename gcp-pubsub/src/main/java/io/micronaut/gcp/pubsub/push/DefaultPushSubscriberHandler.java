package io.micronaut.gcp.pubsub.push;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.gcp.pubsub.exception.PubSubListenerException;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import jakarta.inject.Singleton;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class DefaultPushSubscriberHandler implements PushSubscriberHandler {

    private final ConcurrentHashMap<ProjectSubscriptionName, MessageReceiver> receivers = new ConcurrentHashMap<>();

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
