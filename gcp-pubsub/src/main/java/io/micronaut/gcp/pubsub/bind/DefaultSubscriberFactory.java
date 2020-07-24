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
package io.micronaut.gcp.pubsub.bind;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedExecutorProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriberInterface;
import com.google.pubsub.v1.ProjectSubscriptionName;
import io.micronaut.context.BeanContext;
import io.micronaut.gcp.pubsub.configuration.SubscriberConfigurationProperties;
import io.micronaut.gcp.pubsub.exception.PubSubListenerException;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Default implementation of {@link SubscriberFactory}.
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Singleton
public class DefaultSubscriberFactory implements SubscriberFactory, AutoCloseable {

    private final ConcurrentHashMap<ProjectSubscriptionName, Subscriber> subscribers = new ConcurrentHashMap<>();
    private final TransportChannelProvider transportChannelProvider;
    private final CredentialsProvider credentialsProvider;
    private final BeanContext beanContext;
    private final Collection<SubscriberConfigurationProperties> subscriberConfigurationProperties;
    private final Logger logger = LoggerFactory.getLogger(DefaultSubscriberFactory.class);

    public DefaultSubscriberFactory(TransportChannelProvider transportChannelProvider,
                                    CredentialsProvider credentialsProvider,
                                    BeanContext beanContext,
                                    Collection<SubscriberConfigurationProperties> subscriberConfigurationProperties
                                    ) {
        this.transportChannelProvider = transportChannelProvider;
        this.credentialsProvider = credentialsProvider;
        this.beanContext = beanContext;
        this.subscriberConfigurationProperties = subscriberConfigurationProperties;
    }

    @Override
    public Subscriber createSubscriber(SubscriberFactoryConfig config) {
        Subscriber subscriber = subscribers.compute(config.getSubscriptionName(), (k, v) -> {
            if (v == null) {
                Subscriber.Builder builder = Subscriber.newBuilder(config.getSubscriptionName(), config.getReceiver())
                        .setChannelProvider(this.transportChannelProvider)
                        .setCredentialsProvider(this.credentialsProvider);

                Optional<SubscriberConfigurationProperties> subscriberConfiguration = subscriberConfigurationProperties.stream().filter(p -> p.getName().equals(config.getSubscriberConfiguration())).findFirst();
                String executor = subscriberConfiguration.map(s -> s.getExecutor()).orElse(config.getDefaultExecutor());
                ExecutorService executorService = beanContext.getBean(ExecutorService.class, Qualifiers.byName(executor));
                if (!(executorService instanceof ScheduledExecutorService)) {
                    throw new IllegalStateException("Invalid Executor type provided, please make sure you have a ScheduledExecutorService configured for Subscriber: "  + config.getSubscriptionName().getSubscription());
                }
                builder.setExecutorProvider(FixedExecutorProvider.create((ScheduledExecutorService) executorService));
                if (subscriberConfiguration.isPresent()) {
                    SubscriberConfigurationProperties properties = subscriberConfiguration.get();
                    builder.setMaxAckExtensionPeriod(properties.getMaxAckExtensionPeriod());
                    builder.setParallelPullCount(properties.getParallelPullCount());
                    builder.setMaxDurationPerAckExtension(properties.getMaxDurationPerAckExtension());
                }
                return builder.build();
            }
            throw new PubSubListenerException(String.format("Subscription %s is already registered for another" +
                    " method", config.getSubscriptionName().toString()));
        });
        subscriber.startAsync();
        return subscriber;
    }

    @PreDestroy
    @Override
    public void close() throws Exception {
        while (!subscribers.entrySet().isEmpty()) {
            Iterator<Map.Entry<ProjectSubscriptionName, Subscriber>> it = subscribers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<ProjectSubscriptionName, Subscriber> entry = it.next();
                SubscriberInterface subscriber = entry.getValue();
                try {
                    subscriber.stopAsync().awaitTerminated();
                } catch (Exception e) {
                    logger.error("Failed stopping subscriber for " + entry.getKey(), e);
                } finally {
                    it.remove();
                }

            }
        }
    }
}
