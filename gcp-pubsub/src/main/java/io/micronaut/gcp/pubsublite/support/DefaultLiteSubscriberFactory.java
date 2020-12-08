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
package io.micronaut.gcp.pubsublite.support;

import com.google.cloud.pubsublite.MessageTransformer;
import com.google.cloud.pubsublite.Partition;
import com.google.cloud.pubsublite.SequencedMessage;
import com.google.cloud.pubsublite.SubscriptionPath;
import com.google.cloud.pubsublite.cloudpubsub.FlowControlSettings;
import com.google.cloud.pubsublite.cloudpubsub.NackHandler;
import com.google.cloud.pubsublite.cloudpubsub.Subscriber;
import com.google.cloud.pubsublite.cloudpubsub.SubscriberSettings;
import com.google.cloud.pubsublite.v1.CursorServiceClient;
import com.google.cloud.pubsublite.v1.SubscriberServiceClient;
import com.google.common.annotations.VisibleForTesting;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.context.BeanContext;
import io.micronaut.gcp.pubsub.exception.PubSubListenerException;
import io.micronaut.gcp.pubsublite.configuration.LiteSubscriberConfigurationProperties;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link LiteSubscriberFactory}.
 *
 * Based on {@link io.micronaut.gcp.pubsub.bind.DefaultSubscriberFactory}
 *
 * @author Jacob Mims
 * @since 2.2.0
 */
@Singleton
public class DefaultLiteSubscriberFactory implements LiteSubscriberFactory, AutoCloseable  {
    private final ConcurrentHashMap<SubscriptionPath, Subscriber> subscribers = new ConcurrentHashMap<>();
    private final BeanContext beanContext;
    private final Logger logger = LoggerFactory.getLogger(DefaultLiteSubscriberFactory.class);

    public DefaultLiteSubscriberFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public Subscriber createSubscriber(LiteSubscriberFactoryConfig config) {
        Subscriber subscriber = subscribers.compute(config.getSubscriptionPath(), (k, v) -> {
            if (v == null) {
                SubscriberSettings.Builder builder = SubscriberSettings.newBuilder()
                        .setSubscriptionPath(config.getSubscriptionPath())
                        .setReceiver(config.getReceiver());

                Optional<LiteSubscriberConfigurationProperties> subscriberConfiguration =
                        beanContext.findBean(LiteSubscriberConfigurationProperties.class,
                                Qualifiers.byName(config.getSubscriberConfiguration()));

                if (subscriberConfiguration.isPresent()) {
                    LiteSubscriberConfigurationProperties subscriberConfigurationProperties = subscriberConfiguration.get();

                    if (subscriberConfigurationProperties.getFlowControlSettings() != null) {
                        builder.setPerPartitionFlowControlSettings(subscriberConfigurationProperties.getFlowControlSettings().build());
                    }

                    if (subscriberConfigurationProperties.getCursorServiceClientSupplier() != null) {
                        Supplier<CursorServiceClient> clientSupplier = beanContext.findBean(Supplier.class,
                                Qualifiers.byName(subscriberConfigurationProperties.getCursorServiceClientSupplier())).orElse(null);
                        if (clientSupplier != null) {
                            builder.setCursorServiceClientSupplier(clientSupplier);
                        } else {
                            logger.warn("Could not find Supplier<CursorServiceClient> bean [{}] for subscriber [{}].",
                                    subscriberConfigurationProperties.getCursorServiceClientSupplier(),
                                    config.getSubscriberConfiguration());
                        }
                    }

                    if (subscriberConfigurationProperties.getSubscriberServiceClientSupplier() != null) {
                        Supplier<SubscriberServiceClient> clientSupplier = beanContext.findBean(Supplier.class,
                                Qualifiers.byName(subscriberConfigurationProperties.getSubscriberServiceClientSupplier())).orElse(null);
                        if (clientSupplier != null) {
                            builder.setSubscriberServiceClientSupplier(clientSupplier);
                        } else {
                            logger.warn("Could not find Supplier<SubscriberServiceClient> bean [{}] for subscriber [{}].",
                                    subscriberConfigurationProperties.getSubscriberServiceClientSupplier(),
                                    config.getSubscriberConfiguration());
                        }
                    }

                    if (subscriberConfigurationProperties.getNackHandler() != null) {
                        NackHandler nackHandler = beanContext.findBean(NackHandler.class,
                                Qualifiers.byName(subscriberConfigurationProperties.getNackHandler())).orElse(null);
                        if (nackHandler != null) {
                            builder.setNackHandler(nackHandler);
                        } else {
                            logger.warn("Could not find NackHandler bean [{}] for subscriber [{}].",
                                    subscriberConfigurationProperties.getNackHandler(),
                                    config.getSubscriberConfiguration());
                        }
                    }

                    if (subscriberConfigurationProperties.getMessageTransformer() != null) {
                        MessageTransformer<SequencedMessage, PubsubMessage> messageTransformer =
                                beanContext.findBean(MessageTransformer.class,
                                        Qualifiers.byName(subscriberConfigurationProperties.getMessageTransformer()))
                                        .orElse(null);
                        if (messageTransformer != null) {
                            builder.setTransformer(messageTransformer);
                        } else {
                            logger.warn("Could not find MessageTransformer<SequencedMessage, PubsubMessage> bean [{}] for subscriber [{}].",
                                    subscriberConfigurationProperties.getMessageTransformer(),
                                    config.getSubscriberConfiguration());
                        }
                    }

                    if (subscriberConfigurationProperties.getPartitions() != null &&
                            subscriberConfigurationProperties.getPartitions().size() > 0) {
                        builder.setPartitions(subscriberConfigurationProperties.getPartitions()
                                .stream().map(Partition::of)
                                .collect(Collectors.toList()));
                    }
                } else {
                    builder.setPerPartitionFlowControlSettings(FlowControlSettings.builder()
                            .setMessagesOutstanding(1000L)
                            .setBytesOutstanding(100L * 1024L * 1024L)
                            .build());
                }

                return startSubscriber(builder);
            }
            throw new PubSubListenerException(String.format("Subscription %s is already registered for another" +
                    " method", config.getSubscriptionPath().toString()));
        });

        return subscriber;
    }

    /**
     * Encapsulation to allow for testing subscriber creation without calling
     * the Google API.
     * @param subscriberSettings Settings for the subscriber
     * @return A subscriber in the process of starting.
     */
    @VisibleForTesting
    public Subscriber startSubscriber(SubscriberSettings.Builder subscriberSettings) {
        Subscriber subscriber = Subscriber.create(subscriberSettings.build());
        subscriber.startAsync();
        return subscriber;
    }

    @PreDestroy
    @Override
    public void close() {
        while (!subscribers.entrySet().isEmpty()) {
            Iterator<Map.Entry<SubscriptionPath, Subscriber>> it = subscribers.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<SubscriptionPath, Subscriber> entry = it.next();
                Subscriber subscriber = entry.getValue();
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
