/*
 * Copyright 2017-2024 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.gcp.pubsub.intercept;

import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.ProjectSubscriptionName;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.processor.ExecutableMethodProcessor;
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

/**
 * Implementation of an {@link ExecutableMethodProcessor} that creates {@link com.google.cloud.pubsub.v1.MessageReceiver}s to
 * be invoked in response to PubSub push messages routed from the {@link io.micronaut.gcp.pubsub.push.PushController}. The provided
 * {@code MessageReceiver} then invokes methods annotated by {@link PushSubscription}.
 *
 * <p>
 * There can be only one subscriber for any given subscription (in order to avoid issues with message
 * acknowledgement control). Having more than one method using the same subscription raises a {@link io.micronaut.gcp.pubsub.exception.PubSubListenerException}.
 * </p>
 *
 * <p>
 * Special handling is provided for subscriber methods that are also annotated with {@link io.micronaut.core.annotation.Blocking}, so as to avoid
 * blocking the main HTTP event loop.
 * </p>
 *
 * @author Jeremy Grelle
 * @since 5.4.0
 */
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

    /**
     * If the {@link PushSubscription} method is marked with {@link io.micronaut.core.annotation.Blocking}, executes the subscriber
     * method using the {@link TaskExecutors#BLOCKING} executor service to avoid blocking the main HTTP event loop.
     *
     * @param executable the bound executable subscription method
     * @param bean the bean PubSub listener bean
     * @param isBlocking whether the subscription method is blocking
     * @return a {@link Flux} that will complete after subscriber execution
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected Flux<?> executeSubscriberMethod(BoundExecutable executable, Object bean, boolean isBlocking) {
        if (isBlocking) {
            return Mono.fromCallable(() -> Objects.requireNonNull(executable).invoke(bean)).flux().subscribeOn(scheduler);
        }
        return super.executeSubscriberMethod(executable, bean, false);
    }

}
