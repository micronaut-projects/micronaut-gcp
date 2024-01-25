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
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.pubsub.annotation.PushSubscription;
import io.micronaut.gcp.pubsub.bind.PubSubBinderRegistry;
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverException;
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverExceptionHandler;
import io.micronaut.gcp.pubsub.push.PubSubPushMessageReceiverException;
import io.micronaut.gcp.pubsub.push.PushControllerConfiguration;
import io.micronaut.gcp.pubsub.push.PushSubscriberHandler;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.scheduling.executor.ExecutorSelector;
import jakarta.inject.Singleton;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Objects;

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
@Requires(beans = PushControllerConfiguration.class)
@Singleton
@Internal
final class PubSubPushConsumerAdvice extends AbstractPubSubConsumerMethodProcessor<PushSubscription> {

    private static final String EXECUTE_ON = ExecuteOn.class.getName();

    private final PushSubscriberHandler subscriberHandler;
    private final PushControllerConfiguration pubSubConfigurationProperties;
    private final ExecutorSelector executorSelector;

    protected PubSubPushConsumerAdvice(
        BeanContext beanContext,
        ConversionService conversionService,
        GoogleCloudConfiguration googleCloudConfiguration,
        PubSubBinderRegistry binderRegistry,
        PubSubMessageReceiverExceptionHandler exceptionHandler,
        ExecutorSelector executorSelector,
        PushSubscriberHandler subscriberHandler,
        PushControllerConfiguration pubSubConfigurationProperties
    ) {
        super(PushSubscription.class, beanContext, conversionService, googleCloudConfiguration, binderRegistry, exceptionHandler);
        this.executorSelector = executorSelector;
        this.subscriberHandler = subscriberHandler;
        this.pubSubConfigurationProperties = pubSubConfigurationProperties;
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
     * @param beanDefinition the bean definition of the subscriber
     * @param method         the executable method reference
     * @param executable     the bound executable subscription method
     * @param bean           the bean PubSub listener bean
     * @return a {@link Flux} that will complete after subscriber execution
     */
    @Override
    protected Flux<Object> executeSubscriberMethod(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method, BoundExecutable<Object, Object> executable, Object bean) {
        Scheduler subscribeOnScheduler = schedulerFor(beanDefinition, method);
        if (subscribeOnScheduler != null) {
            return Mono.fromCallable(() -> Objects.requireNonNull(executable).invoke(bean)).flux().subscribeOn(subscribeOnScheduler);
        }
        return super.executeSubscriberMethod(beanDefinition, method, executable, bean);
    }

    private @Nullable Scheduler schedulerFor(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        if (beanDefinition.hasDeclaredAnnotation(ExecuteOn.class)) {
            return executorSelector.select(beanDefinition.stringValue(EXECUTE_ON).orElse(null)).map(Schedulers::fromExecutorService).orElse(null);
        }
        return executorSelector.select(method, null).map(Schedulers::fromExecutorService).orElse(null);
    }

}
