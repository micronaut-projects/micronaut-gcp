package io.micronaut.gcp.pubsublite

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.core.SettableApiFuture
import com.google.cloud.pubsublite.cloudpubsub.Publisher
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.aop.exceptions.UnimplementedAdviceException
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.support.DefaultPublisherFactory
import io.micronaut.gcp.pubsublite.annotation.LiteTopic
import io.micronaut.gcp.pubsublite.annotation.PubSubLiteClient
import io.micronaut.gcp.pubsublite.support.LitePublisherFactory
import io.micronaut.gcp.pubsublite.support.LitePublisherFactoryConfig
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.reactivex.Single
import spock.lang.Specification

import javax.inject.Inject
import java.time.OffsetDateTime
import java.time.ZoneOffset

@MicronautTest
@Property(name = "spec.name", value = "PubSubLiteClientSpec")
class PubSubLiteClientSpec extends Specification {

    PubsubMessage dataHolder

    @Inject
    ObjectMapper mapper;

    @Inject
    LitePublisherFactory litePublisherFactory

    @Inject
    PubSubLiteClientSpecClient testClient

    def "test client with defaults"() {
        when:
        String published = testClient.testPublishDefaults("test")

        then:
        verifyAll {
            dataHolder.getData().toStringUtf8() == "\"test\""
            published == "1234"
        }
    }

    def "test client with location set"() {
        when:
        String published = testClient.testPublishLocation("test")

        then:
        verifyAll {
            dataHolder.getData().toStringUtf8() == "\"test\""
            published == "1234"
        }
    }

    def "test client with future return value"() {
        when:
        Single<String> published = testClient.testWithFuture("test")

        then:
        verifyAll {
            dataHolder.getData().toStringUtf8() == "\"test\""
            published.blockingGet() == "1234"
        }
    }

    def "test client with pojo parameter"() {
        when:
        PubSubLiteClientSpecPublishPojo pojo = new PubSubLiteClientSpecPublishPojo();
        pojo.data = "test"
        pojo.publishDate = OffsetDateTime.of(2020, 12, 8, 0, 0, 0, 0, ZoneOffset.UTC)
        String published = testClient.testPublishWithPojo(pojo)

        then:
        PubSubLiteClientSpecPublishPojo testPublishPojo = mapper.readValue(dataHolder.getData().toByteArray(), PubSubLiteClientSpecPublishPojo.class)
        verifyAll {
            testPublishPojo.getData() == pojo.getData()
            testPublishPojo.getPublishDate() == pojo.getPublishDate()
            published == "1234"
        }
    }

    def "wrong topic type is ignored"() {
        when:
        testClient.ignoredPubSubTopic("test")

        then:
        thrown(UnimplementedAdviceException)
    }

    @Primary
    @MockBean(DefaultPublisherFactory)
    LitePublisherFactory litePublisherFactory() {
        def mockFactory = Mock(LitePublisherFactory)
        def mockPublisher = Mock(Publisher)
        def future = new SettableApiFuture<String>()
        future.set("1234")
        mockPublisher.publish(_ as PubsubMessage) >> { PubsubMessage message -> dataHolder = message; return future; }
        mockFactory.createLitePublisher(_ as LitePublisherFactoryConfig) >> mockPublisher
        return mockFactory
    }
}

class PubSubLiteClientSpecPublishPojo {
    String data;
    OffsetDateTime publishDate;
}

@PubSubLiteClient
@Requires(property = "spec.name", value = "")
interface PubSubLiteClientSpecClient {

    @LiteTopic(name = "test-topic")
    String testPublishDefaults(String testData);

    @LiteTopic(name = "test-topic")
    String testPublishWithPojo(PubSubLiteClientSpecPublishPojo testData);

    @LiteTopic(name = "test-topic", location = "us-central1-b")
    String testPublishLocation(String testData);

    @LiteTopic(name = "test-topic")
    Single<String> testWithFuture(String testData);

    @Topic("test-topic")
    String ignoredPubSubTopic(String testData);
}
