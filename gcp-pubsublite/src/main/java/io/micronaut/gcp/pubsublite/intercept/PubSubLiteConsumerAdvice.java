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
package io.micronaut.gcp.pubsublite.intercept;

import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsublite.SubscriptionPath;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.context.BeanContext;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.bind.DefaultExecutableBinder;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.StringUtils;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.pubsub.bind.DefaultPubSubAcknowledgement;
import io.micronaut.gcp.pubsub.exception.PubSubListenerException;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDesRegistry;
import io.micronaut.gcp.pubsublite.annotation.LiteSubscription;
import io.micronaut.gcp.pubsublite.annotation.PubSubLiteListener;
import io.micronaut.gcp.pubsublite.bind.PubSubLiteBinderRegistry;
import io.micronaut.gcp.pubsublite.bind.PubSubLiteConsumerState;
import io.micronaut.gcp.pubsublite.configuration.PubSubLiteConfigurationProperties;
import io.micronaut.gcp.pubsublite.exception.PubSubLiteMessageReceiverException;
import io.micronaut.gcp.pubsublite.exception.PubSubLiteMessageReceiverExceptionHandler;
import io.micronaut.gcp.pubsublite.support.LiteSubscriberFactory;
import io.micronaut.gcp.pubsublite.support.LiteSubscriberFactoryConfig;
import io.micronaut.gcp.pubsublite.support.PubSubLiteSubscriptionUtils;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.messaging.Acknowledgement;
import io.micronaut.messaging.exceptions.MessageListenerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Optional;

/**
 * Implementation of an {@link ExecutableMethodProcessor} that creates
 * {@link com.google.cloud.pubsub.v1.MessageReceiver} that subscribes to the PubSub Lite subscription
 * and invoke methods annotated by @{@link LiteSubscription}.
 * <p>
 * There can be only one subscriber for any given subscription (in order to avoid issues with message
 * Ack control). Having more than one method using the same subscription raises a {@link io.micronaut.gcp.pubsub.exception.PubSubListenerException}.
 *
 * Based on {@link io.micronaut.gcp.pubsub.intercept.PubSubConsumerAdvice}
 *
 * @author Jacob Mims
 * @since 2.2.0
 */
@Singleton
public class PubSubLiteConsumerAdvice implements ExecutableMethodProcessor<PubSubLiteListener> {
    private final Logger logger = LoggerFactory.getLogger(PubSubLiteConsumerAdvice.class);
    private final BeanContext beanContext;
    private final ConversionService<?> conversionService;
    private final PubSubMessageSerDesRegistry serDesRegistry;
    private final LiteSubscriberFactory subscriberFactory;
    private final GoogleCloudConfiguration googleCloudConfiguration;
    private final PubSubLiteConfigurationProperties pubSubConfigurationProperties;
    private final PubSubLiteBinderRegistry binderRegistry;
    private final PubSubLiteMessageReceiverExceptionHandler exceptionHandler;

    public PubSubLiteConsumerAdvice(BeanContext beanContext,
                                ConversionService<?> conversionService,
                                PubSubMessageSerDesRegistry serDesRegistry,
                                LiteSubscriberFactory subscriberFactory,
                                GoogleCloudConfiguration googleCloudConfiguration,
                                PubSubLiteConfigurationProperties pubSubConfigurationProperties,
                                PubSubLiteBinderRegistry binderRegistry,
                                PubSubLiteMessageReceiverExceptionHandler exceptionHandler) {
        this.beanContext = beanContext;
        this.conversionService = conversionService;
        this.serDesRegistry = serDesRegistry;
        this.subscriberFactory = subscriberFactory;
        this.googleCloudConfiguration = googleCloudConfiguration;
        this.pubSubConfigurationProperties = pubSubConfigurationProperties;
        this.binderRegistry = binderRegistry;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        AnnotationValue<LiteSubscription> subscriptionAnnotation = method.getAnnotation(LiteSubscription.class);
        io.micronaut.context.Qualifier<Object> qualifer = beanDefinition
                .getAnnotationTypeByStereotype(Qualifier.class)
                .map(type -> Qualifiers.byAnnotation(beanDefinition, type))
                .orElse(null);
        boolean hasAckArg = Arrays.stream(method.getArguments())
                .anyMatch(arg -> Acknowledgement.class.isAssignableFrom(arg.getType()));

        Class<Object> beanType = (Class<Object>) beanDefinition.getBeanType();
        Object bean = beanContext.findBean(beanType, qualifer).orElseThrow(() -> new MessageListenerException("Could not find the bean to execute the method " + method));
        DefaultExecutableBinder<PubSubLiteConsumerState> binder = new DefaultExecutableBinder<>();

        if (subscriptionAnnotation != null) {
            AnnotationValue<PubSubLiteListener> listener = method.findAnnotation(PubSubLiteListener.class).orElseThrow(() -> new IllegalStateException("No @PubSubLiteListener annotation present"));
            SubscriptionPath subscriptionPath;
            if (!subscriptionAnnotation.stringValue().isPresent()) {
                String projectId = googleCloudConfiguration.getProjectId();
                long projectNumber = listener.longValue("projectNumber").orElse(0);
                String subscriptionName = subscriptionAnnotation.getRequiredValue("name", String.class);
                String location = subscriptionAnnotation.getRequiredValue("location", String.class);

                 subscriptionPath = projectNumber > 0 ?
                        PubSubLiteSubscriptionUtils.toSubscriptionPath(subscriptionName, projectNumber, location) :
                        PubSubLiteSubscriptionUtils.toSubscriptionPath(subscriptionName, projectId, location);
            } else {
                subscriptionPath = SubscriptionPath.parse(subscriptionAnnotation.stringValue().get());
            }

            String defaultContentType = subscriptionAnnotation.get("contentType", String.class).orElse("");
            String configuration = subscriptionAnnotation.get("configuration", String.class).orElse("");

            MessageReceiver receiver = (PubsubMessage message, AckReplyConsumer ackReplyConsumer) -> {
                String messageContentType = message.getAttributesMap().getOrDefault("Content-Type", "");
                String contentType = Optional.of(messageContentType)
                        .filter(StringUtils::isNotEmpty)
                        .orElse(defaultContentType);
                DefaultPubSubAcknowledgement pubSubAcknowledgement = new DefaultPubSubAcknowledgement(ackReplyConsumer);

                PubSubLiteConsumerState consumerState = new PubSubLiteConsumerState(message, ackReplyConsumer,
                        subscriptionPath, contentType);
                try {
                    BoundExecutable executable = null;
                    try {
                        executable = binder.bind(method, binderRegistry, consumerState);
                    } catch (Exception ex) {
                        handleException(new PubSubLiteMessageReceiverException("Error binding message to the method", ex, bean, consumerState));
                    }
                    executable.invoke(bean); // Discard result
                    if (!hasAckArg) { // if manual ack is not specified we auto ack message after method execution
                        pubSubAcknowledgement.ack();
                    } else {
                        Optional<Object> boundAck = Arrays.stream(executable.getBoundArguments()).filter(o -> (o instanceof DefaultPubSubAcknowledgement)).findFirst();
                        if (boundAck.isPresent()) {
                            DefaultPubSubAcknowledgement manualAck = (DefaultPubSubAcknowledgement) boundAck.get();
                            if (!manualAck.isClientAck()) {
                                logger.warn("Method {} was executed and no message acknowledge detected. Did you forget to invoke ack()/nack()?", method.getName());
                            }
                        }
                    }
                } catch (Exception e) {
                    handleException(new PubSubLiteMessageReceiverException("Error handling message", e, bean, consumerState));
                }
            };
            try {
                this.subscriberFactory.createSubscriber(new LiteSubscriberFactoryConfig(subscriptionPath, receiver, configuration));
            } catch (Exception e) {
                throw new PubSubListenerException("Failed to create subscriber", e);
            }

        }
    }

    private void handleException(PubSubLiteMessageReceiverException ex) {
        Object bean = ex.getListener();
        if (bean instanceof PubSubLiteMessageReceiverExceptionHandler) {
            ((PubSubLiteMessageReceiverExceptionHandler) bean).handle(ex);
        } else {
            exceptionHandler.handle(ex);
        }
    }
}
