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
package io.micronaut.gcp.credentials;

import io.micronaut.context.annotation.BootstrapContextCompatible;
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
@BootstrapContextCompatible
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

    private String location;

    private String encodedKey;

    /**
     * The scopes to use.
     * @return The scopes
     */
    public @Nonnull List<URI> getScopes() {
        return scopes;
    }

    /**
     * The default scopes to associate with the application to access specific APIs.
     * See <a href="https://developers.google.com/identity/protocols/googlescopes">Google Scopes</a> for a complete list.
     * Leave this empty if you don't need additional API access.
     *
     * @param scopes The scopes
     */
    public void setScopes(@Nullable List<URI> scopes) {
        this.scopes = scopes == null ? Collections.emptyList() : scopes;
    }

    /**
     * The location of the service account credential key file. 
     * See <a href="https://cloud.google.com/iam/docs/understanding-service-accounts">Understanding Service Accounts</a>
     * for more information on generating a service account key file.
     * @return The location
     */
    public @Nonnull Optional<String> getLocation() {
        return Optional.ofNullable(location);
    }

    /**
     * Sets the location to the service account credential key file.
     * @param location The location
     */
    public void setLocation(@Nullable String location) {
        this.location = location;
    }

    /**
     * The Base64 encoded service account key content. This is not recommended except if you need to encode service
     * account key via an environmental variable. For other use cases, configure <pre>location</pre> instead.
     * @return The key
     */
    public @Nonnull Optional<String> getEncodedKey() {
        return Optional.ofNullable(encodedKey);
    }

    /**
     * Sets the Base64 encoded service account key content..
     * @param encodedKey The key
     */
    public void setEncodedKey(@Nullable String encodedKey) {
        this.encodedKey = encodedKey;
    }
}
