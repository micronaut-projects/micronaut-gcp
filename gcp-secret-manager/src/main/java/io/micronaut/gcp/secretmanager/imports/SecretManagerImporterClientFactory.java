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
package io.micronaut.gcp.secretmanager.imports;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import io.micronaut.core.annotation.Internal;
import io.micronaut.gcp.secretmanager.SecretManagerFactory;
import io.micronaut.gcp.secretmanager.configuration.SecretManagerConfigurationProperties;

/**
 * Internal client factory seam for Google Secret Manager config imports.
 *
 * @since 5.0
 */
@Internal
public final class SecretManagerImporterClientFactory {

    /**
     * Creates the low-level Secret Manager client used during config import.
     *
     * @param configurationProperties Secret Manager client configuration derived for the import
     * @param credentialsProvider Credentials provider used to authenticate the import request
     * @param transportChannelProvider Transport channel provider for the Secret Manager client
     * @return A Secret Manager service client
     */
    public SecretManagerServiceClient create(SecretManagerConfigurationProperties configurationProperties,
                                             CredentialsProvider credentialsProvider,
                                             TransportChannelProvider transportChannelProvider) {
        SecretManagerFactory factory = new SecretManagerFactory(configurationProperties);
        return factory.secretManagerServiceClient(credentialsProvider, transportChannelProvider);
    }
}
