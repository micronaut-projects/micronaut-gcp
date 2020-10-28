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


import com.google.cloud.pubsub.v1.PublisherInterface;
import com.google.pubsub.v1.ProjectTopicName;
import io.micronaut.core.type.Argument;

import java.util.Map;
import java.util.Optional;

/**
 * Stores the context of a PubSubMessage to be pulished. Values of this class comes from parsing of method
 * annotations and hence are cached @see {@link io.micronaut.gcp.pubsub.intercept.PubSubConsumerAdvice}
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
public class PubSubPublisherState {

    private final TopicState topicState;
    private final Map<String, String> staticMessageAttributes;
    private final Argument<?> bodyArgument;
    private final PublisherInterface publisher;
    private final Optional<Argument> orderingArgument;

    public PubSubPublisherState(TopicState topicState,
                                Map<String, String> staticMessageAttributes,
                                Argument<?> bodyArgument,
                                PublisherInterface publisher,
                                Optional<Argument> orderingArgument) {
        this.topicState = topicState;
        this.staticMessageAttributes = staticMessageAttributes;
        this.bodyArgument = bodyArgument;
        this.publisher = publisher;
        this.orderingArgument = orderingArgument;
    }

    public PublisherInterface getPublisher() {
        return publisher;
    }

    /**
     *
     * @return topicState information
     */
    public TopicState getTopicState() {
        return topicState;
    }

    /**
     *
     * @return Message Attributes from Header annotations
     */
    public Map<String, String> getStaticMessageAttributes() {
        return staticMessageAttributes;
    }

    /**
     *
     * @return the body argument
     */
    public Argument<?> getBodyArgument() {
        return bodyArgument;
    }

    /**
     *
     * @return Argument annotated with @{@link io.micronaut.gcp.pubsub.annotation.OrderingKey}.
     */
    public Optional<Argument> getOrderingArgument() {
        return orderingArgument;
    }

    public static class TopicState {

        private final String contentType;
        private final ProjectTopicName projectTopicName;
        private final String configurationName;
        private final String endpoint;
        private final Boolean ordered;

        public TopicState(String contentType, ProjectTopicName projectTopicName, String configurationName, String endpoint, Boolean ordered) {
            this.contentType = contentType;
            this.projectTopicName = projectTopicName;
            this.configurationName = configurationName;
            this.endpoint = endpoint;
            this.ordered = ordered;
        }

        /**
         *
         * @return the contentType
         */
        public String getContentType() {
            return contentType;
        }

        /**
         *
         * @return the topic name
         */
        public ProjectTopicName getProjectTopicName() {
            return projectTopicName;
        }

        /**
         *
         * @return the name of the {@link io.micronaut.gcp.pubsub.configuration.PublisherConfigurationProperties} to be used
         */
        public String getConfigurationName() {
            return configurationName;
        }

        /**
         *
         * @return the endpoint to be used, or empty for global endpoint
         */
        public String getEndpoint() {
            return endpoint;
        }

        /**
         *
         * @return if message ordering should be enabled
         */
        public Boolean getOrdered() {
            return ordered;
        }
    }

}
