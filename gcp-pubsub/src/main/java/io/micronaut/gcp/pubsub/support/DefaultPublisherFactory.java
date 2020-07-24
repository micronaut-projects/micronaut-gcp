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
package io.micronaut.gcp.pubsub.support;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedExecutorProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.ProjectTopicName;
import io.micronaut.context.BeanContext;
import io.micronaut.gcp.pubsub.configuration.PubSubConfigurationProperties;
import io.micronaut.gcp.pubsub.configuration.PublisherConfigurationProperties;
import io.micronaut.gcp.pubsub.exception.PubSubClientException;
import io.micronaut.inject.qualifiers.Qualifiers;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The default {@link PublisherFactory} implementation.
 *
 * <p>Creates {@link Publisher}s for topics once, caches and reuses them.
 *
 * Original source at : https://github.com/spring-cloud/spring-cloud-gcp/blob/master/spring-cloud-gcp-pubsub/src/main/java/org/springframework/cloud/gcp/pubsub/support/DefaultPublisherFactory.java
 *
 * @author João André Martins
 * @author Chengyuan Zhao
 * @author Vinicius Carvalho
 *
 * @since 2.0.0
 *
 */
@Singleton
public class DefaultPublisherFactory implements PublisherFactory {

    private final ConcurrentHashMap<ProjectTopicName, Publisher> publishers = new ConcurrentHashMap<>();
    private final TransportChannelProvider transportChannelProvider;
    private final CredentialsProvider credentialsProvider;
    private final Collection<PublisherConfigurationProperties> publisherConfigurationProperties;
    private final BeanContext beanContext;

    public DefaultPublisherFactory(TransportChannelProvider transportChannelProvider,
                                   CredentialsProvider credentialsProvider,
                                   Collection<PublisherConfigurationProperties> publisherConfigurationProperties,
                                   PubSubConfigurationProperties pubSubConfigurationProperties,
                                   BeanContext beanContext) {
        this.transportChannelProvider = transportChannelProvider;
        this.credentialsProvider = credentialsProvider;
        this.publisherConfigurationProperties = publisherConfigurationProperties;
        this.beanContext = beanContext;
    }

    /**
     * Creates and caches a publisher for a given topic.
     * @param config
     * @return
     */
    @Override
    public Publisher createPublisher(@Nonnull PublisherFactoryConfig config) {
        return this.publishers.computeIfAbsent(config.getTopicName(), (key) -> {
            try {
                Publisher.Builder publisherBuilder = Publisher.newBuilder(config.getTopicName());
                Optional<PublisherConfigurationProperties> publisherConfiguration = publisherConfigurationProperties.stream().filter(p -> p.getName().equals(config.getPublisherConfiguration())).findFirst();
                String executor = publisherConfiguration.map(p -> p.getExecutor()).orElse(config.getDefaultExecutor());
                ExecutorService executorService = beanContext.getBean(ExecutorService.class, Qualifiers.byName(executor));
                if (publisherConfiguration.isPresent()) {
                    publisherBuilder.setRetrySettings(publisherConfiguration.get().getRetrySettings().build());
                    publisherBuilder.setBatchingSettings(publisherConfiguration.get().getBatchingSettings().build());
                }
                if (!(executorService instanceof ScheduledExecutorService)) {
                    throw new IllegalStateException("Invalid Executor type provided, please make sure you have a ScheduledExecutorService configured for Publisher: "  + config.getTopicName().getTopic());
                }
                publisherBuilder.setExecutorProvider(FixedExecutorProvider.create((ScheduledExecutorService) executorService));
                publisherBuilder.setChannelProvider(this.transportChannelProvider);
                publisherBuilder.setCredentialsProvider(this.credentialsProvider);
                return publisherBuilder.build();
            } catch (Exception ex) {
                throw new PubSubClientException("Failed to create subscriber", ex);
            }
        });
    }

}
