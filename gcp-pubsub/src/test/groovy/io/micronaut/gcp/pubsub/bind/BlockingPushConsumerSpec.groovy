package io.micronaut.gcp.pubsub.bind

import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.PushSubscription
import io.micronaut.gcp.pubsub.push.PushRequest
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.messaging.Acknowledgement
import io.micronaut.scheduling.LoomSupport
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.atomic.AtomicBoolean

@MicronautTest
@Property(name = "spec.name", value = "BlockingPushConsumerSpec")
@Property(name = "gcp.projectId", value = "test-project")
class BlockingPushConsumerSpec extends Specification {

    static final String EXECUTOR_NAME_MATCH = "%s-executor".formatted(LoomSupport.supported ? TaskExecutors.VIRTUAL : TaskExecutors.IO)

    @Inject
    @Client("/")
    HttpClient pushClient

    @Inject
    @Shared
    BlockingPushConsumer blockingMethodConsumer

    @Inject
    @Shared
    BlockingClassPushConsumer blockingClassConsumer

    void setup() {
        blockingMethodConsumer.msg = null
        blockingMethodConsumer.threadId = null
        blockingMethodConsumer.finished.set(false)
        blockingClassConsumer.msg = null
        blockingClassConsumer.threadId = null
        blockingClassConsumer.finished.set(false)
    }

    void verifyAck(String payload, HttpResponse response, BlockingPushSpecConsumer consumer) {
        assert response.status() == HttpStatus.OK
        assert consumer.msg
        assert consumer.threadId.startsWith(EXECUTOR_NAME_MATCH)
        assert consumer.msg.data.toByteArray() == payload.getBytes()
        assert consumer.finished.get()
    }

    void verifyNack(HttpClientResponseException ex, BlockingPushSpecConsumer consumer) {
        assert ex
        assert ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        assert consumer.threadId.startsWith(EXECUTOR_NAME_MATCH)
        assert consumer.finished.get()
    }

    HttpResponse executePushRequest(String payload, String topic) {
        String encodedData = Base64.getEncoder().encodeToString(payload.getBytes())
        PushRequest request = new PushRequest("projects/test-project/subscriptions/${topic}",
                new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))
        return pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))
    }

    void "a push message can be successfully processed by a blocking subscriber"() {
        when:
        HttpResponse response = executePushRequest(payload, topic)

        then:
        verifyAck(payload, response, consumer)

        where:
        payload                             | topic                                 | consumer
        "ping-blocking-result"              | "blocking-result"                     | blockingMethodConsumer
        "ping-blocking-result-manual-ack"   | "blocking-result-manual-ack"          | blockingMethodConsumer
        "ping-blocking-result"              | "blocking-class-result"               | blockingClassConsumer
        "ping-blocking-result-manual-ack"   | "blocking-class-result-manual-ack"    | blockingClassConsumer
    }

    void "a push message can be nacked when processed by a blocking subscriber"() {
        when:
        executePushRequest(payload, topic)

        then:
        HttpClientResponseException ex = thrown()
        verifyNack(ex, consumer)

        where:
        payload                             | topic                                 | consumer
        "ping-blocking-result-error"        | "blocking-result-error"               | blockingMethodConsumer
        "ping-blocking-result-manual-nack"  | "blocking-result-manual-nack"         | blockingMethodConsumer
        "ping-blocking-result-error"        | "blocking-class-result-error"         | blockingClassConsumer
        "ping-blocking-result-manual-nack"  | "blocking-class-result-manual-nack"   | blockingClassConsumer
    }
}

class BlockingPushSpecConsumer {
    PubsubMessage msg
    AtomicBoolean finished = new AtomicBoolean(false)
    String threadId
}

@Requires(property = "spec.name", value = "BlockingPushConsumerSpec")
@PubSubListener
class BlockingPushConsumer extends BlockingPushSpecConsumer {

    @PushSubscription("blocking-result")
    @ExecuteOn(TaskExecutors.BLOCKING)
    void onMessage1(PubsubMessage message) {
        this.msg = message
        this.threadId = Thread.currentThread().name
        Thread.sleep(100)
        finished.set(true)
    }

    @PushSubscription("blocking-result-manual-ack")
    @ExecuteOn(TaskExecutors.BLOCKING)
    void onMessage2(PubsubMessage message, Acknowledgement acknowledgement) {
        this.msg = message
        this.threadId = Thread.currentThread().name
        Thread.sleep(100)
        finished.set(true)
        acknowledgement.ack()
    }

    @PushSubscription("blocking-result-error")
    @ExecuteOn(TaskExecutors.BLOCKING)
    void onMessage3(PubsubMessage message) {
        this.msg = message
        this.threadId = Thread.currentThread().name
        Thread.sleep(100)
        finished.set(true)
        throw new RuntimeException("Message processing error")
    }

    @PushSubscription("blocking-result-manual-nack")
    @ExecuteOn(TaskExecutors.BLOCKING)
    void onMessage4(PubsubMessage message, Acknowledgement acknowledgement) {
        this.msg = message
        this.threadId = Thread.currentThread().name
        Thread.sleep(100)
        finished.set(true)
        acknowledgement.nack()
    }
}

@Requires(property = "spec.name", value = "BlockingPushConsumerSpec")
@PubSubListener
@ExecuteOn(TaskExecutors.BLOCKING)
class BlockingClassPushConsumer extends BlockingPushSpecConsumer {

    PubsubMessage msg
    AtomicBoolean finished = new AtomicBoolean(false)
    String threadId

    @PushSubscription("blocking-class-result")
    void onMessage1(PubsubMessage message) {
        this.msg = message
        this.threadId = Thread.currentThread().name
        Thread.sleep(100)
        finished.set(true)
    }

    @PushSubscription("blocking-class-result-manual-ack")
    void onMessage2(PubsubMessage message, Acknowledgement acknowledgement) {
        this.msg = message
        this.threadId = Thread.currentThread().name
        Thread.sleep(100)
        finished.set(true)
        acknowledgement.ack()
    }

    @PushSubscription("blocking-class-result-error")
    void onMessage3(PubsubMessage message) {
        this.msg = message
        this.threadId = Thread.currentThread().name
        Thread.sleep(100)
        finished.set(true)
        throw new RuntimeException("Message processing error")
    }

    @PushSubscription("blocking-class-result-manual-nack")
    void onMessage4(PubsubMessage message, Acknowledgement acknowledgement) {
        this.msg = message
        this.threadId = Thread.currentThread().name
        Thread.sleep(100)
        finished.set(true)
        acknowledgement.nack()
    }
}
