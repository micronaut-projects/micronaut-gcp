/*
 * Copyright 2017-2020 original authors
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

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.context.BeanContext;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.bind.DefaultExecutableBinder;
import io.micronaut.core.bind.exceptions.UnsatisfiedArgumentException;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.StringUtils;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.pubsub.annotation.PubSubListener;
import io.micronaut.gcp.pubsub.annotation.Subscription;
import io.micronaut.gcp.pubsub.bind.DefaultPubSubAcknowledgement;
import io.micronaut.gcp.pubsub.bind.PubSubBinderRegistry;
import io.micronaut.gcp.pubsub.bind.PubSubConsumerState;
import io.micronaut.gcp.pubsub.bind.SubscriberFactory;
import io.micronaut.gcp.pubsub.bind.SubscriberFactoryConfig;
import io.micronaut.gcp.pubsub.configuration.PubSubConfigurationProperties;
import io.micronaut.gcp.pubsub.exception.PubSubListenerException;
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverException;
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverExceptionHandler;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDesRegistry;
import io.micronaut.gcp.pubsub.support.PubSubSubscriptionUtils;
import io.micronaut.http.MediaType;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.messaging.Acknowledgement;
import io.micronaut.messaging.exceptions.MessageListenerException;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Implementation of an {@link ExecutableMethodProcessor} that creates
 * {@link com.google.cloud.pubsub.v1.MessageReceiver} that subscribes to the PubSub subscription
 * and invoke methods annotated by @{@link io.micronaut.gcp.pubsub.annotation.Subscription}.
 * <p>
 * There can be only one subscriber for any given subscription (in order to avoid issues with message
 * acknowledgement control). Having more than one method using the same subscription raises a {@link io.micronaut.gcp.pubsub.exception.PubSubListenerException}.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Singleton
public class PubSubConsumerAdvice implements ExecutableMethodProcessor<Subscription> {

    private final Logger logger = LoggerFactory.getLogger(PubSubConsumerAdvice.class);
    private final BeanContext beanContext;
    private final ConversionService conversionService;
    private final SubscriberFactory subscriberFactory;
    private final GoogleCloudConfiguration googleCloudConfiguration;
    private final PubSubConfigurationProperties pubSubConfigurationProperties;
    private final PubSubBinderRegistry binderRegistry;
    private final PubSubMessageReceiverExceptionHandler exceptionHandler;
    private final AtomicBoolean shutDownMode = new AtomicBoolean(false);

    public PubSubConsumerAdvice(BeanContext beanContext,
                                ConversionService conversionService,
                                PubSubMessageSerDesRegistry serDesRegistry,
                                SubscriberFactory subscriberFactory,
                                GoogleCloudConfiguration googleCloudConfiguration,
                                PubSubConfigurationProperties pubSubConfigurationProperties,
                                PubSubBinderRegistry binderRegistry,
                                PubSubMessageReceiverExceptionHandler exceptionHandler) {
        this.beanContext = beanContext;
        this.conversionService = conversionService;
        this.subscriberFactory = subscriberFactory;
        this.googleCloudConfiguration = googleCloudConfiguration;
        this.pubSubConfigurationProperties = pubSubConfigurationProperties;
        this.binderRegistry = binderRegistry;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        if (beanDefinition.hasDeclaredAnnotation(PubSubListener.class)) {
            AnnotationValue<Subscription> subscriptionAnnotation = method.getAnnotation(Subscription.class);
            io.micronaut.context.Qualifier<Object> qualifier = beanDefinition
                    .getAnnotationTypeByStereotype(Qualifier.class)
                    .map(type -> Qualifiers.byAnnotation(beanDefinition, type))
                    .orElse(null);
            boolean hasAckArg = Arrays.stream(method.getArguments())
                    .anyMatch(arg -> Acknowledgement.class.isAssignableFrom(arg.getType()));

            Class<Object> beanType = (Class<Object>) beanDefinition.getBeanType();
            Object bean = beanContext.findBean(beanType, qualifier)
                    .orElseThrow(() -> new MessageListenerException("Could not find the bean to execute the method " + method));
            DefaultExecutableBinder<PubSubConsumerState> binder = new DefaultExecutableBinder<>();

            if (subscriptionAnnotation != null) {
                String subscriptionName = subscriptionAnnotation.getRequiredValue(String.class);
                ProjectSubscriptionName projectSubscriptionName = PubSubSubscriptionUtils.toProjectSubscriptionName(subscriptionName, googleCloudConfiguration.getProjectId());
                String defaultContentType = subscriptionAnnotation.stringValue("contentType").orElse(MediaType.APPLICATION_JSON);
                String configuration = subscriptionAnnotation.stringValue("configuration").orElse("");
                MessageReceiver receiver = (PubsubMessage message, AckReplyConsumer ackReplyConsumer) -> {

                    if (pubSubConfigurationProperties.isNackOnShutdown() && shutDownMode.get()) {
                        ackReplyConsumer.nack();
                        return;
                    }

                    String messageContentType = message.getAttributesMap().getOrDefault("Content-Type", "");
                    String contentType = Optional.of(messageContentType)
                            .filter(StringUtils::isNotEmpty)
                            .orElse(defaultContentType);
                    DefaultPubSubAcknowledgement pubSubAcknowledgement = new DefaultPubSubAcknowledgement(ackReplyConsumer);

                    PubSubConsumerState consumerState = new PubSubConsumerState(message, ackReplyConsumer,
                            projectSubscriptionName, contentType);
                    boolean autoAcknowledge = !hasAckArg;
                    try {
                        @SuppressWarnings("rawtypes")
                        BoundExecutable executable = binder.bind(method, binderRegistry, consumerState);
                        Flux<?> resultPublisher = resultAsFlux(Objects.requireNonNull(executable).invoke(bean));
                        resultPublisher.subscribe(data -> { }, //no-op
                            ex -> handleException(new PubSubMessageReceiverException("Error handling message", ex, bean, consumerState, autoAcknowledge)),
                            autoAcknowledge ? pubSubAcknowledgement::ack : () -> this.verifyManualAcknowledgment(executable, method.getName()));
                    } catch (UnsatisfiedArgumentException e) {
                        handleException(new PubSubMessageReceiverException("Error binding message to the method", e, bean, consumerState, autoAcknowledge));
                    } catch (Exception e) {
                        handleException(new PubSubMessageReceiverException("Error handling message", e, bean, consumerState, autoAcknowledge));
                    }
                };
                try {
                    this.subscriberFactory.createSubscriber(new SubscriberFactoryConfig(projectSubscriptionName, receiver, configuration, pubSubConfigurationProperties.getSubscribingExecutor()));
                } catch (Exception e) {
                    throw new PubSubListenerException("Failed to create subscriber for %s with subscription method %s".formatted(beanDefinition.getBeanType(), method.getName()), e);
                }
            }
        }
    }

    private void verifyManualAcknowledgment(@SuppressWarnings("rawtypes") BoundExecutable executable, String methodName) {
        Optional<Object> boundAck = Arrays
            .stream(executable.getBoundArguments())
            .filter(o -> (o instanceof DefaultPubSubAcknowledgement))
            .findFirst();
        if (boundAck.isPresent()) {
            DefaultPubSubAcknowledgement manualAck = (DefaultPubSubAcknowledgement) boundAck.get();
            if (!manualAck.isClientAck()) {
                logger.warn("Method {} was executed and no message acknowledge detected. Did you forget to invoke ack()/nack()?", methodName);
            }
        }
    }

    @PreDestroy
    public final void shutDown() {
        shutDownMode.set(true);
    }

    private void handleException(PubSubMessageReceiverException ex) {
        if (ex.getListener() instanceof PubSubMessageReceiverExceptionHandler bean) {
            bean.handle(ex);
        } else {
            exceptionHandler.handle(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Flux<T> resultAsFlux(T result) {
        if (!Publishers.isConvertibleToPublisher(result)) {
            return Flux.empty();
        }
        return Flux.from(Publishers.convertPublisher(conversionService, result, Publisher.class));
    }

}
