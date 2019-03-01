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
package io.micronaut.gcp.credentials;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import io.micronaut.gcp.GoogleCloudConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Configuration for the Google credentials.
 *
 * @author graemerocher
 * @author Ray Tsang
 * @since 1.0
 */
@ConfigurationProperties(GoogleCredentialsConfiguration.PREFIX)
@Context
public class GoogleCredentialsConfiguration {
    /**
     * The default scopes.
     */
    public static final List<URI> DEFAULT_SCOPES = Collections.emptyList();

    /**
     * The prefix to use.
     */
    public static final String PREFIX = GoogleCloudConfiguration.PREFIX + ".credentials";

    private List<URI> scopes = DEFAULT_SCOPES;

    private String path;

    private String encodedKey;

    /**
     * The scopes to use.
     * @return The scopes
     */
    public @Nonnull List<URI> getScopes() {
        return scopes;
    }

    /**
     * The scopes to use.
     *
     * @param scopes The scopes
     */
    public void setScopes(@Nullable List<URI> scopes) {
        this.scopes = scopes == null ? Collections.emptyList() : scopes;
    }

    /**
     * The location of the credentials.
     * @return The location
     */
    public @Nonnull Optional<String> getLocation() {
        return Optional.ofNullable(path);
    }

    /**
     * Sets the location to use.
     * @param location The location
     */
    public void setLocation(@Nullable String location) {
        this.path = path;
    }

    /**
     * The encoded key.
     * @return The key
     */
    public @Nonnull Optional<String> getEncodedKey() {
        return Optional.ofNullable(encodedKey);
    }

    /**
     * Sets the encoded key.
     * @param encodedKey The key
     */
    public void setEncodedKey(@Nullable String encodedKey) {
        this.encodedKey = encodedKey;
    }
}
