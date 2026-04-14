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

import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.StringUtils;
import io.micronaut.gcp.secretmanager.configuration.SecretManagerConfigurationProperties;

/**
 * Helper methods for building Secret Manager requests and mapping responses.
 *
 * @since 5.0
 */
@Internal
public final class SecretManagerSecretAccessor {

    private SecretManagerSecretAccessor() {
    }

    public static AccessSecretVersionRequest accessRequest(String projectId,
                                                           String secretId,
                                                           String version,
                                                           SecretManagerConfigurationProperties configurationProperties) {
        SecretVersionName secretVersionName = StringUtils.isEmpty(configurationProperties.getLocation())
            ? SecretVersionName.of(projectId, secretId, version)
            : SecretVersionName.ofProjectLocationSecretSecretVersionName(projectId, configurationProperties.getLocation(), secretId, version);
        return AccessSecretVersionRequest.newBuilder()
            .setName(secretVersionName.toString())
            .build();
    }

    public static VersionedSecret accessSecret(SecretManagerServiceClient client,
                                               String projectId,
                                               String secretId,
                                               String version,
                                               SecretManagerConfigurationProperties configurationProperties) {
        AccessSecretVersionResponse response = client.accessSecretVersion(accessRequest(projectId, secretId, version, configurationProperties));
        return toVersionedSecret(secretId, version, projectId, response, configurationProperties);
    }

    public static VersionedSecret toVersionedSecret(String secretId,
                                                    String version,
                                                    String projectId,
                                                    AccessSecretVersionResponse response,
                                                    SecretManagerConfigurationProperties configurationProperties) {
        return StringUtils.isEmpty(configurationProperties.getLocation())
            ? new VersionedSecret(secretId, projectId, version, response.getPayload().getData().toByteArray())
            : new VersionedSecret(secretId, projectId, version, response.getPayload().getData().toByteArray(), configurationProperties.getLocation());
    }
}
