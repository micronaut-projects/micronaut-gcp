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
import io.micronaut.core.convert.ConversionService;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.pubsub.annotation.Subscription;
import io.micronaut.gcp.pubsub.bind.PubSubBinderRegistry;
import io.micronaut.gcp.pubsub.bind.SubscriberFactory;
import io.micronaut.gcp.pubsub.bind.SubscriberFactoryConfig;
import io.micronaut.gcp.pubsub.configuration.PubSubConfigurationProperties;
import io.micronaut.gcp.pubsub.exception.PubSubListenerException;
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverExceptionHandler;
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDesRegistry;
import jakarta.inject.Singleton;


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
public class PubSubConsumerAdvice extends AbstractPubSubConsumerMethodProcessor<Subscription> {

    private final SubscriberFactory subscriberFactory;
    private final PubSubConfigurationProperties pubSubConfigurationProperties;

    public PubSubConsumerAdvice(BeanContext beanContext,
                                ConversionService conversionService,
                                PubSubMessageSerDesRegistry serDesRegistry,
                                SubscriberFactory subscriberFactory,
                                GoogleCloudConfiguration googleCloudConfiguration,
                                PubSubConfigurationProperties pubSubConfigurationProperties,
                                PubSubBinderRegistry binderRegistry,
                                PubSubMessageReceiverExceptionHandler exceptionHandler) {
        super(Subscription.class, beanContext, conversionService, googleCloudConfiguration, binderRegistry, exceptionHandler);
        this.subscriberFactory = subscriberFactory;
        this.pubSubConfigurationProperties = pubSubConfigurationProperties;
    }

    /**
     * If nack on shutdown is enabled and shutdown has been initiated, immediately nacks all remaining messages that
     * may be queued in memory.
     *
     * @param message the pub sub message being processed
     * @param ackReplyConsumer the ack reply consumer for the message being processed
     * @return false if messages are immediately nacked
     */
    @Override
    protected boolean doBeforeSubscriber(PubsubMessage message, AckReplyConsumer ackReplyConsumer) {
        if (pubSubConfigurationProperties.isNackOnShutdown() && isShutDownInitiated()) {
            ackReplyConsumer.nack();
            return false;
        }
        return true;
    }

    @Override
    protected void addSubscriber(ProjectSubscriptionName projectSubscriptionName, MessageReceiver receiver, String configuration) {
        try {
            this.subscriberFactory.createSubscriber(new SubscriberFactoryConfig(projectSubscriptionName, receiver, configuration, pubSubConfigurationProperties.getSubscribingExecutor()));
        } catch (Exception e) {
            throw new PubSubListenerException("Failed to create subscriber", e);
        }
    }
}
