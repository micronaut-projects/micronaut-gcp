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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ConfigurationProperties("gcp.credentials")
@Context
public class GoogleCredentialsConfiguration {
    public static final List<URI> DEFAULT_SCOPES = Collections.emptyList();

    private List<URI> scopes = DEFAULT_SCOPES;

    private String path;

    private String encodedKey;

    public @Nonnull List<URI> getScopes() {
        return scopes;
    }

    public void setScopes(@Nullable List<URI> scopes) {
        this.scopes = scopes == null ? Collections.emptyList() : scopes;
    }

    public Optional<String> getLocation() {
        return Optional.ofNullable(path);
    }

    public void setLocation(String location) {
        this.path = path;
    }

    public Optional<String> getEncodedKey() {
        return Optional.ofNullable(encodedKey);
    }

    public void setEncodedKey(String encodedKey) {
        this.encodedKey = encodedKey;
    }
}
