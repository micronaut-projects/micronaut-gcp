package io.micronaut.gcp.pubsub.configuration

import com.google.api.gax.batching.FlowControlSettings
import io.micronaut.context.ApplicationContext
import io.micronaut.inject.qualifiers.Qualifiers
import spock.lang.Specification



class PubSubConfigurationSpec extends Specification {

    void "test configuration binding"() {
        ApplicationContext ctx = ApplicationContext.run([
                "gcp.pubsub.keepAliveIntervalMinutes" : "10"])
        PubSubConfigurationProperties properties = ctx.getBean(PubSubConfigurationProperties)

        expect:
        properties.keepAliveIntervalMinutes == 10
    }

    void "test publisher configuration binding"() {
        ApplicationContext ctx = ApplicationContext.run([
                "gcp.pubsub.publisher.animals.retry.initial-retry-delay" : "30s",
                "gcp.pubsub.publisher.animals.flow-control.maxOutstandingElementCount" : 100,
                "gcp.pubsub.publisher.animals.flow-control.maxOutstandingRequestBytes" : 1024,
                "gcp.pubsub.publisher.animals.batching.elementCountThreshold" : "1"
                ])
        PublisherConfigurationProperties animals = ctx.getBean(PublisherConfigurationProperties, Qualifiers.byName("animals"))

        expect:
            animals.retrySettings.initialRetryDelay.seconds == 30
            animals.flowControlSettings.maxOutstandingElementCount == 100
            animals.flowControlSettings.maxOutstandingRequestBytes == 1024L

    }

    void "test multiple publisher configurations"() {
        ApplicationContext ctx = ApplicationContext.run([
                "gcp.pubsub.publisher.animals.retry.initial-retry-delay" : "10s",
                "gcp.pubsub.publisher.cars.retry.initial-retry-delay" : "20s",
        ])
        Collection<PublisherConfigurationProperties> properties = ctx.getBeansOfType(PublisherConfigurationProperties)
        PublisherConfigurationProperties animals = properties.stream().filter({ p -> (p.getName() == "animals") }).findFirst().get()
        expect:
            properties.size() == 2
            animals.retrySettings.initialRetryDelay.seconds == 10

    }

    void "test subscriber configuration binding"() {
        ApplicationContext ctx = ApplicationContext.run([
                "gcp.pubsub.subscriber.animals.maxDurationPerAckExtension" : "100ms",
                "gcp.pubsub.subscriber.animals.flow-control.maxOutstandingElementCount" : 100,
                "gcp.pubsub.subscriber.animals.flow-control.maxOutstandingRequestBytes" : 1024

        ])
        Collection<SubscriberConfigurationProperties> properties = ctx.getBeansOfType(SubscriberConfigurationProperties)
        SubscriberConfigurationProperties animals = properties.stream().filter({ p -> (p.getName() == "animals") }).findFirst().get()
        FlowControlSettings settings = FlowControlSettings.newBuilder().build()
        expect:
            animals.maxDurationPerAckExtension.toMillis() == 100
            animals.flowControlSettings.maxOutstandingElementCount == 100
            animals.flowControlSettings.maxOutstandingRequestBytes == 1024L

    }
}
