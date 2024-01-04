package io.micronaut.gcp.pubsub.intercept;

import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.ProjectSubscriptionName;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.pubsub.annotation.PushSubscription;
import io.micronaut.gcp.pubsub.bind.PubSubBinderRegistry;
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverExceptionHandler;
import io.micronaut.gcp.pubsub.push.PubSubPushConfigurationProperties;
import io.micronaut.gcp.pubsub.push.PushSubscriberHandler;
import jakarta.inject.Singleton;

@Requires(beans = PubSubPushConfigurationProperties.class)
@Singleton
public class PubSubPushConsumerAdvice extends AbstractPubSubConsumerMethodProcessor<PushSubscription> {

    private final PushSubscriberHandler subscriberHandler;
    private final PubSubPushConfigurationProperties pubSubConfigurationProperties;

    protected PubSubPushConsumerAdvice(BeanContext beanContext, ConversionService conversionService, GoogleCloudConfiguration googleCloudConfiguration, PubSubBinderRegistry binderRegistry, PubSubMessageReceiverExceptionHandler exceptionHandler, PushSubscriberHandler subscriberHandler, PubSubPushConfigurationProperties pubSubConfigurationProperties) {
        super(PushSubscription.class, beanContext, conversionService, googleCloudConfiguration, binderRegistry, exceptionHandler);
        this.subscriberHandler = subscriberHandler;
        this.pubSubConfigurationProperties = pubSubConfigurationProperties;
    }

    @Override
    protected void addSubscriber(ProjectSubscriptionName projectSubscriptionName, MessageReceiver receiver, String configuration) {
        subscriberHandler.addSubscriber(projectSubscriptionName, receiver);
    }
}
