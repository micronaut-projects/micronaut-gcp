/*
 * Copyright 2017-2026 original authors
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
package io.micronaut.gcp.parametermanager.client;

/**
 * A common wrapper class around
 * {@link com.google.cloud.parametermanager.v1.RenderParameterVersionResponse}
 * and {@link com.google.cloud.parametermanager.v1.ParameterVersion} with parameter information.
 *
 * @author Dhaval Bhensdadiya
 * @since 6.0.0
 */
public class VersionedParameter {

    private final String projectId;
    private final String location;
    private final String name;
    private final String version;
    private final byte[] contents;

    /**
     * Constructor for the {@link VersionedParameter}.
     *
     * @param projectId - The GCP project ID of the parameter.
     * @param location  - The location of the parameter.
     * @param name      - The name of the parameter.
     * @param version   - The version of the parameter.
     * @param contents  - The content of the specific parameter version.
     */
    public VersionedParameter(String projectId, String location, String name, String version,
                              byte[] contents) {
        this.projectId = projectId;
        this.location = location;
        this.name = name;
        this.version = version;
        this.contents = contents;
    }

    /**
     * Returns the GCP project ID of the Parameter.
     *
     * @return projectId
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * Returns the location of the Parameter.
     *
     * @return location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the name of the Parameter.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the version of the Parameter.
     *
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the content of the specific Parameter version.
     *
     * @return contents
     */
    public byte[] getContents() {
        return contents;
    }
}
