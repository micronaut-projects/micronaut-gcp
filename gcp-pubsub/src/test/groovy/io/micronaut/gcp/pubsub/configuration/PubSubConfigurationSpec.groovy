package io.micronaut.gcp.pubsub.configuration

import io.micronaut.context.ApplicationContext
import spock.lang.Specification

import java.time.Duration

class PubSubConfigurationSpec extends Specification {

    void "test configuration binding"() {
        ApplicationContext ctx = ApplicationContext.run(["gcp.pubsub.publisher.retry.initialRetryDelay" : "10s",
                "gcp.pubsub.publisher.executorThreads" : "2",
                "gcp.pubsub.keepAliveIntervalMinutes" : "10"])
        PubSubConfigurationProperties properties = ctx.getBean(PubSubConfigurationProperties)

        expect:
            //properties.publisher.retrySettings.initialRetryDelay.equals(Duration.ofSeconds(10L))
            properties.publisher.executorThreads == 2
    }

}
