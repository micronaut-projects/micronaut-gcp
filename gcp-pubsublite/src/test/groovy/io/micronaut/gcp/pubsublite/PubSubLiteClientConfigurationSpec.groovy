package io.micronaut.gcp.pubsublite

import com.google.api.gax.batching.BatchingSettings
import com.google.api.gax.batching.FlowController
import com.google.cloud.pubsublite.Message
import com.google.cloud.pubsublite.MessageTransformer
import com.google.cloud.pubsublite.TopicPath
import com.google.cloud.pubsublite.cloudpubsub.KeyExtractor
import com.google.cloud.pubsublite.cloudpubsub.Publisher
import com.google.cloud.pubsublite.cloudpubsub.PublisherSettings
import com.google.cloud.pubsublite.v1.PublisherServiceClient
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsublite.support.DefaultLitePublisherFactory
import io.micronaut.gcp.pubsublite.support.LitePublisherFactory
import io.micronaut.gcp.pubsublite.support.LitePublisherFactoryConfig
import io.micronaut.gcp.pubsublite.support.PubSubLitePublisherState
import io.micronaut.http.MediaType
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import org.threeten.bp.Duration
import spock.lang.Specification

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import java.util.function.Supplier

@MicronautTest
@Property(name = "spec.name", value = "PubSubLiteClientConfigurationSpec")
@Property(name = "gcp.pubsublite.publisher.test-service-client.service-client-supplier", value = "customServiceClientSupplier")
@Property(name = "gcp.pubsublite.publisher.test-key-extractor.key-extractor", value = "customKeyExtractor")
@Property(name = "gcp.pubsublite.publisher.test-message-transformer.message-transformer", value = "customMessageTransformer")
@Property(name = "gcp.pubsublite.publisher.test-batching.batching.delay-threshold", value = "2302ms")
@Property(name = "gcp.pubsublite.publisher.test-batching.batching.request-byte-threshold", value = "3122")
@Property(name = "gcp.pubsublite.publisher.test-batching.batching.element-count-threshold", value = "4123")
@Property(name = "gcp.pubsublite.publisher.test-batching.batching.is-enabled", value = "false")
@Property(name = "gcp.pubsublite.publisher.test-batching.flow-control.max-outstanding-element-count", value = "12302")
@Property(name = "gcp.pubsublite.publisher.test-batching.flow-control.max-outstanding-request-bytes", value = "1882")
@Property(name = "gcp.pubsublite.publisher.test-batching.flow-control.limit-exceeded-behavior", value = "Block")
class PubSubLiteClientConfigurationSpec extends Specification {

    @Inject
    KeyExtractor customKeyExtractor

    @Inject
    Supplier customServiceClientSupplier

    @Inject
    MessageTransformer customMessageTransformer

    @Inject
    DefaultLitePublisherFactory defaultPublisherFactory

    LitePublisherFactory spyPublisherFactory
    PublisherSettings.Builder testResults

    def setup() {
        spyPublisherFactory = Spy(defaultPublisherFactory)
        spyPublisherFactory.startPublisher(_ as PublisherSettings.Builder) >>  { PublisherSettings.Builder publisherSettings ->
            testResults = publisherSettings
            return Mock(Publisher)
        }
    }

    def "test client creation with defaults"() {
        def state = new PubSubLitePublisherState.TopicState(MediaType.APPLICATION_JSON,
                Mock(TopicPath), "no-config")

        when:
        spyPublisherFactory.createLitePublisher(new LitePublisherFactoryConfig(state))

        then:
        testResults != null
    }

    def "test client creation with custom service client supplier"() {
        def state = new PubSubLitePublisherState.TopicState(MediaType.APPLICATION_JSON,
                Mock(TopicPath), "test-service-client")

        when:
        spyPublisherFactory.createLitePublisher(new LitePublisherFactoryConfig(state))

        then:
        ((Optional) testResults.getProperties().get("serviceClientSupplier")).get() == customServiceClientSupplier
    }

    def "test client creation with custom key extractor"() {
        def state = new PubSubLitePublisherState.TopicState(MediaType.APPLICATION_JSON,
                Mock(TopicPath), "test-key-extractor")

        when:
        spyPublisherFactory.createLitePublisher(new LitePublisherFactoryConfig(state))

        then:
        ((Optional) testResults.getProperties().get("keyExtractor")).get() == customKeyExtractor
    }

    def "test client creation with custom message transformer"() {
        def state = new PubSubLitePublisherState.TopicState(MediaType.APPLICATION_JSON,
                Mock(TopicPath), "test-message-transformer")

        when:
        spyPublisherFactory.createLitePublisher(new LitePublisherFactoryConfig(state))

        then:
        ((Optional) testResults.getProperties().get("messageTransformer")).get() == customMessageTransformer
    }

    def "test client creation with custom batch settings"() {
        def state = new PubSubLitePublisherState.TopicState(MediaType.APPLICATION_JSON,
                Mock(TopicPath), "test-batching")

        when:
        spyPublisherFactory.createLitePublisher(new LitePublisherFactoryConfig(state))

        then:
        BatchingSettings batchingSettings = ((Optional<BatchingSettings>) testResults.getProperties().get("batchingSettings")).get()
        batchingSettings.delayThreshold == Duration.ofMillis(2302)
        batchingSettings.requestByteThreshold == 3122L
        batchingSettings.elementCountThreshold == 4123L
        !batchingSettings.isEnabled
        batchingSettings.flowControlSettings.maxOutstandingElementCount == 12302L
        batchingSettings.flowControlSettings.maxOutstandingRequestBytes == 1882L
        batchingSettings.flowControlSettings.limitExceededBehavior == FlowController.LimitExceededBehavior.Block
    }

    @Singleton
    @Named("customKeyExtractor")
    @Requires(property = "spec.name", value = "PubSubLiteClientConfigurationSpec")
    KeyExtractor customKeyExtractor() {
        Mock(KeyExtractor)
    }

    @Singleton
    @Named("customServiceClientSupplier")
    @Requires(property = "spec.name", value = "PubSubLiteClientConfigurationSpec")
    Supplier<PublisherServiceClient> customServiceClientSupplier() {
        Mock(Supplier<PublisherServiceClient>)
    }

    @Singleton
    @Named("customMessageTransformer")
    @Requires(property = "spec.name", value = "PubSubLiteClientConfigurationSpec")
    MessageTransformer<PubsubMessage, Message> messageTransformer() {
        Mock(MessageTransformer<PubsubMessage, Message>)
    }
}
