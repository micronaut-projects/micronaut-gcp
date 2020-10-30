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

/**
 * Holds necessary configuration to create {@link com.google.cloud.pubsub.v1.Publisher} instances via {@link PublisherFactory}.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
public class PublisherFactoryConfig {

    private final String defaultExecutor;
    private final PubSubPublisherState.TopicState topicState;

    public PublisherFactoryConfig(PubSubPublisherState.TopicState topicState, String defaultExecutor) {
        this.defaultExecutor = defaultExecutor;
        this.topicState = topicState;
    }

    /**
     * @return TopicState configuration for the bound topic.
     */
    public PubSubPublisherState.TopicState getTopicState() {
        return topicState;
    }

    /**
     * @return Default {@link java.util.concurrent.ExecutorService} set for all publishers.
     */
    public String getDefaultExecutor() {
        return defaultExecutor;
    }

}
