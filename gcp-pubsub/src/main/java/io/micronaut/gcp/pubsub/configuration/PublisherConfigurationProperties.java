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
package io.micronaut.gcp.pubsub.configuration;

import com.google.api.gax.batching.BatchingSettings;
import com.google.api.gax.batching.FlowControlSettings;
import com.google.api.gax.retrying.RetrySettings;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.scheduling.TaskExecutors;

/**
 * Configuration properties for PubSub Publishers. Each topic has its own configuration if set by the user.
 * for example gcp.pubsub.publisher.animals and gcp.pubsub.publisher.cars would define publishers with different
 * configurations for each topic.
 *
 * @author Vinicius Carvalho
 * @author James Kleeh
 *
 * @since 2.0.0
 */
@EachProperty(PubSubConfigurationProperties.PREFIX + ".publisher")
public class PublisherConfigurationProperties {

    @ConfigurationBuilder(prefixes = "set", configurationPrefix = "retry")
    private RetrySettings.Builder retrySettings = RetrySettings.newBuilder();

    @ConfigurationBuilder(prefixes = "set", configurationPrefix = "batching")
    private BatchingSettings.Builder batchingSettings = BatchingSettings.newBuilder();

    @ConfigurationBuilder(prefixes = "set", configurationPrefix = "flow-control")
    private FlowControlSettings.Builder flowControlSettings = FlowControlSettings.newBuilder();

    private final String name;

    private String executor = TaskExecutors.SCHEDULED;

    public PublisherConfigurationProperties(@Parameter String name) {
        this.name = name;
    }

    /**
     * Retry policies.
     * @return the retry settings
     */
    public RetrySettings.Builder getRetrySettings() {
        return retrySettings;
    }

    /**
     * Retry policies.
     * @param retrySettings retry settings
     */
    public void setRetrySettings(RetrySettings.Builder retrySettings) {
        this.retrySettings = retrySettings;
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
    public FlowControlSettings getFlowControlSettings() {
        return flowControlSettings.build();
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
     * @return executor name
     */
    public String getExecutor() {
        return executor;
    }

    /**
     *
     * @param executor name to be set
     */
    public void setExecutor(String executor) {
        this.executor = executor;
    }
}
