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
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.context.BeanContext;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.bind.DefaultExecutableBinder;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.pubsub.annotation.PubSubListener;
import io.micronaut.gcp.pubsub.annotation.Subscription;
import io.micronaut.gcp.pubsub.bind.DefaultPubSubAcknowledgement;
import io.micronaut.gcp.pubsub.bind.PubSubBinderRegistry;
import io.micronaut.gcp.pubsub.bind.PubSubConsumerState;
import io.micronaut.gcp.pubsub.bind.SubscriberFactory;
import io.micronaut.gcp.pubsub.exception.PubSubListenerException;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDes;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDesRegistry;
import io.micronaut.gcp.pubsub.support.PubSubSubscriptionUtils;
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
 * {@link com.google.cloud.pubsub.v1.MessageReceiver} that subscribes to the PubSub subscription
 * and invoke methods annotated by @{@link io.micronaut.gcp.pubsub.annotation.Subscription}.
 *
 * There can be only one subscriber for any given subscription (in order to avoid issues with message
 * Ack control). Having more than one method using the same subscription raises a {@link io.micronaut.gcp.pubsub.exception.PubSubListenerException}.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Singleton
public class PubSubConsumerAdvice implements ExecutableMethodProcessor<PubSubListener> {

    private final Logger logger = LoggerFactory.getLogger(PubSubConsumerAdvice.class);
    private final BeanContext beanContext;
    private final ConversionService<?> conversionService;
    private final PubSubMessageSerDesRegistry serDesRegistry;
    private final SubscriberFactory subscriberFactory;
    private final GoogleCloudConfiguration googleCloudConfiguration;
    private final PubSubBinderRegistry binderRegistry;


    public PubSubConsumerAdvice(BeanContext beanContext,
                                ConversionService<?> conversionService,
                                PubSubMessageSerDesRegistry serDesRegistry,
                                SubscriberFactory subscriberFactory,
                                GoogleCloudConfiguration googleCloudConfiguration,
                                PubSubBinderRegistry binderRegistry) {
        this.beanContext = beanContext;
        this.conversionService = conversionService;
        this.serDesRegistry = serDesRegistry;
        this.subscriberFactory = subscriberFactory;
        this.googleCloudConfiguration = googleCloudConfiguration;
        this.binderRegistry = binderRegistry;
    }

    @Override
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        AnnotationValue<Subscription> subscriptionAnnotation = method.getAnnotation(Subscription.class);
        io.micronaut.context.Qualifier<Object> qualifer = beanDefinition
                .getAnnotationTypeByStereotype(Qualifier.class)
                .map(type -> Qualifiers.byAnnotation(beanDefinition, type))
                .orElse(null);
        boolean hasAckArg = Arrays.stream(method.getArguments())
                .anyMatch(arg -> Acknowledgement.class.isAssignableFrom(arg.getType()));

        Class<Object> beanType = (Class<Object>) beanDefinition.getBeanType();
        Object bean = beanContext.findBean(beanType, qualifer).orElseThrow(() -> new MessageListenerException("Could not find the bean to execute the method " + method));
        DefaultExecutableBinder<PubSubConsumerState> binder = new DefaultExecutableBinder<>();

        if (subscriptionAnnotation != null) {
            String subscriptionName = subscriptionAnnotation.getRequiredValue(String.class);
            ProjectSubscriptionName projectSubscriptionName = PubSubSubscriptionUtils.toProjectSubscriptionName(subscriptionName, googleCloudConfiguration.getProjectId());
            String defaultContentType = subscriptionAnnotation.get("contentType", String.class).orElse("");
            MessageReceiver receiver = (PubsubMessage message, AckReplyConsumer consumer) -> {
                String messageContentType = message.getAttributesMap().getOrDefault("Content-Type", "");
                String contentType = Optional.of(messageContentType).orElse(defaultContentType);
                DefaultPubSubAcknowledgement pubSubAcknowledgement = new DefaultPubSubAcknowledgement(consumer);

                PubSubConsumerState consumerState = new PubSubConsumerState(message, consumer, contentType);
                try {
                    BoundExecutable executable = binder.bind(method, binderRegistry, consumerState);
                    executable.invoke(bean); // Discard result
                    if (!hasAckArg) { // if manual ack is not specified we auto ack message after method execution
                        pubSubAcknowledgement.ack();
                    } else {
                        if (!pubSubAcknowledgement.isMessageAcked()) {
                            logger.warn("Method {} was executed and no message acknowledge detected, message will be redelivered.", method.getName());
                        }
                    }
                } catch (Exception e) {
                    throw new PubSubListenerException("");
                }
            };
            Subscriber subscriber = this.subscriberFactory.createSubscriber(projectSubscriptionName, receiver);
        }

    }

}
