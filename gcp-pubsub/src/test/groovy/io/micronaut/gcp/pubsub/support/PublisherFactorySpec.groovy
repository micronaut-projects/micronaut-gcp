package io.micronaut.gcp.pubsub.support

import com.google.api.core.SettableApiFuture
import com.google.api.gax.retrying.RetrySettings
import com.google.cloud.pubsub.v1.Publisher
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Replaces
import io.micronaut.gcp.pubsub.DataHolder
import spock.lang.Specification

class PublisherFactorySpec extends Specification {

    void "test factory exists"() {
        given:
            ApplicationContext context = ApplicationContext.run(["spec.name" : getClass().simpleName, "gcp.projectId" : "test-project"], "test")
            context.start()
        expect: "publisher factory is available"
            context.containsBean(PublisherFactory)
        when: "a factory is available"
            PublisherFactory publisherFactory = context.getBean(PublisherFactory)

        then: "A publisher should be returned"
            publisherFactory.createPublisher("foo")
    }

    void "custom retry settings is available"() {
        given:
            ApplicationContext context = ApplicationContext.run([
                    "gcp.pubsub.keepAliveIntervalMinutes" : 2,
                    "gcp.pubsub.publisher.retry.maxAttempts" : 3,
                    "gcp.projectId" : "test-project"
            ], "test")
            context.start()
        expect: "retrySettings bean to be present"
            context.containsBean(RetrySettings)
        when: "retrySettings is available"
            RetrySettings retrySettings = context.getBean(RetrySettings)
        then:
            retrySettings != null

    }

    @Replaces(PublisherFactory)
    PublisherFactory publisherFactory() {
        def factory = Mock(PublisherFactory)
        def publisher = Mock(Publisher)
        def future = new SettableApiFuture<String>()
        future.set("1234")
        publisher.publish(_) >> { PubsubMessage message -> DataHolder.getInstance().setData(message); return future; }
        factory.createPublisher(_) >> publisher
        return factory
    }
}
