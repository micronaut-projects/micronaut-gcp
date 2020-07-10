package io.micronaut.gcp.pubsub.support

import com.google.api.core.SettableApiFuture
import com.google.cloud.pubsub.v1.Publisher
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Replaces
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import spock.lang.Specification

@MicronautTest
class SerDesSpec extends Specification {

    @MockBean
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
