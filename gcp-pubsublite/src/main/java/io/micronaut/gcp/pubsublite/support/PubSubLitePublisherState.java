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
package io.micronaut.gcp.pubsublite.support;

import com.google.cloud.pubsublite.TopicPath;
import com.google.cloud.pubsublite.cloudpubsub.Publisher;
import io.micronaut.core.type.Argument;

import java.util.Map;
import java.util.Optional;

/**
 * Stores the context of a PubSubMessage to be published. Values of this class comes from parsing of method
 * annotations and hence are cached.
 *
 * Based on {@link io.micronaut.gcp.pubsub.support.PubSubPublisherState}.
 *
 * @author Jacob Mims
 * @since 2.2.0
 */
public class PubSubLitePublisherState {
    private final TopicState topicState;
    private final Map<String, String> staticMessageAttributes;
    private final Argument<?> bodyArgument;
    private final Publisher publisher;
    private final Optional<Argument> orderingArgument;

    public PubSubLitePublisherState(TopicState topicState,
                                    Map<String, String> staticMessageAttributes,
                                    Argument<?> bodyArgument,
                                    Publisher publisher,
                                    Optional<Argument> orderingArgument) {
        this.topicState = topicState;
        this.staticMessageAttributes = staticMessageAttributes;
        this.bodyArgument = bodyArgument;
        this.publisher = publisher;
        this.orderingArgument = orderingArgument;
    }

    /**
     *
     * @return the cached publisher associated with the method.
     */
    public Publisher getPublisher() {
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

    /**
     * Topic State for a PubSub Lite Client.
     *
     * Based on {@link io.micronaut.gcp.pubsub.support.PubSubPublisherState.TopicState}
     *
     * @author Jacob Mims
     * @since 2.2.0
     */
    public static class TopicState {

        private final String contentType;
        private final TopicPath topicPath;
        private final String configurationName;

        public TopicState(String contentType, TopicPath topicPath, String configurationName) {
            this.contentType = contentType;
            this.topicPath = topicPath;
            this.configurationName = configurationName;
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
         * @return the topic path
         */
        public TopicPath getTopicPath() {
            return topicPath;
        }

        /**
         *
         * @return the name of the {@link io.micronaut.gcp.pubsublite.configuration.LitePublisherConfigurationProperties} to be used
         */
        public String getConfigurationName() {
            return configurationName;
        }
    }
}
