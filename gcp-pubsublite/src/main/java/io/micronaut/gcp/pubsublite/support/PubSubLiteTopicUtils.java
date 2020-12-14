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

import com.google.cloud.pubsublite.CloudZone;
import com.google.cloud.pubsublite.ProjectId;
import com.google.cloud.pubsublite.ProjectNumber;
import com.google.cloud.pubsublite.TopicName;
import com.google.cloud.pubsublite.TopicPath;

import javax.annotation.Nonnull;

/**
 * Various utility methods for dealing with Pub/Sub Lite topics.
 *
 * Based on {@link io.micronaut.gcp.pubsub.support.PubSubTopicUtils}
 *
 * @author Jacob Mims
 * @since 2.2.0
 */
public final class PubSubLiteTopicUtils {

    private PubSubLiteTopicUtils() {
    }

    /**
     * Create a {@link TopicPath} based on a topic name at a location within a project.
     * @param topic the topic name in the project or the fully-qualified project name
     * @param projectNumber the project number of the topic
     * @param location the location of the topic
     * @return the Pub/Sub Lite object representing the topic name
     */
    public static TopicPath toPubsubLiteTopic(@Nonnull String topic, @Nonnull Long projectNumber, @Nonnull String location) {
        return TopicPath.newBuilder()
                .setProject(ProjectNumber.of(projectNumber))
                .setLocation(CloudZone.parse(location))
                .setName(TopicName.of(topic))
                .build();
    }

    /**
     * Create a {@link TopicPath} based on a topic name at a location within a project.
     * @param topic the topic name in the project or the fully-qualified project name
     * @param projectId the project ID of the topic
     * @param location the location of the topic
     * @return the Pub/Sub Lite object representing the topic name
     */
    public static TopicPath toPubsubLiteTopic(@Nonnull String topic, @Nonnull String projectId, @Nonnull String location) {
        return TopicPath.newBuilder()
                .setProject(ProjectId.of(projectId))
                .setLocation(CloudZone.parse(location))
                .setName(TopicName.of(topic))
                .build();
    }
}
