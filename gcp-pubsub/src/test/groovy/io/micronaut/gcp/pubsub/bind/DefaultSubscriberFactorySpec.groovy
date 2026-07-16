package io.micronaut.gcp.pubsub.bind

import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.rpc.TransportChannelProvider
import com.google.cloud.pubsub.v1.Subscriber
import com.google.pubsub.v1.ProjectSubscriptionName
import io.micronaut.context.BeanContext
import io.micronaut.core.order.Ordered
import io.micronaut.runtime.graceful.GracefulShutdownCapable
import spock.lang.Specification

class DefaultSubscriberFactorySpec extends Specification {

    void "subscriber factory participates in graceful shutdown before executors"() {
        given:
        def factory = new DefaultSubscriberFactory(
                Mock(TransportChannelProvider),
                Mock(CredentialsProvider),
                Mock(BeanContext)
        )

        expect:
        factory instanceof GracefulShutdownCapable
        factory.order == Ordered.HIGHEST_PRECEDENCE + 100
    }

    void "graceful shutdown stops subscribers once"() {
        given:
        def factory = new DefaultSubscriberFactory(
                Mock(TransportChannelProvider),
                Mock(CredentialsProvider),
                Mock(BeanContext)
        )
        def subscriptionName = ProjectSubscriptionName.of("test-project", "test-subscription")
        def subscriber = Mock(Subscriber)
        factory.@subscribers.put(subscriptionName, subscriber)

        when:
        def shutdown = factory.shutdownGracefully()
        factory.close()

        then:
        shutdown.toCompletableFuture().done
        1 * subscriber.isRunning() >> true
        1 * subscriber.stopAsync() >> subscriber
        1 * subscriber.awaitTerminated() >> subscriber
        0 * subscriber._
    }
}
