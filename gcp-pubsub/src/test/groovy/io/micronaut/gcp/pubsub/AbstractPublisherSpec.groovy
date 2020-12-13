package io.micronaut.gcp.pubsub

import com.google.api.core.SettableApiFuture
import com.google.cloud.pubsub.v1.Publisher
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Replaces
import io.micronaut.gcp.pubsub.support.PublisherFactory
import spock.lang.Specification

/**
 * @author vinicius*
 *
 */

abstract class AbstractPublisherSpec extends Specification {

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
