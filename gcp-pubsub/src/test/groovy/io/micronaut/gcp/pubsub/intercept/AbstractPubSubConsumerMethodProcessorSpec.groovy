package io.micronaut.gcp.pubsub.intercept

import com.google.cloud.pubsub.v1.MessageReceiver
import com.google.pubsub.v1.ProjectSubscriptionName
import io.micronaut.context.BeanContext
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.order.Ordered
import io.micronaut.gcp.GoogleCloudConfiguration
import io.micronaut.gcp.pubsub.annotation.Subscription
import io.micronaut.gcp.pubsub.bind.PubSubBinderRegistry
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverExceptionHandler
import io.micronaut.runtime.graceful.GracefulShutdownCapable
import spock.lang.Specification

class AbstractPubSubConsumerMethodProcessorSpec extends Specification {

    void "consumer processor enters shutdown mode during graceful shutdown"() {
        given:
        def processor = new TestConsumerMethodProcessor(
                Mock(BeanContext),
                Mock(ConversionService),
                Mock(GoogleCloudConfiguration),
                Mock(PubSubBinderRegistry),
                Mock(PubSubMessageReceiverExceptionHandler)
        )

        when:
        def shutdown = processor.shutdownGracefully()

        then:
        processor instanceof GracefulShutdownCapable
        processor.order == Ordered.HIGHEST_PRECEDENCE
        processor.shutdownInitiated()
        shutdown.toCompletableFuture().done
    }

    private static final class TestConsumerMethodProcessor extends AbstractPubSubConsumerMethodProcessor<Subscription> {

        TestConsumerMethodProcessor(BeanContext beanContext,
                                    ConversionService conversionService,
                                    GoogleCloudConfiguration googleCloudConfiguration,
                                    PubSubBinderRegistry binderRegistry,
                                    PubSubMessageReceiverExceptionHandler exceptionHandler) {
            super(Subscription, beanContext, conversionService, googleCloudConfiguration, binderRegistry, exceptionHandler)
        }

        @Override
        protected void addSubscriber(ProjectSubscriptionName projectSubscriptionName, MessageReceiver receiver, String configuration) {
        }

        boolean shutdownInitiated() {
            isShutDownInitiated()
        }
    }
}
