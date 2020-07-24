package io.micronaut.gcp.pubsub.configuration

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
                "gcp.pubsub.publisher.animals.retry.initial-retry-delay" : "10s",
                "gcp.pubsub.publisher.animals.executorThreads" : "2"])
        PublisherConfigurationProperties properties = ctx.getBean(PublisherConfigurationProperties, Qualifiers.byName("animals"))

        expect:
            properties.retrySettings.initialRetryDelay.seconds == 10
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

        ])
        Collection<SubscriberConfigurationProperties> properties = ctx.getBeansOfType(SubscriberConfigurationProperties)
        SubscriberConfigurationProperties animals = properties.stream().filter({ p -> (p.getName() == "animals") }).findFirst().get()

        expect:
            animals.maxDurationPerAckExtension.toMillis() == 100
    }
}
