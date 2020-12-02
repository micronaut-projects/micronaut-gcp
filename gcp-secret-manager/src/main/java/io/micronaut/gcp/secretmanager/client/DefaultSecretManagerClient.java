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

import com.google.cloud.secretmanager.v1.*;
import io.micronaut.context.annotation.Requires;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.condition.RequiresGoogleProjectId;
import io.reactivex.Maybe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
@RequiresGoogleProjectId
@Requires(classes = SecretManagerServiceClient.class)
public class DefaultSecretManagerClient implements SecretManagerClient {

    private final SecretManagerServiceClient client;
    private final GoogleCloudConfiguration googleCloudConfiguration;
    private final Logger logger = LoggerFactory.getLogger(SecretManagerClient.class);

    public DefaultSecretManagerClient(SecretManagerServiceClient client, GoogleCloudConfiguration googleCloudConfiguration) {
        this.client = client;
        this.googleCloudConfiguration = googleCloudConfiguration;
    }

    @Override
    public Maybe<VersionedSecret> getSecret(String secretId) {
        return getSecret(secretId, LATEST ,this.googleCloudConfiguration.getProjectId());
    }

    @Override
    public Maybe<VersionedSecret> getSecret(String secretId, String version) {
        return getSecret(secretId, version, this.googleCloudConfiguration.getProjectId());
    }

    @Override
    public Maybe<VersionedSecret> getSecret(String secretId, String version, String projectId) {
        SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, version);
        AccessSecretVersionRequest request = AccessSecretVersionRequest.newBuilder()
                .setName(secretVersionName.toString())
                .build();
        return Maybe.fromFuture(client.accessSecretVersionCallable().futureCall(request))
                .map(response -> new VersionedSecret(secretId, projectId, version, response.getPayload().toByteArray()))
                .onErrorResumeNext(Maybe.empty());
    }
}
