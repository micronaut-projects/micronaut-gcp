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

import com.google.cloud.pubsublite.Message;
import com.google.cloud.pubsublite.MessageTransformer;
import com.google.cloud.pubsublite.TopicPath;
import com.google.cloud.pubsublite.cloudpubsub.KeyExtractor;
import com.google.cloud.pubsublite.cloudpubsub.Publisher;
import com.google.cloud.pubsublite.cloudpubsub.PublisherSettings;
import com.google.cloud.pubsublite.v1.PublisherServiceClient;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.pubsub.v1.PubsubMessage;
import io.micronaut.context.BeanContext;
import io.micronaut.gcp.pubsub.exception.PubSubClientException;
import io.micronaut.gcp.pubsublite.configuration.LitePublisherConfigurationProperties;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The default {@link LitePublisherFactory} implementation.
 *
 * <p>Creates {@link Publisher}s for topics once, caches and reuses them.
 * <p>
 * Based on {@link io.micronaut.gcp.pubsub.support.DefaultPublisherFactory}.
 *
 * @author Jacob Mims
 * @since 2.2.0
 */
@Singleton
public class DefaultLitePublisherFactory implements LitePublisherFactory {

    private final BeanContext beanContext;
    private final Logger logger = LoggerFactory.getLogger(DefaultLitePublisherFactory.class);

    public DefaultLitePublisherFactory(BeanContext beanContext) {
        this.beanContext = beanContext;
    }

    @Override
    public Publisher createLitePublisher(LitePublisherFactoryConfig config) {
        try {
            TopicPath topicPath = Preconditions.checkNotNull(config.getTopicState().getTopicPath(),
                    "PubSub Lite requires a TopicPath");
            PublisherSettings.Builder publisherSettings = PublisherSettings.newBuilder();
            publisherSettings.setTopicPath(topicPath);

            Optional<LitePublisherConfigurationProperties> publisherConfiguration =
                    beanContext.findBean(LitePublisherConfigurationProperties.class,
                            Qualifiers.byName(config.getTopicState().getConfigurationName()));

            if (publisherConfiguration.isPresent()) {
                LitePublisherConfigurationProperties publisherConfigurationProperties = publisherConfiguration.get();
                publisherSettings.setBatchingSettings(publisherConfigurationProperties.getBatchingSettings()
                        .setFlowControlSettings(publisherConfigurationProperties.getFlowControlSettings().build())
                        .build());

                if (publisherConfigurationProperties.getServiceClientSupplier() != null) {
                    Supplier<PublisherServiceClient> serviceClientSupplier =
                            beanContext.findBean(Supplier.class,
                                    Qualifiers.byName(publisherConfigurationProperties.getServiceClientSupplier()))
                                    .orElse(null);
                    if (serviceClientSupplier != null) {
                        publisherSettings.setServiceClientSupplier(serviceClientSupplier);
                    } else {
                        logger.warn("Could not find Supplier<PublisherServiceClient> bean [{}] for publisher [{}].",
                                publisherConfigurationProperties.getServiceClientSupplier(),
                                config.getTopicState().getConfigurationName());
                    }
                }

                if (publisherConfigurationProperties.getKeyExtractor() != null) {
                    KeyExtractor keyExtractor =
                            beanContext.findBean(KeyExtractor.class,
                                    Qualifiers.byName(publisherConfigurationProperties.getKeyExtractor()))
                                    .orElse(null);
                    if (keyExtractor != null) {
                        publisherSettings.setKeyExtractor(keyExtractor);
                    } else {
                        logger.warn("Could not find KeyExtractor bean [{}] for publisher [{}].",
                                publisherConfigurationProperties.getKeyExtractor(),
                                config.getTopicState().getConfigurationName());
                    }
                }

                if (publisherConfigurationProperties.getMessageTransformer() != null) {
                    MessageTransformer<PubsubMessage, Message> messageTransformer =
                            beanContext.findBean(MessageTransformer.class,
                                    Qualifiers.byName(publisherConfigurationProperties.getMessageTransformer()))
                                    .orElse(null);
                    if (messageTransformer != null) {
                        publisherSettings.setMessageTransformer(messageTransformer);
                    } else {
                        logger.warn("Could not find MessageTransformer<PubsubMessage, Message> bean [{}] for publisher [{}].",
                                publisherConfigurationProperties.getMessageTransformer(),
                                config.getTopicState().getConfigurationName());
                    }
                }
            }

            return startPublisher(publisherSettings);
        } catch (Exception ex) {
            throw new PubSubClientException("Failed to create publisher", ex);
        }
    }

    /**
     * Encapsulation to allow for testing publisher creation without calling
     * the Google API.
     * @param publisherSettings Settings for the publisher
     * @return A started publisher
     */
    @VisibleForTesting
    public Publisher startPublisher(PublisherSettings.Builder publisherSettings) {
        Publisher publisher = Publisher.create(publisherSettings.build());
        publisher.startAsync().awaitRunning();
        return publisher;
    }
}
