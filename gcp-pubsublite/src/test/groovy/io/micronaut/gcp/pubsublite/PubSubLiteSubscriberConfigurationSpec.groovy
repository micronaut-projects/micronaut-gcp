package io.micronaut.gcp.pubsublite

import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.cloud.pubsublite.MessageTransformer
import com.google.cloud.pubsublite.Partition
import com.google.cloud.pubsublite.SequencedMessage
import com.google.cloud.pubsublite.SubscriptionPath
import com.google.cloud.pubsublite.cloudpubsub.FlowControlSettings
import com.google.cloud.pubsublite.cloudpubsub.NackHandler
import com.google.cloud.pubsublite.cloudpubsub.Subscriber
import com.google.cloud.pubsublite.cloudpubsub.SubscriberSettings
import com.google.cloud.pubsublite.v1.CursorServiceClient
import com.google.cloud.pubsublite.v1.SubscriberServiceClient
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Property
import io.micronaut.gcp.pubsublite.support.DefaultLiteSubscriberFactory
import io.micronaut.gcp.pubsublite.support.LiteSubscriberFactory
import io.micronaut.gcp.pubsublite.support.LiteSubscriberFactoryConfig
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import java.util.function.Supplier

@MicronautTest
@Property(name = "spec.name", value = "PubSubLiteSubscriberConfigurationSpec")
@Property(name = "gcp.pubsublite.subscriber.test-flow-control.flow-control.bytes-outstanding", value = "3023")
@Property(name = "gcp.pubsublite.subscriber.test-flow-control.flow-control.messages-outstanding", value = "9382")
@Property(name = "gcp.pubsublite.subscriber.test-cursor-service.cursor-service-client-supplier", value = "customCursorServiceClientSupplier")
@Property(name = "gcp.pubsublite.subscriber.test-subscriber-service.subscriber-service-client-supplier", value = "customSubscriberServiceClientSupplier")
@Property(name = "gcp.pubsublite.subscriber.test-nack-handler.nack-handler", value = "customNackHandler")
@Property(name = "gcp.pubsublite.subscriber.test-message-transformer.message-transformer", value = "customMessageTransformer")
@Property(name = "gcp.pubsublite.subscriber.test-partition-list.partitions[0]", value = "100")
@Property(name = "gcp.pubsublite.subscriber.test-partition-list.partitions[1]", value = "200")
class PubSubLiteSubscriberConfigurationSpec extends Specification {
    @Inject
    DefaultLiteSubscriberFactory defaultLiteSubscriberFactory

    @Inject
    @Named("customCursorServiceClientSupplier")
    Supplier<CursorServiceClient> customCursorServiceClientSupplier

    @Inject
    @Named("customSubscriberServiceClientSupplier")
    Supplier<SubscriberServiceClient> customSubscriberServiceClientSupplier

    @Inject
    @Named("customNackHandler")
    NackHandler customNackHandler

    @Inject
    @Named("customMessageTransformer")
    MessageTransformer<SequencedMessage, PubsubMessage> customMessageTransformer;

    LiteSubscriberFactory spySubscriberFactory
    SubscriberSettings.Builder testResults

    def setup() {
         spySubscriberFactory = Spy(defaultLiteSubscriberFactory)
         spySubscriberFactory.startSubscriber(_ as SubscriberSettings.Builder) >>  { SubscriberSettings.Builder subscriberSettings ->
            testResults = subscriberSettings
            return Mock(Subscriber)
        }
    }
    def "test subscriber creation with defaults"() {
        when:
        spySubscriberFactory.createSubscriber(new LiteSubscriberFactoryConfig(Mock(SubscriptionPath),
                Mock(MessageReceiver), "no-config"))

        then:
        verifyAll {
            FlowControlSettings flowControlSettings = (FlowControlSettings) testResults.getProperties().get("perPartitionFlowControlSettings")
            flowControlSettings.bytesOutstanding() == (100L * 1024L * 1024L)
            flowControlSettings.messagesOutstanding() == 1000L
        }
    }

    def "test subscriber creation with custom flow control"() {
        when:
        spySubscriberFactory.createSubscriber(new LiteSubscriberFactoryConfig(Mock(SubscriptionPath),
                Mock(MessageReceiver), "test-flow-control"))

        then:
        verifyAll {
            FlowControlSettings flowControlSettings = (FlowControlSettings) testResults.getProperties().get("perPartitionFlowControlSettings")
            flowControlSettings.bytesOutstanding() == 3023L
            flowControlSettings.messagesOutstanding() == 9382L
        }
    }

    def "test subscriber creation with custom cursor service"() {
        when:
        spySubscriberFactory.createSubscriber(new LiteSubscriberFactoryConfig(Mock(SubscriptionPath),
                Mock(MessageReceiver), "test-cursor-service"))

        then:
        ((Optional<Supplier<CursorServiceClient>>) testResults.getProperties().get("cursorServiceClientSupplier")).get() == customCursorServiceClientSupplier
    }

    def "test subscriber creation with custom subscriber service"() {
        when:
        spySubscriberFactory.createSubscriber(new LiteSubscriberFactoryConfig(Mock(SubscriptionPath),
                Mock(MessageReceiver), "test-subscriber-service"))

        then:
        ((Optional<Supplier<SubscriberServiceClient>>) testResults.getProperties().get("subscriberServiceClientSupplier")).get() == customSubscriberServiceClientSupplier
    }

    def "test subscriber creation with custom nack handler"() {
        when:
        spySubscriberFactory.createSubscriber(new LiteSubscriberFactoryConfig(Mock(SubscriptionPath),
                Mock(MessageReceiver), "test-nack-handler"))

        then:
        ((Optional<NackHandler>) testResults.getProperties().get("nackHandler")).get() == customNackHandler
    }

    def "test subscriber creation with custom message transformer"() {
        when:
        spySubscriberFactory.createSubscriber(new LiteSubscriberFactoryConfig(Mock(SubscriptionPath),
                Mock(MessageReceiver), "test-message-transformer"))

        then:
        ((Optional<MessageTransformer<SequencedMessage, PubsubMessage>>) testResults.getProperties().get("transformer")).get() == customMessageTransformer
    }

    def "test subscriber creation with custom partition assignment"() {
        when:
        spySubscriberFactory.createSubscriber(new LiteSubscriberFactoryConfig(Mock(SubscriptionPath),
                Mock(MessageReceiver), "test-partition-list"))

        then:
        List<Partition> partitionList = ((Optional<List<Partition>>) testResults.getProperties().get("partitions")).get()
        verifyAll {
            partitionList != null
            partitionList.size() == 2
            partitionList.get(0).value() == 100L
            partitionList.get(1).value() == 200L
        }
    }


    @Singleton
    @Named("customCursorServiceClientSupplier")
    Supplier<CursorServiceClient> customCursorServiceClientSupplier() {
        Mock(Supplier<CursorServiceClient>)
    }

    @Singleton
    @Named("customSubscriberServiceClientSupplier")
    Supplier<SubscriberServiceClient> customSubscriberServiceClientSupplier() {
        Mock(Supplier<SubscriberServiceClient>)
    }

    @Singleton
    @Named("customNackHandler")
    NackHandler customNackHandler() {
        Mock(NackHandler)
    }

    @Singleton
    @Named("customMessageTransformer")
    MessageTransformer<SequencedMessage, PubsubMessage> customMessageTransformer() {
        Mock(MessageTransformer<SequencedMessage, PubsubMessage>)
    }
}
