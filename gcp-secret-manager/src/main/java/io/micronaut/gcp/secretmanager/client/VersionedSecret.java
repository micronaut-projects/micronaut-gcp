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
package io.micronaut.gcp.secretmanager.client;

import io.micronaut.core.annotation.Nullable;

/**
 * A wrapper class around {@link com.google.cloud.secretmanager.v1.AccessSecretVersionResponse} with secret information.
 *
 * @author Vinicius Carvalho
 * @since 3.4.0
 */
public class VersionedSecret {

    private final String name;
    private final String projectId;
    private final byte[] contents;
    private final String version;

    @Nullable
    private final String location;

    public VersionedSecret(String name, String projectId, String version, byte[] contents) {
        this(name, projectId, version, contents, null);
    }

    public VersionedSecret(String name, String projectId, String version, byte[] contents, String location) {
        this.name = name;
        this.projectId = projectId;
        this.contents = contents;
        this.version = version;
        this.location = location;
    }

    /**
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return contents
     */
    public byte[] getContents() {
        return contents;
    }

    /**
     *
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     *
     * @return projectId
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     *
     * @return location
     * @since 5.8.0
     */
    @Nullable
    public String getLocation() {
        return location;
    }
}
