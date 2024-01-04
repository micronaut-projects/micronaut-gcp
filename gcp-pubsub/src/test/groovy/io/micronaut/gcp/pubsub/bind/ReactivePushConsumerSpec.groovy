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
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.reactivex.rxjava3.core.Flowable
import jakarta.inject.Inject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

@MicronautTest
@Property(name = "spec.name", value = "ReactivePushConsumerSpec")
@Property(name = "gcp.projectId", value = "test-project")
class ReactivePushConsumerSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient pushClient

    @Inject
    ReactivePushConsumer consumer;

    void setup() {
        consumer.msg = null
        consumer.finished.set(false)
    }

    void verifyAck(String payload, HttpResponse response) {
        assert response.status() == HttpStatus.OK
        assert consumer.msg
        assert consumer.msg.data.toByteArray() == payload.getBytes()
        assert consumer.finished.get()
    }

    void verifyNack(HttpClientResponseException ex) {
        assert ex
        assert ex.status == HttpStatus.UNPROCESSABLE_ENTITY
        assert consumer.finished.get()
    }

    HttpResponse executePushRequest(String payload, String topic) {
        String encodedData = Base64.getEncoder().encodeToString(payload.getBytes())
        PushRequest request = new PushRequest("projects/test-project/subscriptions/${topic}",
                new PushRequest.PushMessage(new HashMap<>(), encodedData, "1", "2021-02-26T19:13:55.749Z"))
        return pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))
    }

    void "a push message is auto-acknowledged when a returned publisher completes successfully"() {
        when:
        HttpResponse response = executePushRequest(payload, topic)

        then:
        verifyAck(payload, response)

        where:
        payload                 | topic
        "ping-mono-result"      | "mono-result"
        "ping-flux-result"      | "flux-result"
        "ping-flowable-result"  | "flowable-result"
    }

    void "a push message is auto-nacked when a returned publisher completes with an error"() {
        when:
        executePushRequest(payload, topic)

        then:
        HttpClientResponseException ex = thrown()
        verifyNack(ex)

        where:
        payload                         | topic
        "ping-mono-result-error"        | "mono-result-error"
        "ping-flux-result-error"        | "flux-result-error"
        "ping-flowable-result-error"    | "flowable-result-error"
    }

    void "a push message can be manually acknowledged when returning a publisher"() {
        when:
        HttpResponse response = executePushRequest(payload, topic)

        then:
        verifyAck(payload, response)

        where:
        payload                             | topic
        "ping-mono-result-manual-ack"       | "mono-result-manual-ack"
        "ping-flux-result-manual-ack"       | "flux-result-manual-ack"
        "ping-flowable-result-manual-ack"   | "flowable-result-manual-ack"
    }

    void "a push message can be manually nacked when returning a publisher"() {
        when:
        executePushRequest(payload, topic)

        then:
        HttpClientResponseException ex = thrown()
        verifyNack(ex)

        where:
        payload                             | topic
        "ping-mono-result-manual-nack"      | "mono-result-manual-nack"
        "ping-flux-result-manual-nack"      | "flux-result-manual-nack"
        "ping-flowable-result-manual-nack"  | "flowable-result-manual-nack"
    }

    void "a push message can be consumed as a reactive type and auto-acknowledged"() {
        when:
        HttpResponse response = executePushRequest(payload, topic)

        then:
        verifyAck(payload, response)

        where:
        payload                             | topic
        "ping-mono-payload-and-result"      | "mono-payload-and-result"
        "ping-flux-payload-and-result"      | "flux-payload-and-result"
        "ping-flowable-payload-and-result"  | "flowable-payload-and-result"
    }

    void "a push message can be consumed as a reactive type and auto-nacked on error"() {
        when:
        executePushRequest(payload, topic)

        then:
        HttpClientResponseException ex = thrown()
        verifyNack(ex)

        where:
        payload                                     | topic
        "ping-mono-payload-and-result-error"        | "mono-payload-and-result-error"
        "ping-flux-payload-and-result-error"        | "flux-payload-and-result-error"
        "ping-flowable-payload-and-result-error"    | "flowable-payload-and-result-error"
    }

    void "a push message can be consumed as a reactive type and  manually acknowledged"() {
        when:
        HttpResponse response = executePushRequest(payload, topic)

        then:
        verifyAck(payload, response)

        where:
        payload                                         | topic
        "ping-mono-payload-and-result-manual-ack"       | "mono-payload-and-result-manual-ack"
        "ping-flux-payload-and-result-manual-ack"       | "flux-payload-and-result-manual-ack"
        "ping-flowable-payload-and-result-manual-ack"   | "flowable-payload-and-result-manual-ack"
    }

    void "a push message can be consumed as a reactive type and  manually nacked"() {
        when:
        executePushRequest(payload, topic)

        then:
        HttpClientResponseException ex = thrown()
        verifyNack(ex)

        where:
        payload                                         | topic
        "ping-mono-payload-and-result-manual-nack"      | "mono-payload-and-result-manual-nack"
        "ping-flux-payload-and-result-manual-nack"      | "flux-payload-and-result-manual-nack"
        "ping-flowable-payload-and-result-manual-nack"  | "flowable-payload-and-result-manual-nack"
    }
}

@Requires(property = "spec.name", value = "ReactivePushConsumerSpec")
@PubSubListener
class ReactivePushConsumer {

    PubsubMessage msg
    AtomicBoolean finished = new AtomicBoolean(false)

    @PushSubscription("mono-result")
    Mono<String> onMessage1(PubsubMessage message) {
        this.msg = message
        return Mono.just(message).delayElement(Duration.ofMillis(100)).then(Mono.just("success")).doOnTerminate { finished.set(true) }
    }

    @PushSubscription("flux-result")
    Flux<String> onMessage2(PubsubMessage message) {
        this.msg = message
        return Flux.just(message).thenMany(Flux.just("1", "2")).delayElements(Duration.ofMillis(100)).doOnTerminate { finished.set(true) };
    }

    @PushSubscription("flowable-result")
    Flowable<String> onMessage2Rx(PubsubMessage message) {
        return Flowable.fromPublisher(onMessage2(message))
    }

    @PushSubscription("mono-result-error")
    Mono<String> onMessage3(PubsubMessage message) {
        this.msg = message
        return Mono.just(message).delayElement(Duration.ofMillis(100)).then(Mono.error(new RuntimeException("Message processing error"))).doOnTerminate { finished.set(true) }
    }

    @PushSubscription("flux-result-error")
    Flux<String> onMessage4(PubsubMessage message) {
        this.msg = message
        return Flux.just(message).thenMany(Flux.just("1", "2")).delayElements(Duration.ofMillis(100)).thenMany(Flux.error(new RuntimeException("Message processing error"))).doOnTerminate { finished.set(true) };
    }

    @PushSubscription("flowable-result-error")
    Flowable<String> onMessage4Rx(PubsubMessage message) {
        return Flowable.fromPublisher(onMessage4(message))
    }

    @PushSubscription("mono-result-manual-ack")
    Mono<String> onMessage5(PubsubMessage message, Acknowledgement acknowledgement) {
        this.msg = message
        return Mono.just(message).delayElement(Duration.ofMillis(100)).then(Mono.just("success"))
                .doOnSuccess {
                    finished.set(true)
                    acknowledgement.ack()
                }
    }

    @PushSubscription("flux-result-manual-ack")
    Flux<String> onMessage6(PubsubMessage message, Acknowledgement acknowledgement) {
        this.msg = message
        return Flux.just(message).thenMany(Flux.just("1", "2")).delayElements(Duration.ofMillis(100))
                .doOnComplete(() -> {
                    finished.set(true)
                    acknowledgement.ack()
                })
    }

    @PushSubscription("flowable-result-manual-ack")
    Flowable<String> onMessage6Rx(PubsubMessage message, Acknowledgement acknowledgement) {
        return Flowable.fromPublisher(onMessage6(message, acknowledgement))
    }

    @PushSubscription("mono-result-manual-nack")
    Mono<String> onMessage7(PubsubMessage message, Acknowledgement acknowledgement) {
        this.msg = message
        return Mono.just(message).delayElement(Duration.ofMillis(100)).then(Mono.just("success")).doOnSuccess {
            finished.set(true)
            acknowledgement.nack()
        }
    }

    @PushSubscription("flux-result-manual-nack")
    Flux<String> onMessage8(PubsubMessage message, Acknowledgement acknowledgement) {
        this.msg = message
        return Flux.just(message).thenMany(Flux.just("1", "2")).delayElements(Duration.ofMillis(100))
                .doOnComplete(() -> {
                    finished.set(true)
                    acknowledgement.nack()
                })
    }

    @PushSubscription("flowable-result-manual-nack")
    Flowable<String> onMessage8Rx(PubsubMessage message, Acknowledgement acknowledgement) {
        return Flowable.fromPublisher(onMessage8(message, acknowledgement))
    }

    @PushSubscription("mono-payload-and-result")
    Mono<String> onMessage9(Mono<PubsubMessage> message) {
        return message.doOnNext {this.msg = it }.delayElement(Duration.ofMillis(100)).then(Mono.just("success")).doOnTerminate { finished.set(true) }
    }

    @PushSubscription("flux-payload-and-result")
    Flux<String> onMessage10(Flux<PubsubMessage> message) {
        return message.doOnNext {
            this.msg = it
        }.thenMany(Flux.just("1", "2")).delayElements(Duration.ofMillis(100)).doOnTerminate { finished.set(true) };
    }

    @PushSubscription("flowable-payload-and-result")
    Flowable<String> onMessage10Rx(Flowable<PubsubMessage> message) {
        return Flowable.fromPublisher(onMessage10(Flux.from(message)))
    }

    @PushSubscription("mono-payload-and-result-error")
    Mono<String> onMessage11(Mono<PubsubMessage> message) {
        return message.doOnNext {this.msg = it }.delayElement(Duration.ofMillis(100)).then(Mono.error(new RuntimeException("Message processing error"))).doOnTerminate { finished.set(true) }
    }

    @PushSubscription("flux-payload-and-result-error")
    Flux<String> onMessage12(Flux<PubsubMessage> message) {
        return message.doOnNext {
            this.msg = it
        }.thenMany(Flux.just("1", "2")).delayElements(Duration.ofMillis(100)).thenMany(Flux.error(new RuntimeException("Message processing error"))).doOnTerminate { finished.set(true) };
    }

    @PushSubscription("flowable-payload-and-result-error")
    Flowable<String> onMessage12Rx(Flowable<PubsubMessage> message) {
        return Flowable.fromPublisher(onMessage12(Flux.from(message)))
    }

    @PushSubscription("mono-payload-and-result-manual-ack")
    Mono<String> onMessage13(Mono<PubsubMessage> message, Acknowledgement acknowledgement) {
        return message.doOnNext {this.msg = it }.delayElement(Duration.ofMillis(100)).then(Mono.just("success"))
                .doOnSuccess {
                    finished.set(true)
                    acknowledgement.ack()
                }
    }

    @PushSubscription("flux-payload-and-result-manual-ack")
    Flux<String> onMessage14(Flux<PubsubMessage> message, Acknowledgement acknowledgement) {
        return message.doOnNext {this.msg = it }.thenMany(Flux.just("1", "2")).delayElements(Duration.ofMillis(100))
                .doOnComplete {
                    finished.set(true)
                    acknowledgement.ack()
                }
    }

    @PushSubscription("flowable-payload-and-result-manual-ack")
    Flowable<String> onMessage14Rx(Flowable<PubsubMessage> message, Acknowledgement acknowledgement) {
        return Flowable.fromPublisher(onMessage14(Flux.from(message), acknowledgement))
    }

    @PushSubscription("mono-payload-and-result-manual-nack")
    Mono<String> onMessage15(Mono<PubsubMessage> message, Acknowledgement acknowledgement) {
        return message.doOnNext {this.msg = it }.delayElement(Duration.ofMillis(100)).then(Mono.just("success"))
                .doOnSuccess {
                    finished.set(true)
                    acknowledgement.nack()
                }
    }

    @PushSubscription("flux-payload-and-result-manual-nack")
    Flux<String> onMessage16(Flux<PubsubMessage> message, Acknowledgement acknowledgement) {
        return message.doOnNext {this.msg = it }.thenMany(Flux.just("1", "2")).delayElements(Duration.ofMillis(100))
                .doOnComplete {
                    finished.set(true)
                    acknowledgement.nack()
                }
    }

    @PushSubscription("flowable-payload-and-result-manual-nack")
    Flowable<String> onMessage16Rx(Flowable<PubsubMessage> message, Acknowledgement acknowledgement) {
        return Flowable.fromPublisher(onMessage16(Flux.from(message), acknowledgement))
    }
}
