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


import com.google.pubsub.v1.ProjectTopicName;
import io.micronaut.core.type.Argument;

import java.util.Map;

/**
 * Stores the context of a PubSubMessage to be pulished. Values of this class comes from parsing of method
 * annotations and hence are cached @see {@link io.micronaut.gcp.pubsub.intercept.PubSubConsumerAdvice}
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
public class PubSubPublisherState {

    private final String contentType;
    private final ProjectTopicName topicName;
    private final Map<String, String> staticMessageAttributes;
    private final Argument<?> bodyArgument;
    private final String configuration;

    public PubSubPublisherState(String contentType,
                                ProjectTopicName topicName,
                                Map<String, String> staticMessageAttributes,
                                Argument<?> bodyArgument,
                                String executor) {
        this.contentType = contentType;
        this.topicName = topicName;
        this.staticMessageAttributes = staticMessageAttributes;
        this.bodyArgument = bodyArgument;
        this.configuration = executor;
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
    public ProjectTopicName getTopicName() {
        return topicName;
    }

    /**
     *
     * @return Message Attibutes from Header annotations
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
     * @return the name of the {@link io.micronaut.gcp.pubsub.configuration.PublisherConfigurationProperties} to be used
     */
    public String getConfiguration() {
        return configuration;
    }
}
