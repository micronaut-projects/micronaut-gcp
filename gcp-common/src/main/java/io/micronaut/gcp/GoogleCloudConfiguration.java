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
