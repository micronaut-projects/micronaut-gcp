/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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


@ConfigurationProperties(Environment.GOOGLE_COMPUTE)
public class GoogleCloudConfiguration {

    public static final String NO_PROJECT_ID_MESSAGE = "No Google Cloud Project ID found. See ServiceOptions.getDefaultProjectId() for description on how to configure the service ID";
    private String projectId;

    public String getProjectId() {
        if (projectId == null) {
            projectId = ServiceOptions.getDefaultProjectId();
            if (projectId == null) {
                throw new ConfigurationException(NO_PROJECT_ID_MESSAGE);
            }
        }
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public boolean hasProjectId() {
        try {
            return getProjectId() != null;
        } catch (ConfigurationException e) {
            return false;
        }
    }
}
