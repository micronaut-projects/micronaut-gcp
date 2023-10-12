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

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.scheduling.TaskExecutors;

/**
 * Configuration properties for PubSub support.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@ConfigurationProperties(PubSubConfigurationProperties.PREFIX)
public class PubSubConfigurationProperties {

    public static final String PREFIX = GoogleCloudConfiguration.PREFIX + ".pubsub";
    public static final boolean DEFAULT_NACK_ON_SHUTDOWN = false;
    private int keepAliveIntervalMinutes = 5;

    private String publishingExecutor = TaskExecutors.SCHEDULED;

    private String subscribingExecutor = TaskExecutors.SCHEDULED;

    private String topicEndpoint = "";

    private boolean nackOnShutdown = DEFAULT_NACK_ON_SHUTDOWN;

    /**
     * The name of the {@link java.util.concurrent.ScheduledExecutorService} to be used by all {@link com.google.cloud.pubsub.v1.Publisher} instances. Defaults to "scheduled".
     * @return the name of the publishing executor
     *
     */
    public String getPublishingExecutor() {
        return publishingExecutor;
    }

    /**
     *
     * @param publishingExecutor Name of the {@link java.util.concurrent.ScheduledExecutorService} to be used by all {@link com.google.cloud.pubsub.v1.Publisher} instances. Default: "scheduled"
     */
    public void setPublishingExecutor(String publishingExecutor) {
        this.publishingExecutor = publishingExecutor;
    }

    /**
     * The name of the {@link java.util.concurrent.ScheduledExecutorService} to be used by all {@link com.google.cloud.pubsub.v1.Subscriber} instances. Defaults to "scheduled".
     * @return the name of the subscribing executor
     */
    public String getSubscribingExecutor() {
        return subscribingExecutor;
    }

    /**
     *
     * @param subscribingExecutor Name of the {@link java.util.concurrent.ScheduledExecutorService} to be used by all {@link com.google.cloud.pubsub.v1.Subscriber} instances. Default: "scheduled"
     */
    public void setSubscribingExecutor(String subscribingExecutor) {
        this.subscribingExecutor = subscribingExecutor;
    }

    /**
     * How often to ping the server to keep the channel alive. Defaults to 5 minutes.
     * @return interval
     */
    public int getKeepAliveIntervalMinutes() {
        return keepAliveIntervalMinutes;
    }

    /**
     * How often to ping the server to keep the channel alive. Default: 5 minutes.
     * @param keepAliveIntervalMinutes
     */
    public void setKeepAliveIntervalMinutes(int keepAliveIntervalMinutes) {
        this.keepAliveIntervalMinutes = keepAliveIntervalMinutes;
    }

    /**
     * Which endpoint the {@link com.google.cloud.pubsub.v1.Publisher} should publish messages to. Defaults to the global endpoint
     * @return endpoint
     */
    public String getTopicEndpoint() {
        return this.topicEndpoint;
    }

    /**
     *
     * @param topicEndpoint to be used by all {@link com.google.cloud.pubsub.v1.Publisher} instances. Default: "" (i.e. the global endpoint)
     */
    public void setTopicEndpoint(String topicEndpoint) {
        this.topicEndpoint = topicEndpoint;
    }

    /**
     * Whether subscribers should stop processing pending in-memory messages and eagerly nack() during application shutdown. Defaults to {@value #DEFAULT_NACK_ON_SHUTDOWN}.
     * @return nack on shutdown configuration
     * @since 5.2.0
     */
    public boolean isNackOnShutdown() {
        return this.nackOnShutdown;
    }

    /**
     *
     * @param nackOnShutdown whether subscribers should stop processing pending in-memory messages and eagerly nack() during application shutdown.
     * @since 5.2.0
     */
    public void setNackOnShutdown(boolean nackOnShutdown) {
        this.nackOnShutdown = nackOnShutdown;
    }
}
