package io.micronaut.gcp.pubsub.bind

import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.Blocking
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
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
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
    BlockingPushConsumer consumer;

    void setup() {
        consumer.msg = null
        consumer.threadId = null
        consumer.finished.set(false)
    }

    void verifyAck(String payload, HttpResponse response) {
        assert response.status() == HttpStatus.OK
        assert consumer.msg
        assert consumer.threadId.startsWith(EXECUTOR_NAME_MATCH)
        assert consumer.msg.data.toByteArray() == payload.getBytes()
        assert consumer.finished.get()
    }

    void verifyNack(HttpClientResponseException ex) {
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
        verifyAck(payload, response)

        where:
        payload                             | topic
        "ping-blocking-result"              | "blocking-result"
        "ping-blocking-result-manual-ack"   | "blocking-result-manual-ack"
    }

    void "a push message can be nacked when processed by a blocking subscriber"() {
        when:
        executePushRequest(payload, topic)

        then:
        HttpClientResponseException ex = thrown()
        verifyNack(ex)

        where:
        payload                             | topic
        "ping-blocking-result-error"        | "blocking-result-error"
        "ping-blocking-result-manual-nack"  | "blocking-result-manual-nack"
    }
}

@Requires(property = "spec.name", value = "BlockingPushConsumerSpec")
@PubSubListener
class BlockingPushConsumer {

    PubsubMessage msg
    AtomicBoolean finished = new AtomicBoolean(false)
    String threadId

    @PushSubscription("blocking-result")
    @Blocking
    void onMessage1(PubsubMessage message) {
        this.msg = message
        this.threadId = Thread.currentThread().name
        Thread.sleep(100)
        finished.set(true)
    }

    @PushSubscription("blocking-result-manual-ack")
    @Blocking
    void onMessage2(PubsubMessage message, Acknowledgement acknowledgement) {
        this.msg = message
        this.threadId = Thread.currentThread().name
        Thread.sleep(100)
        finished.set(true)
        acknowledgement.ack()
    }

    @PushSubscription("blocking-result-error")
    @Blocking
    void onMessage3(PubsubMessage message) {
        this.msg = message
        this.threadId = Thread.currentThread().name
        Thread.sleep(100)
        finished.set(true)
        throw new RuntimeException("Message processing error")
    }

    @PushSubscription("blocking-result-manual-nack")
    @Blocking
    void onMessage4(PubsubMessage message, Acknowledgement acknowledgement) {
        this.msg = message
        this.threadId = Thread.currentThread().name
        Thread.sleep(100)
        finished.set(true)
        acknowledgement.nack()
    }
}
