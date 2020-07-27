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

import com.google.api.gax.batching.FlowControlSettings;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.scheduling.TaskExecutors;
import org.threeten.bp.Duration;

/**
 * Configuration properties for PubSub {@link com.google.cloud.pubsub.v1.Subscriber}. Each topic has its own configuration if set by the user.
 * for example gcp.pubsub.publisher.animals and gcp.pubsub.publisher.cars would define subscribers with different
 * configurations for each {@link io.micronaut.gcp.pubsub.annotation.Subscription}.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 *
 */
@EachProperty(PubSubConfigurationProperties.PREFIX + ".subscriber")
public class SubscriberConfigurationProperties {
    private static final Duration DEFAULT_MAX_ACK_EXTENSION_PERIOD = Duration.ofMinutes(60);

    private final String name;

    private String executor = TaskExecutors.SCHEDULED;

    private Integer parallelPullCount = 1;
    private Duration maxAckExtensionPeriod = DEFAULT_MAX_ACK_EXTENSION_PERIOD;
    private Duration maxDurationPerAckExtension = Duration.ofMillis(0);

    @ConfigurationBuilder(prefixes = "set", configurationPrefix = "flow-control")
    private FlowControlSettings.Builder flowControlSettings = FlowControlSettings
            .newBuilder()
            .setMaxOutstandingElementCount(1000L)
            .setMaxOutstandingRequestBytes(100L * 1024L * 1024L);

    /**
     *
     * @param name of this configuration.
     */
    public SubscriberConfigurationProperties(@Parameter String name) {
        this.name = name;
    }

    /**
     *
     * @return number of concurrent pulls
     */
    public Integer getParallelPullCount() {
        return parallelPullCount;
    }

    /**
     *
     * @param parallelPullCount number of concurrent pulls
     */
    public void setParallelPullCount(Integer parallelPullCount) {
        this.parallelPullCount = parallelPullCount;
    }

    /**
     * Set the maximum period a message ack deadline will be extended. Defaults to one hour.
     * @return max ack deadline.
     */
    public Duration getMaxAckExtensionPeriod() {
        return maxAckExtensionPeriod;
    }

    /**
     * Set the maximum period a message ack deadline will be extended. Defaults to one hour.
     * @param maxAckExtensionPeriod value
     */
    public void setMaxAckExtensionPeriod(Duration maxAckExtensionPeriod) {
        this.maxAckExtensionPeriod = maxAckExtensionPeriod;
    }

    /**
     *
     * @return the maxDurationPerAckExtension
     */
    public Duration getMaxDurationPerAckExtension() {
        return maxDurationPerAckExtension;
    }

    /**
     * Set the upper bound for a single mod ack extention period.
     *
     * <p>The ack deadline will continue to be extended by up to this duration until
     * MaxAckExtensionPeriod is reached. Setting MaxDurationPerAckExtension bounds the maximum
     * amount of time before a mesage re-delivery in the event the Subscriber fails to extend the
     * deadline.
     *
     * <p>MaxDurationPerAckExtension configuration can be disabled by specifying a zero duration.
     * @param maxDurationPerAckExtension value
     */
    public void setMaxDurationPerAckExtension(Duration maxDurationPerAckExtension) {
        this.maxDurationPerAckExtension = maxDurationPerAckExtension;
    }

    /**
     *
     * @return the name of the configuration
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
}
