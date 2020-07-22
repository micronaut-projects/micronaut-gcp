package io.micronaut.gcp.pubsub.configuration

import io.micronaut.context.ApplicationContext
import spock.lang.Specification

import java.time.Duration

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
                "gcp.pubsub.publisher.retry.initial-retry-delay" : "10s",
                "gcp.pubsub.publisher.executorThreads" : "2"])
        PublisherConfigurationProperties properties = ctx.getBean(PublisherConfigurationProperties)

        expect:
        properties.retrySettings.initialRetryDelay.seconds == 10
        properties.executorThreads == 2
    }

}
