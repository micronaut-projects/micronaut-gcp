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

import io.reactivex.Maybe;

/**
 * This interface is intended to abstract interactions with {@link com.google.cloud.secretmanager.v1.SecretManagerServiceClient}, and instead of returning Google's {@link com.google.api.core.ApiFuture}
 * transform it on reactive extensions.
 *
 * @author Vinicius Carvalho
 * @since 3.2.0
 */
public interface SecretManagerClient {

    String LATEST = "latest";

    /**
     * Fetches a secret from the Secret Manager storage using the `gcp.projectId` project, and "latest" as the version.
     * @param secretId - name of the secret
     * @return String value of the secret or empty
     */
    Maybe<VersionedSecret> getSecret(String secretId);

    /**
     * Fetches a secret from the Secret Manager storage using the `gcp.projectId` project.
     * @param secretId - name of the secret
     * @param version - version of the secret
     * @return String value of the secret or empty
     */
    Maybe<VersionedSecret> getSecret(String secretId, String version);

    /**
     * Fetches a secret from the Secret Manager storage.
     * @param secretId - name of the secret
     * @param version - version of the secret
     * @param projectId - project identifier
     * @return String value of the secret or empty
     */
    Maybe<VersionedSecret> getSecret(String secretId, String version, String projectId);

}
