package io.micronaut.gcp.pubsub.support

import com.google.api.gax.retrying.RetrySettings
import io.micronaut.context.ApplicationContext
import spock.lang.Specification

class PublisherFactorySpec extends Specification{

    void "test factory exists"() {
        given:
            ApplicationContext context = ApplicationContext.run(["spec.name" : getClass().simpleName], "test")
            context.start()
        expect: "publisher factory is available"
            context.containsBean(DefaultPublisherFactory)
        when: "a factory is available"
            PublisherFactory publisherFactory = context.getBean(PublisherFactory)

        then: "A publisher should be returned"
            publisherFactory.createPublisher("foo")
    }

    void "custom retry settings is available"() {
        given:
            ApplicationContext context = ApplicationContext.run([
                    "gcp.pubsub.keepAliveIntervalMinutes" : 2,
                    "gcp.pubsub.publisher.retry.maxAttempts" : 3
            ], "test")
            context.start()
        expect: "retrySettings bean to be present"
            context.containsBean(RetrySettings)
        when: "retrySettings is available"
            RetrySettings retrySettings = context.getBean(RetrySettings)
        then:
            retrySettings != null

    }
}
