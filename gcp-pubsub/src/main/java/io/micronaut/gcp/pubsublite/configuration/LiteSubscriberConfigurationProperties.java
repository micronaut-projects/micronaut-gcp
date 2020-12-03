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

import com.google.cloud.pubsublite.cloudpubsub.FlowControlSettings;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;

import java.util.List;

/**
 * Configuration properties for PubSub Lite {@link com.google.cloud.pubsublite.cloudpubsub.Subscriber}.
 * Each topic has its own configuration if set by the user.
 * For example, gcp.pubsublite.publisher.animals and gcp.pubsublite.publisher.cars would define subscribers with different
 * configurations for each {@link io.micronaut.gcp.pubsublite.annotation.LiteSubscription}.
 *
 * Based on {@link io.micronaut.gcp.pubsub.configuration.SubscriberConfigurationProperties}
 * See {@link com.google.cloud.pubsublite.cloudpubsub.SubscriberSettings for more information}
 * @author Jacob Mims
 * @since 2.2.0
 *
 */
@EachProperty(value = PubSubLiteConfigurationProperties.PREFIX + ".subscriber")
public class LiteSubscriberConfigurationProperties {
    private final String name;
    private String assignmentServiceClient;
    private String cursorServiceClientSupplier;
    private String subscriberServiceClientSupplier;
    private String nackHandler;
    private List<Long> partitions;
    private String messageTransformer;

    @ConfigurationBuilder(prefixes = "set", configurationPrefix = "flow-control")
    private FlowControlSettings.Builder flowControlSettings = FlowControlSettings
            .builder()
            .setMessagesOutstanding(1000L)
            .setBytesOutstanding(100L * 1024L * 1024L);

    /**
     *
     * @param name of this configuration.
     */
    public LiteSubscriberConfigurationProperties(@Parameter String name) {
        this.name = name;
    }

    /**
     *
     * @return the name of the configuration
     */
    public String getName() {
        return name;
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
     * @return bean name for the assignmentServiceClient
     */
    public String getAssignmentServiceClient() {
        return assignmentServiceClient;
    }

    /**
     *
     * @param assignmentServiceClient bean name
     */
    public void setAssignmentServiceClient(String assignmentServiceClient) {
        this.assignmentServiceClient = assignmentServiceClient;
    }

    /**
     *
     * @return bean name for the cursorServiceClientSupplier.
     */
    public String getCursorServiceClientSupplier() {
        return cursorServiceClientSupplier;
    }

    /**
     *
     * @param cursorServiceClientSupplier bean name
     */
    public void setCursorServiceClientSupplier(String cursorServiceClientSupplier) {
        this.cursorServiceClientSupplier = cursorServiceClientSupplier;
    }

    /**
     *
     * @return bean name for the subscriberServiceClientSupplier
     */
    public String getSubscriberServiceClientSupplier() {
        return subscriberServiceClientSupplier;
    }

    /**
     *
     * @param subscriberServiceClientSupplier bean name
     */
    public void setSubscriberServiceClientSupplier(String subscriberServiceClientSupplier) {
        this.subscriberServiceClientSupplier = subscriberServiceClientSupplier;
    }

    /**
     *
     * @return bean name for the nackHandler
     */
    public String getNackHandler() {
        return nackHandler;
    }

    /**
     *
     * @param nackHandler bean name
     */
    public void setNackHandler(String nackHandler) {
        this.nackHandler = nackHandler;
    }

    /**
     *
     * @return list of partition identifiers
     */
    public List<Long> getPartitions() {
        return partitions;
    }

    /**
     *
     * @param partitions list of partition identifiers to subscribe to
     */
    public void setPartitions(List<Long> partitions) {
        this.partitions = partitions;
    }

    /**
     *
     * @return bean name for the message transformer
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
}
