package io.micronaut.gcp.pubsub

import com.google.api.core.SettableApiFuture
import com.google.cloud.pubsub.v1.PublisherInterface
import com.google.cloud.pubsub.v1.SubscriberInterface
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Replaces
import io.micronaut.gcp.pubsub.bind.SubscriberFactory
import io.micronaut.gcp.pubsub.bind.SubscriberFactoryConfig
import io.micronaut.gcp.pubsub.support.PublisherFactory
import io.micronaut.gcp.pubsub.support.PublisherFactoryConfig
import io.micronaut.test.annotation.MockBean
import spock.lang.Specification



abstract class AbstractConsumerSpec extends Specification{

    @MockBean
    @Replaces(PublisherFactory)
    PublisherFactory publisherFactory(MockPubSubEngine pubSubEngine){
        def factory = Mock(PublisherFactory)
        def publisher = Mock(MockPublisher)

        def future = new SettableApiFuture<String>()
        future.set("1234")
        factory.createPublisher(_) >> { PublisherFactoryConfig config ->
            publisher.getTopicNameString() >> config.topicName.topic
            publisher.publish(_) >> { PubsubMessage message -> pubSubEngine.publish(message, publisher.getTopicNameString()); return future }
            return publisher
        }
        return factory
    }

    @MockBean
    @Replaces(SubscriberFactory)
    SubscriberFactory subscriberFactory(MockPubSubEngine pubSubEngine) {
        def factory = Mock(SubscriberFactory)
        def subscriber = Mock(SubscriberInterface)
        factory.createSubscriber(_)  >> { SubscriberFactoryConfig config ->
            pubSubEngine.registerReceiver(config.receiver, config.getSubscriptionName().getSubscription())
            return subscriber
        }
        return factory
    }
}

interface MockPublisher extends PublisherInterface {
    String getTopicNameString()
}