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

public class PublisherFactoryConfig {

    private final ProjectTopicName topicName;
    private final String publisherConfiguration;
    private final String defaultExecutor;

    public PublisherFactoryConfig(ProjectTopicName topicName, String publisherConfiguration, String defaultExecutor) {
        this.topicName = topicName;
        this.publisherConfiguration = publisherConfiguration;
        this.defaultExecutor = defaultExecutor;
    }

    /**
     *
     * @return ProjectTopicName to be used
     */
    public ProjectTopicName getTopicName() {
        return topicName;
    }

    /**
     *
     * @return Name of the publisher configuration passed via {@link io.micronaut.gcp.pubsub.annotation.Topic} annotation.
     */
    public String getPublisherConfiguration() {
        return publisherConfiguration;
    }

    /**
     *
     * @return Default {@link java.util.concurrent.ExecutorService} set for all publishers.
     */
    public String getDefaultExecutor() {
        return defaultExecutor;
    }
}
