package io.micronaut.gcp.pubsub.configuration;

import com.google.api.gax.batching.BatchingSettings;
import com.google.api.gax.batching.FlowControlSettings;
import com.google.api.gax.retrying.RetrySettings;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties(PubSubConfigurationProperties.PREFIX + ".publisher")
public class PublisherConfigurationProperties {

    private int executorThreads = 4;

    @ConfigurationBuilder(prefixes = "set", configurationPrefix = "retry")
    private RetrySettings.Builder retrySettings = RetrySettings.newBuilder();

    @ConfigurationBuilder(prefixes = "set", configurationPrefix = "batching")
    private BatchingSettings.Builder batchingSettings = BatchingSettings.newBuilder();

    @ConfigurationBuilder(prefixes = "set", configurationPrefix = "flow-control")
    private FlowControlSettings.Builder flowControlSettings = FlowControlSettings.newBuilder();

    /**
     * Number of threads used by every publisher.
     * @return executorThreads
     */
    public int getExecutorThreads() {
        return this.executorThreads;
    }

    /**
     * Number of threads used by every publisher.
     * @param executorThreads
     */
    public void setExecutorThreads(int executorThreads) {
        this.executorThreads = executorThreads;
    }

    public RetrySettings.Builder getRetrySettings() {
        return retrySettings;
    }

    public void setRetrySettings(RetrySettings.Builder retrySettings) {
        this.retrySettings = retrySettings;
    }

    public BatchingSettings.Builder getBatchingSettings() {
        return batchingSettings;
    }

    public void setBatchingSettings(BatchingSettings.Builder batchingSettings) {
        this.batchingSettings = batchingSettings;
    }

    public FlowControlSettings getFlowControlSettings() {
        return flowControlSettings.build();
    }

    public void setFlowControlSettings(FlowControlSettings.Builder flowControlSettings) {
        this.flowControlSettings = flowControlSettings;
    }
}
