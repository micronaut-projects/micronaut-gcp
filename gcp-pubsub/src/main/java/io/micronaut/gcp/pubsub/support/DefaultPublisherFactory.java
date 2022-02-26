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

import com.google.api.gax.batching.BatchingSettings;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedExecutorProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import io.micronaut.context.BeanContext;
import io.micronaut.core.util.StringUtils;
import io.micronaut.gcp.Modules;
import io.micronaut.gcp.pubsub.configuration.PublisherConfigurationProperties;
import io.micronaut.gcp.pubsub.exception.PubSubClientException;
import io.micronaut.inject.qualifiers.Qualifiers;

import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The default {@link PublisherFactory} implementation.
 *
 * <p>Creates {@link Publisher}s for topics once, caches and reuses them.
 * <p>
 * Original source at : https://github.com/spring-cloud/spring-cloud-gcp/blob/master/spring-cloud-gcp-pubsub/src/main/java/org/springframework/cloud/gcp/pubsub/support/DefaultPublisherFactory.java
 *
 * @author João André Martins
 * @author Chengyuan Zhao
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Singleton
public class DefaultPublisherFactory implements PublisherFactory {

    private final TransportChannelProvider transportChannelProvider;
    private final CredentialsProvider credentialsProvider;
    private final BeanContext beanContext;

    public DefaultPublisherFactory(@Named(Modules.PUBSUB) TransportChannelProvider transportChannelProvider,
                                   @Named(Modules.PUBSUB) CredentialsProvider credentialsProvider,
                                   BeanContext beanContext) {
        this.transportChannelProvider = transportChannelProvider;
        this.credentialsProvider = credentialsProvider;
        this.beanContext = beanContext;
    }

    /**
     * Creates a publisher for a given topic.
     *
     * @param config {@link PublisherFactoryConfig} object containing all required properties.
     * @return An instance of {@link Publisher} configured using the config and environment properties from `gcp.pubsub.publisher.&gt;config_name&lt;`
     */
    @Override
    public Publisher createPublisher(@NonNull PublisherFactoryConfig config) {
        try {
            Publisher.Builder publisherBuilder = Publisher.newBuilder(config.getTopicState().getProjectTopicName());
            Optional<PublisherConfigurationProperties> publisherConfiguration = beanContext.findBean(PublisherConfigurationProperties.class, Qualifiers.byName(config.getTopicState().getConfigurationName()));
            String executor = publisherConfiguration.map(p -> p.getExecutor()).orElse(config.getDefaultExecutor());
            ExecutorService executorService = beanContext.getBean(ExecutorService.class, Qualifiers.byName(executor));
            publisherBuilder.setEnableMessageOrdering(config.getTopicState().getOrdered());
            if (!StringUtils.isEmpty(config.getTopicState().getEndpoint())) {
                publisherBuilder.setEndpoint(config.getTopicState().getEndpoint());
            }
            if (publisherConfiguration.isPresent()) {

                publisherBuilder.setRetrySettings(publisherConfiguration.get().getRetrySettings().build());
                //FlowControl had to be flatten in order to be parsed
                BatchingSettings batchSettings = publisherConfiguration.get().getBatchingSettings().build();
                publisherBuilder.setBatchingSettings(BatchingSettings.newBuilder()
                        .setDelayThreshold(batchSettings.getDelayThreshold())
                        .setElementCountThreshold(batchSettings.getElementCountThreshold())
                        .setIsEnabled(batchSettings.getIsEnabled())
                        .setRequestByteThreshold(batchSettings.getRequestByteThreshold())
                        .setFlowControlSettings(publisherConfiguration.get().getFlowControlSettings().build())
                        .build());
            }
            if (!(executorService instanceof ScheduledExecutorService)) {
                throw new IllegalStateException("Invalid Executor type provided, please make sure you have a ScheduledExecutorService configured for Publisher: " + config.getTopicState().getProjectTopicName().getTopic());
            }
            publisherBuilder.setExecutorProvider(FixedExecutorProvider.create((ScheduledExecutorService) executorService));
            publisherBuilder.setChannelProvider(this.transportChannelProvider);
            publisherBuilder.setCredentialsProvider(this.credentialsProvider);
            return publisherBuilder.build();
        } catch (Exception ex) {
            throw new PubSubClientException("Failed to create subscriber", ex);
        }
    }

}
