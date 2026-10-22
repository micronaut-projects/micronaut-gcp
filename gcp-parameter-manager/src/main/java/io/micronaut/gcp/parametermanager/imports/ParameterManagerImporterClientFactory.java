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
package io.micronaut.gcp.parametermanager.imports;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import io.micronaut.core.annotation.Internal;
import io.micronaut.gcp.parametermanager.ParameterManagerFactory;
import io.micronaut.gcp.parametermanager.configuration.ParameterManagerConfigurationProperties;

/**
 * Internal client factory seam for Google Cloud Parameter Manager config imports.
 *
 * @since 6.1.0
 */
@Internal
public final class ParameterManagerImporterClientFactory {

    /**
     * Creates the low-level Parameter Manager client used during config import.
     *
     * @param configurationProperties Parameter Manager client configuration derived for the import
     * @param credentialsProvider Credentials provider used to authenticate the import request
     * @param transportChannelProvider Transport channel provider for the Parameter Manager client
     * @return A Parameter Manager client
     */
    public ParameterManagerClient create(ParameterManagerConfigurationProperties configurationProperties,
                                         CredentialsProvider credentialsProvider,
                                         TransportChannelProvider transportChannelProvider) {
        ParameterManagerFactory factory = new ParameterManagerFactory(configurationProperties);
        return factory.parameterManagerClient(credentialsProvider, transportChannelProvider);
    }
}
