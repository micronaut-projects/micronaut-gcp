package io.micronaut.gcp.pubsub.bind

import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.rpc.TransportChannelProvider
import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsub.v1.Subscriber
import com.google.pubsub.v1.ProjectSubscriptionName
import io.micronaut.context.BeanContext
import io.micronaut.core.order.Ordered
import io.micronaut.gcp.pubsub.exception.PubSubListenerException
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

    void "running subscribers are reported as active tasks"() {
        given:
        def factory = new DefaultSubscriberFactory(
                Mock(TransportChannelProvider),
                Mock(CredentialsProvider),
                Mock(BeanContext)
        )
        factory.@subscribers.put(ProjectSubscriptionName.of("test-project", "running"), Stub(Subscriber) { isRunning() >> true })
        factory.@subscribers.put(ProjectSubscriptionName.of("test-project", "stopped"), Stub(Subscriber) { isRunning() >> false })

        expect:
        factory.reportActiveTasks() == OptionalLong.of(1)
    }

    void "no subscriber can be created once shutdown has started"() {
        given:
        def factory = new DefaultSubscriberFactory(
                Mock(TransportChannelProvider),
                Mock(CredentialsProvider),
                Mock(BeanContext)
        )
        factory.shutdownGracefully()

        when:
        factory.createSubscriber(new SubscriberFactoryConfig(
                ProjectSubscriptionName.of("test-project", "test-subscription"),
                Mock(MessageReceiver),
                "default",
                "scheduled"
        ))

        then:
        PubSubListenerException e = thrown()
        e.message == "Cannot create subscriber after shutdown has been initiated"
    }
}
