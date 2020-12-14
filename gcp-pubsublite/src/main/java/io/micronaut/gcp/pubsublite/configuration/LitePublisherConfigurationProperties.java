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
package io.micronaut.gcp.pubsublite.configuration;

import com.google.api.gax.batching.BatchingSettings;
import com.google.api.gax.batching.FlowControlSettings;
import com.google.api.gax.batching.FlowController;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import org.threeten.bp.Duration;

/**
 * Configuration properties for PubSub Lite Publishers. Each topic has its own configuration if set by the user.
 * for example gcp.pubsublite.publisher.animals and gcp.pubsublite.publisher.cars would define publishers with different
 * configurations for each topic.
 *
 * Based on {@link io.micronaut.gcp.pubsub.configuration.PublisherConfigurationProperties}.
 * See {@link com.google.cloud.pubsublite.cloudpubsub.PublisherSettings for more information}
 *
 * @author Jacob Mims
 *
 * @since 2.2.0
 */
@EachProperty(value = PubSubLiteConfigurationProperties.PREFIX + ".publisher")
public class LitePublisherConfigurationProperties {
    // Meaningful defaults.
    static final long DEFAULT_ELEMENT_COUNT_THRESHOLD = 100L;
    static final long DEFAULT_REQUEST_BYTES_THRESHOLD = 1000L; // 1 kB
    static final Duration DEFAULT_DELAY_THRESHOLD = Duration.ofMillis(1);

    @ConfigurationBuilder(prefixes = "set", configurationPrefix = "batching")
    private BatchingSettings.Builder batchingSettings = BatchingSettings
            .newBuilder()
            .setDelayThreshold(DEFAULT_DELAY_THRESHOLD)
            .setRequestByteThreshold(DEFAULT_REQUEST_BYTES_THRESHOLD)
            .setElementCountThreshold(DEFAULT_ELEMENT_COUNT_THRESHOLD);

    @ConfigurationBuilder(prefixes = "set", configurationPrefix = "flow-control")
    private FlowControlSettings.Builder flowControlSettings = FlowControlSettings.newBuilder()
            .setLimitExceededBehavior(FlowController.LimitExceededBehavior.Ignore);

    private String serviceClientSupplier;

    private String keyExtractor;

    private String messageTransformer;

    private final String name;

    public LitePublisherConfigurationProperties(@Parameter String name) {
        this.name = name;
    }

    /**
     * Controls message publishing batch.
     * @return batchSettings
     */
    public BatchingSettings.Builder getBatchingSettings() {
        return batchingSettings;
    }

    /**
     * Controls message publishing batch.
     * @param batchingSettings batchingSettings
     */
    public void setBatchingSettings(BatchingSettings.Builder batchingSettings) {
        this.batchingSettings = batchingSettings;
    }

    /**
     * Flow Control settings.
     * @return flowControlSettings
     */
    public FlowControlSettings.Builder getFlowControlSettings() {
        return flowControlSettings;
    }

    /**
     * Flow Control settings.
     * @param flowControlSettings flow control settings
     */
    public void setFlowControlSettings(FlowControlSettings.Builder flowControlSettings) {
        this.flowControlSettings = flowControlSettings;
    }

    /**
     *
     * @return the name of this configuration
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return message transformer bean name
     */
    public String getMessageTransformer() {
        return messageTransformer;
    }

    /**
     *
     * @param messageTransformer bean name
     */
    public void setMessageTransformer(String messageTransformer) {
        this.messageTransformer = messageTransformer;
    }

    /**
     *
     * @return service client supplier bean name
     */
    public String getServiceClientSupplier() {
        return serviceClientSupplier;
    }

    /**
     *
     * @param serviceClientSupplier bean name
     */
    public void setServiceClientSupplier(String serviceClientSupplier) {
        this.serviceClientSupplier = serviceClientSupplier;
    }

    /**
     *
     * @return key extractor bean name
     */
    public String getKeyExtractor() {
        return keyExtractor;
    }

    /**
     *
     * @param keyExtractor bean name
     */
    public void setKeyExtractor(String keyExtractor) {
        this.keyExtractor = keyExtractor;
    }
}
