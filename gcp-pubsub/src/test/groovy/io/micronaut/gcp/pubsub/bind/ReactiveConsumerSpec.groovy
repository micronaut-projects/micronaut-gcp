package io.micronaut.gcp.pubsub.bind

import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.AbstractConsumerSpec
import io.micronaut.gcp.pubsub.MockPubSubEngine
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.Subscription
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.messaging.Acknowledgement
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.reactivex.rxjava3.core.Flowable
import jakarta.inject.Inject
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.util.concurrent.PollingConditions

import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

@MicronautTest
@Property(name = "spec.name", value = "ReactiveConsumerSpec")
@Property(name = "gcp.projectId", value = "test-project")
class ReactiveConsumerSpec extends AbstractConsumerSpec {

    @Inject
    TestMessagePublisher publisher;

    @Inject
    ReactiveConsumer consumer;

    @Inject
    MockPubSubEngine mockPubSubEngine

    void setup() {
        consumer.msg = null
        consumer.finished.set(false)
    }

    void verifyAck() {
        def conditions = new PollingConditions(timeout: 5);
        conditions.eventually {
            def message = consumer.msg
            assert mockPubSubEngine.acknowledgements.containsKey(message)
            assert mockPubSubEngine.acknowledgements.get(message) == MockPubSubEngine.ACK
        }
        assert consumer.finished.get()
    }

    void verifyNack() {
        def conditions = new PollingConditions(timeout: 5);
        conditions.eventually {
            def message = consumer.msg
            assert mockPubSubEngine.acknowledgements.containsKey(message)
            assert mockPubSubEngine.acknowledgements.get(message) == MockPubSubEngine.NACK
        }
        assert consumer.finished.get()
    }

    void "a message is auto-acknowledged when a returned publisher completes successfully"() {
        given:
        publisher."$publisherMethod"(payload.getBytes())

        expect:
        verifyAck()

        where:
        payload                 | publisherMethod
        "ping-mono-result"      | "publishMessageMonoResult"
        "ping-flux-result"      | "publishMessageFluxResult"
        "ping-flowable-result"  | "publishMessageFlowableResult"
    }

    void "a message is auto-nacked when a returned publisher completes with an error"() {
        given:
        publisher."$publisherMethod"(payload.getBytes())

        expect:
        verifyNack()

        where:
        payload                         | publisherMethod
        "ping-mono-result-error"        | "publishMessageMonoResultError"
        "ping-flux-result-error"        | "publishMessageFluxResultError"
        "ping-flowable-result-error"    | "publishMessageFlowableResultError"
    }

    void "a message can be manually acknowledged when returning a publisher"() {
        given:
        publisher."$publisherMethod"(payload.getBytes())

        expect:
        verifyAck()

        where:
        payload                             | publisherMethod
        "ping-mono-result-manual-ack"       | "publishMessageMonoResultManualAck"
        "ping-flux-result-manual-ack"       | "publishMessageFluxResultManualAck"
        "ping-flowable-result-manual-ack"   | "publishMessageFlowableResultManualAck"
    }

    void "a message can be manually nacked when returning a publisher"() {
        given:
        publisher."$publisherMethod"(payload.getBytes())

        expect:
        verifyNack()

        where:
        payload                             | publisherMethod
        "ping-mono-result-manual-nack"      | "publishMessageMonoResultManualNack"
        "ping-flux-result-manual-nack"      | "publishMessageFluxResultManualNack"
        "ping-flowable-result-manual-nack"  | "publishMessageFlowableResultManualNack"
    }

    void "a message can be consumed as a reactive type and auto-acknowledged"() {
        given:
        publisher."$publisherMethod"(payload.getBytes())

        expect:
        verifyAck()

        where:
        payload                             | publisherMethod
        "ping-mono-payload-and-result"      | "publishMessageMonoPayloadAndResult"
        "ping-flux-payload-and-result"      | "publishMessageFluxPayloadAndResult"
        "ping-flowable-payload-and-result"  | "publishMessageFlowablePayloadAndResult"
    }

    void "a message can be consumed as a reactive type and auto-nacked on error"() {
        given:
        publisher."$publisherMethod"(payload.getBytes())

        expect:
        verifyNack()

        where:
        payload                                     | publisherMethod
        "ping-mono-payload-and-result-error"        | "publishMessageMonoPayloadAndResultError"
        "ping-flux-payload-and-result-error"        | "publishMessageFluxPayloadAndResultError"
        "ping-flowable-payload-and-result-error"    | "publishMessageFlowablePayloadAndResultError"
    }

    void "a message can be consumed as a reactive type and  manually acknowledged"() {
        given:
        publisher."$publisherMethod"(payload.getBytes())

        expect:
        verifyAck()

        where:
        payload                                         | publisherMethod
        "ping-mono-payload-and-result-manual-ack"       | "publishMessageMonoPayloadAndResultManualAck"
        "ping-flux-payload-and-result-manual-ack"       | "publishMessageFluxPayloadAndResultManualAck"
        "ping-flowable-payload-and-result-manual-ack"   | "publishMessageFlowablePayloadAndResultManualAck"
    }

    void "a message can be consumed as a reactive type and  manually nacked"() {
        given:
        publisher."$publisherMethod"(payload.getBytes())

        expect:
        verifyNack()

        where:
        payload                                         | publisherMethod
        "ping-mono-payload-and-result-manual-nack"      | "publishMessageMonoPayloadAndResultManualNack"
        "ping-flux-payload-and-result-manual-nack"      | "publishMessageFluxPayloadAndResultManualNack"
        "ping-flowable-payload-and-result-manual-nack"  | "publishMessageFlowablePayloadAndResultManualNack"
    }
}

@Requires(property = "spec.name", value = "ReactiveConsumerSpec")
@PubSubClient
interface TestMessagePublisher {

    @Topic("mono-result") void publishMessageMonoResult(byte[] message)
    @Topic("flux-result") void publishMessageFluxResult(byte[] message)
    @Topic("flowable-result") void publishMessageFlowableResult(byte[] message)
    @Topic("mono-result-error") void publishMessageMonoResultError(byte[] message)
    @Topic("flux-result-error") void publishMessageFluxResultError(byte[] message)
    @Topic("flowable-result-error") void publishMessageFlowableResultError(byte[] message)
    @Topic("mono-result-manual-ack") void publishMessageMonoResultManualAck(byte[] message)
    @Topic("flux-result-manual-ack") void publishMessageFluxResultManualAck(byte[] message)
    @Topic("flowable-result-manual-ack") void publishMessageFlowableResultManualAck(byte[] message)
    @Topic("mono-result-manual-nack") void publishMessageMonoResultManualNack(byte[] message)
    @Topic("flux-result-manual-nack") void publishMessageFluxResultManualNack(byte[] message)
    @Topic("flowable-result-manual-nack") void publishMessageFlowableResultManualNack(byte[] message)
    @Topic("mono-payload-and-result") void publishMessageMonoPayloadAndResult(byte[] message)
    @Topic("flux-payload-and-result") void publishMessageFluxPayloadAndResult(byte[] message)
    @Topic("flowable-payload-and-result") void publishMessageFlowablePayloadAndResult(byte[] message)
    @Topic("mono-payload-and-result-error") void publishMessageMonoPayloadAndResultError(byte[] message)
    @Topic("flux-payload-and-result-error") void publishMessageFluxPayloadAndResultError(byte[] message)
    @Topic("flowable-payload-and-result-error") void publishMessageFlowablePayloadAndResultError(byte[] message)
    @Topic("mono-payload-and-result-manual-ack") void publishMessageMonoPayloadAndResultManualAck(byte[] message)
    @Topic("flux-payload-and-result-manual-ack") void publishMessageFluxPayloadAndResultManualAck(byte[] message)
    @Topic("flowable-payload-and-result-manual-ack") void publishMessageFlowablePayloadAndResultManualAck(byte[] message)
    @Topic("mono-payload-and-result-manual-nack") void publishMessageMonoPayloadAndResultManualNack(byte[] message)
    @Topic("flux-payload-and-result-manual-nack") void publishMessageFluxPayloadAndResultManualNack(byte[] message)
    @Topic("flowable-payload-and-result-manual-nack") void publishMessageFlowablePayloadAndResultManualNack(byte[] message)
}

@Requires(property = "spec.name", value = "ReactiveConsumerSpec")
@PubSubListener
class ReactiveConsumer {

    PubsubMessage msg
    AtomicBoolean finished = new AtomicBoolean(false)

    @Subscription("mono-result")
    Mono<String> onMessage1(PubsubMessage message) {
        this.msg = message
        return Mono.just(message).delayElement(Duration.ofSeconds(2)).then(Mono.just("success")).doOnTerminate { finished.set(true) }
    }

    @Subscription("flux-result")
    Flux<String> onMessage2(PubsubMessage message) {
        this.msg = message
        return Flux.just(message).thenMany(Flux.just("1", "2")).delayElements(Duration.ofSeconds(1)).doOnTerminate { finished.set(true) };
    }

    @Subscription("flowable-result")
    Flowable<String> onMessage2Rx(PubsubMessage message) {
        return Flowable.fromPublisher(onMessage2(message))
    }

    @Subscription("mono-result-error")
    Mono<String> onMessage3(PubsubMessage message) {
        this.msg = message
        return Mono.just(message).delayElement(Duration.ofSeconds(2)).then(Mono.error(new RuntimeException("Message processing error"))).doOnTerminate { finished.set(true) }
    }

    @Subscription("flux-result-error")
    Flux<String> onMessage4(PubsubMessage message) {
        this.msg = message
        return Flux.just(message).thenMany(Flux.just("1", "2")).delayElements(Duration.ofSeconds(1)).thenMany(Flux.error(new RuntimeException("Message processing error"))).doOnTerminate { finished.set(true) };
    }

    @Subscription("flowable-result-error")
    Flowable<String> onMessage4Rx(PubsubMessage message) {
        return Flowable.fromPublisher(onMessage4(message))
    }

    @Subscription("mono-result-manual-ack")
    Mono<String> onMessage5(PubsubMessage message, Acknowledgement acknowledgement) {
        this.msg = message
        return Mono.just(message).delayElement(Duration.ofSeconds(2)).then(Mono.just("success"))
                .doOnSuccess {
                    finished.set(true)
                    acknowledgement.ack()
                }
    }

    @Subscription("flux-result-manual-ack")
    Flux<String> onMessage6(PubsubMessage message, Acknowledgement acknowledgement) {
        this.msg = message
        return Flux.just(message).thenMany(Flux.just("1", "2")).delayElements(Duration.ofSeconds(1))
                .doOnComplete(() -> {
                    finished.set(true)
                    acknowledgement.ack()
                })
    }

    @Subscription("flowable-result-manual-ack")
    Flowable<String> onMessage6Rx(PubsubMessage message, Acknowledgement acknowledgement) {
        return Flowable.fromPublisher(onMessage6(message, acknowledgement))
    }

    @Subscription("mono-result-manual-nack")
    Mono<String> onMessage7(PubsubMessage message, Acknowledgement acknowledgement) {
        this.msg = message
        return Mono.just(message).delayElement(Duration.ofSeconds(2)).then(Mono.just("success")).doOnSuccess {
                    finished.set(true)
                    acknowledgement.nack()
                }
    }

    @Subscription("flux-result-manual-nack")
    Flux<String> onMessage8(PubsubMessage message, Acknowledgement acknowledgement) {
        this.msg = message
        return Flux.just(message).thenMany(Flux.just("1", "2")).delayElements(Duration.ofSeconds(1))
                .doOnComplete(() -> {
                    finished.set(true)
                    acknowledgement.nack()
                })
    }

    @Subscription("flowable-result-manual-nack")
    Flowable<String> onMessage8Rx(PubsubMessage message, Acknowledgement acknowledgement) {
        return Flowable.fromPublisher(onMessage8(message, acknowledgement))
    }

    @Subscription("mono-payload-and-result")
    Mono<String> onMessage9(Mono<PubsubMessage> message) {
        return message.doOnNext {this.msg = it }.delayElement(Duration.ofSeconds(2)).then(Mono.just("success")).doOnTerminate { finished.set(true) }
    }

    @Subscription("flux-payload-and-result")
    Flux<String> onMessage10(Flux<PubsubMessage> message) {
        return message.doOnNext {
            this.msg = it
        }.thenMany(Flux.just("1", "2")).delayElements(Duration.ofSeconds(1)).doOnTerminate { finished.set(true) };
    }

    @Subscription("flowable-payload-and-result")
    Flowable<String> onMessage10Rx(Flowable<PubsubMessage> message) {
        return Flowable.fromPublisher(onMessage10(Flux.from(message)))
    }

    @Subscription("mono-payload-and-result-error")
    Mono<String> onMessage11(Mono<PubsubMessage> message) {
        return message.doOnNext {this.msg = it }.delayElement(Duration.ofSeconds(2)).then(Mono.error(new RuntimeException("Message processing error"))).doOnTerminate { finished.set(true) }
    }

    @Subscription("flux-payload-and-result-error")
    Flux<String> onMessage12(Flux<PubsubMessage> message) {
        return message.doOnNext {
            this.msg = it
        }.thenMany(Flux.just("1", "2")).delayElements(Duration.ofSeconds(1)).thenMany(Flux.error(new RuntimeException("Message processing error"))).doOnTerminate { finished.set(true) };
    }

    @Subscription("flowable-payload-and-result-error")
    Flowable<String> onMessage12Rx(Flowable<PubsubMessage> message) {
        return Flowable.fromPublisher(onMessage12(Flux.from(message)))
    }

    @Subscription("mono-payload-and-result-manual-ack")
    Mono<String> onMessage13(Mono<PubsubMessage> message, Acknowledgement acknowledgement) {
        return message.doOnNext {this.msg = it }.delayElement(Duration.ofSeconds(2)).then(Mono.just("success"))
                .doOnSuccess {
                    finished.set(true)
                    acknowledgement.ack()
                }
    }

    @Subscription("flux-payload-and-result-manual-ack")
    Flux<String> onMessage14(Flux<PubsubMessage> message, Acknowledgement acknowledgement) {
        return message.doOnNext {this.msg = it }.thenMany(Flux.just("1", "2")).delayElements(Duration.ofSeconds(1))
                .doOnComplete {
                    finished.set(true)
                    acknowledgement.ack()
                }
    }

    @Subscription("flowable-payload-and-result-manual-ack")
    Flowable<String> onMessage14Rx(Flowable<PubsubMessage> message, Acknowledgement acknowledgement) {
        return Flowable.fromPublisher(onMessage14(Flux.from(message), acknowledgement))
    }

    @Subscription("mono-payload-and-result-manual-nack")
    Mono<String> onMessage15(Mono<PubsubMessage> message, Acknowledgement acknowledgement) {
        return message.doOnNext {this.msg = it }.delayElement(Duration.ofSeconds(2)).then(Mono.just("success"))
                .doOnSuccess {
                    finished.set(true)
                    acknowledgement.nack()
                }
    }

    @Subscription("flux-payload-and-result-manual-nack")
    Flux<String> onMessage16(Flux<PubsubMessage> message, Acknowledgement acknowledgement) {
        return message.doOnNext {this.msg = it }.thenMany(Flux.just("1", "2")).delayElements(Duration.ofSeconds(1))
                .doOnComplete {
                    finished.set(true)
                    acknowledgement.nack()
                }
    }

    @Subscription("flowable-payload-and-result-manual-nack")
    Flowable<String> onMessage16Rx(Flowable<PubsubMessage> message, Acknowledgement acknowledgement) {
        return Flowable.fromPublisher(onMessage16(Flux.from(message), acknowledgement))
    }
}
