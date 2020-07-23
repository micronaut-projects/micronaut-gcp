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

/**
 * Configuration properties for PubSub support.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@ConfigurationProperties(PubSubConfigurationProperties.PREFIX)
public class PubSubConfigurationProperties {

    public static final String PREFIX = GoogleCloudConfiguration.PREFIX + ".pubsub";

    private int keepAliveIntervalMinutes = 5;

    private String publishingExecutor = "scheduled";

    private String subscribingExecutor = "scheduled";

    /**
     *
     * @return the name of the {@link java.util.concurrent.ScheduledExecutorService} to be used by all {@link com.google.cloud.pubsub.v1.Publisher} instances.
     * Defaults to "scheduled"
     */
    public String getPublishingExecutor() {
        return publishingExecutor;
    }

    /**
     *
     * @param publishingExecutor Name of the {@link java.util.concurrent.ScheduledExecutorService} to be used by all {@link com.google.cloud.pubsub.v1.Publisher} instances
     */
    public void setPublishingExecutor(String publishingExecutor) {
        this.publishingExecutor = publishingExecutor;
    }

    /**
     *
     * @return the name of the {@link java.util.concurrent.ScheduledExecutorService} to be used by all {@link com.google.cloud.pubsub.v1.Subscriber} instances.
     * Defaults to "scheduled"
     */
    public String getSubscribingExecutor() {
        return subscribingExecutor;
    }

    /**
     *
     * @param subscribingExecutor Name of the {@link java.util.concurrent.ScheduledExecutorService} to be used by all {@link com.google.cloud.pubsub.v1.Subscriber} instances.
     */
    public void setSubscribingExecutor(String subscribingExecutor) {
        this.subscribingExecutor = subscribingExecutor;
    }

    /**
     * How often to ping the server to keep the channel alive.
     * @return interval
     */
    public int getKeepAliveIntervalMinutes() {
        return keepAliveIntervalMinutes;
    }

    /**
     * How often to ping the server to keep the channel alive.
     * @param keepAliveIntervalMinutes
     */
    public void setKeepAliveIntervalMinutes(int keepAliveIntervalMinutes) {
        this.keepAliveIntervalMinutes = keepAliveIntervalMinutes;
    }

}
