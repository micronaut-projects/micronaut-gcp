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

import com.google.pubsub.v1.ProjectSubscriptionName;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;


/**
 * Various utility methods for dealing with Pub/Sub subscriptions.
 *
 * Original source at : https://github.com/spring-cloud/spring-cloud-gcp/blob/master/spring-cloud-gcp-pubsub/src/main/java/org/springframework/cloud/gcp/pubsub/support/PubSubSubscriptionUtils.java
 * @author Mike Eltsufin
 * @author Vinicius Carvalho
 *
 * @since 2.0.0
 */
public final class PubSubSubscriptionUtils {

    private PubSubSubscriptionUtils() {
    }

    /**
     * Create a {@link ProjectSubscriptionName} based on a subscription name within a project or the
     * fully-qualified subscription name. If the specified subscription is in the
     * {@code projects/<project_name>/subscriptions/<subscription_name>} format, then the {@code projectId} is
     * ignored}
     * @param subscription the subscription name in the project or the fully-qualified project name
     * @param projectId the project ID to use if the subscription is not a fully-qualified name
     * @return the Pub/Sub object representing the subscription name
     */
    @SuppressWarnings("checkstyle:RightCurly")
    public static ProjectSubscriptionName toProjectSubscriptionName(@NonNull String subscription, @Nullable String projectId) {

        ProjectSubscriptionName projectSubscriptionName = null;

        if (ProjectSubscriptionName.isParsableFrom(subscription)) {
            // Fully-qualified subscription name in the "projects/<project_name>/subscriptions/<subscription_name>" format
            projectSubscriptionName = ProjectSubscriptionName.parse(subscription);
        } else {
            if (projectId == null) {
               throw new IllegalArgumentException("The project ID can't be null when using canonical topic name.");
            }
            projectSubscriptionName = ProjectSubscriptionName.of(projectId, subscription);
        }

        return projectSubscriptionName;
    }
}
