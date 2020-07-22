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
import com.google.api.gax.batching.FlowController;
import com.google.api.gax.retrying.RetrySettings;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.gcp.GoogleCloudConfiguration;
import javax.annotation.Nullable;

/**
 * Configuration properties for PubSub support.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@ConfigurationProperties(PubSubConfigurationProperties.PREFIX)
public class PubSubConfigurationProperties {

    public static final String PREFIX = GoogleCloudConfiguration.PREFIX + ".pubsub";

    private  Publisher publisher = new Publisher();

    private int keepAliveIntervalMinutes = 5;

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

    /**
     *
     * @return publisher configuration
     */
    public Publisher getPublisher() {
        return publisher;
    }

    /**
     *
     * @param publisher configuration
     */
    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Publisher settings.
     */
    @ConfigurationProperties("publisher")
    public static class Publisher {

        private int executorThreads = 4;

        @ConfigurationBuilder(prefixes = "set", configurationPrefix = "retry")
        private RetrySettings.Builder retrySettings;

        @ConfigurationBuilder(prefixes = "set", configurationPrefix = "batching")
        private BatchingSettings.Builder batchingSettings;

        @ConfigurationBuilder(prefixes = "set", configurationPrefix = "flow-control")
        private FlowControlSettings.Builder flowControlSettings;

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

        public RetrySettings getRetrySettings() {
            return retrySettings.build();
        }

        public void setRetrySettings(RetrySettings.Builder retrySettings) {
            this.retrySettings = retrySettings;
        }

        public BatchingSettings getBatchingSettings() {
            return batchingSettings.build();
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

}
