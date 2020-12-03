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
import com.google.cloud.pubsublite.SubscriptionName;
import com.google.cloud.pubsublite.SubscriptionPath;

import javax.annotation.Nonnull;

/**
 * Various utility methods for dealing with Pub/Sub Lite subscriptions.
 *
 * Based on {@link io.micronaut.gcp.pubsub.support.PubSubSubscriptionUtils}
 *
 * @author Jacob Mims
 * @since 2.2.0
 */
public final class PubSubLiteSubscriptionUtils {
    private PubSubLiteSubscriptionUtils() {
    }

    /**
     * Create a {@link SubscriptionPath} based on a subscription name at a location within a project.
     * @param subscription the subscription name in the project
     * @param projectNumber the project number of the subscription
     * @param location location of the subscription (eg., us-central-1a)
     * @return the Pub/Sub object representing the subscription name
     */
    public static SubscriptionPath toSubscriptionPath(@Nonnull String subscription, @Nonnull Long projectNumber, @Nonnull String location) {
        return SubscriptionPath.newBuilder()
                .setProject(ProjectNumber.of(projectNumber))
                .setLocation(CloudZone.parse(location))
                .setName(SubscriptionName.of(subscription))
                .build();
    }

    /**
     * Create a {@link SubscriptionPath} based on a subscription name at a location within a project.
     * @param subscription the subscription name in the project
     * @param projectId the project ID to use if the subscription
     * @param location location of the subscription (eg., us-central-1a)
     * @return the Pub/Sub object representing the subscription name
     */
    public static SubscriptionPath toSubscriptionPath(@Nonnull String subscription, @Nonnull String projectId, @Nonnull String location) {
        return SubscriptionPath.newBuilder()
                .setProject(ProjectId.of(projectId))
                .setLocation(CloudZone.parse(location))
                .setName(SubscriptionName.of(subscription))
                .build();
    }
}
