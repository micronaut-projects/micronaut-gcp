package io.micronaut.gcp.pubsub.intercept;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.pubsub.annotation.PushSubscription;
import io.micronaut.gcp.pubsub.bind.PubSubBinderRegistry;
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverException;
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverExceptionHandler;
import io.micronaut.gcp.pubsub.push.PubSubPushConfigurationProperties;
import io.micronaut.gcp.pubsub.push.PubSubPushMessageReceiverException;
import io.micronaut.gcp.pubsub.push.PushSubscriberHandler;
import io.micronaut.scheduling.TaskExecutors;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;
import java.util.concurrent.ExecutorService;

@Requires(beans = PubSubPushConfigurationProperties.class)
@Singleton
public class PubSubPushConsumerAdvice extends AbstractPubSubConsumerMethodProcessor<PushSubscription> {

    private final PushSubscriberHandler subscriberHandler;
    private final PubSubPushConfigurationProperties pubSubConfigurationProperties;
    private final Scheduler scheduler;

    protected PubSubPushConsumerAdvice(BeanContext beanContext, ConversionService conversionService, GoogleCloudConfiguration googleCloudConfiguration, PubSubBinderRegistry binderRegistry, PubSubMessageReceiverExceptionHandler exceptionHandler,
                                       @Named(TaskExecutors.BLOCKING) ExecutorService executorService, PushSubscriberHandler subscriberHandler, PubSubPushConfigurationProperties pubSubConfigurationProperties) {
        super(PushSubscription.class, beanContext, conversionService, googleCloudConfiguration, binderRegistry, exceptionHandler);
        this.subscriberHandler = subscriberHandler;
        this.pubSubConfigurationProperties = pubSubConfigurationProperties;
        this.scheduler = Schedulers.fromExecutorService(executorService);
    }

    @Override
    protected void addSubscriber(ProjectSubscriptionName projectSubscriptionName, MessageReceiver receiver, String configuration) {
        subscriberHandler.addSubscriber(projectSubscriptionName, receiver);
    }

    @Override
    protected void handleException(PubSubMessageReceiverException ex) {
        super.handleException(PubSubPushMessageReceiverException.from(ex));
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Flux<?> executeSubscriberMethod(BoundExecutable executable, Object bean, boolean isBlocking) {
        if (isBlocking) {
            return Mono.fromCallable(() -> Objects.requireNonNull(executable).invoke(bean)).flux().subscribeOn(scheduler);
        }
        return super.executeSubscriberMethod(executable, bean, false);
    }

}
