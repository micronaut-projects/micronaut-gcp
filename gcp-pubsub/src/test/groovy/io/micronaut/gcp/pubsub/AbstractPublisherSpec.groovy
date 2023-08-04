package io.micronaut.gcp.pubsub

import com.google.api.core.SettableApiFuture
import com.google.cloud.pubsub.v1.Publisher
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Replaces
import io.micronaut.gcp.pubsub.support.PublisherFactory
import io.micronaut.gcp.pubsub.support.PublisherFactoryConfig
import io.micronaut.test.annotation.MockBean
import spock.lang.Specification

/**
 * @author vinicius*
 *
 */

abstract class AbstractPublisherSpec extends Specification {

    @MockBean
    @Replaces(PublisherFactory)
    PublisherFactory publisherFactory() {
        def factory = Mock(PublisherFactory)
        def future = new SettableApiFuture<String>()
        future.set("1234")
        factory.createPublisher(_) >> {
            PublisherFactoryConfig config -> {
                return Mock(Publisher) {
                    publish(_) >> {
                        PubsubMessage message -> DataHolder.getInstance().setProjectId(config?.topicState?.projectTopicName?.project).setData(message); return future;
                    }
                }
            }
        }
        return factory
    }
}
