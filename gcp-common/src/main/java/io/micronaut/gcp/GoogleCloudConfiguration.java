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
package io.micronaut.gcp;

import com.google.cloud.ServiceOptions;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.env.Environment;
import io.micronaut.context.exceptions.ConfigurationException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * General Google cloud configuration.
 *
 * @author graemerocher
 * @author Ray Tsang
 * @since 1.0
 */
@ConfigurationProperties(GoogleCloudConfiguration.PREFIX)
public class GoogleCloudConfiguration {

    /**
     * A message to report if no project id is configured.
     */
    public static final String NO_PROJECT_ID_MESSAGE = "No Google Cloud Project ID found. See ServiceOptions.getDefaultProjectId() for description on how to configure the service ID";

    /**
     * The prefix to use.
     */
    public static final String PREFIX = Environment.GOOGLE_COMPUTE;

    private String projectId;

    /**
     * Returns the Google project ID for the project.
     *
     * @return The project id
     * @throws ConfigurationException if no project ID is found
     */
    public @Nonnull String getProjectId() {
        if (projectId == null) {
            projectId = ServiceOptions.getDefaultProjectId();
            if (projectId == null) {
                throw new ConfigurationException(NO_PROJECT_ID_MESSAGE);
            }
        }
        return projectId;
    }

    /**
     * Sets the project id to use.
     * @param projectId The project id to use
     */
    public void setProjectId(@Nullable String projectId) {
        this.projectId = projectId;
    }

    /**
     * Whether a project id is configured.
     * @return True if one is
     */
    public boolean hasProjectId() {
        try {
            getProjectId();
            return true;
        } catch (ConfigurationException e) {
            return false;
        }
    }
}
