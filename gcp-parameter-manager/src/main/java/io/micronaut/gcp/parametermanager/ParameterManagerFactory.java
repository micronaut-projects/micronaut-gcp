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
package io.micronaut.gcp.parametermanager;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterManagerSettings;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.gcp.Modules;
import io.micronaut.gcp.UserAgentHeaderProvider;
import io.micronaut.gcp.parametermanager.configuration.ParameterManagerConfigurationProperties;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.io.IOException;

/**
 * Factory to create ParameterManager clients.
 *
 * @author Dhaval Bhensdadiya
 * @since 6.0.0
 */
@Factory
@Requires(classes = {ParameterManagerClient.class})
@BootstrapContextCompatible
public class ParameterManagerFactory {

    private static final String REGIONAL_ENDPOINT = "parametermanager.%s.rep.googleapis.com:443";
    private final ParameterManagerConfigurationProperties configurationProperties;

    /**
     * Constructor for the {@link ParameterManagerFactory}.
     *
     * @param configurationProperties Parameter Manager Configuration Properties
     */
    @Inject
    public ParameterManagerFactory(
        ParameterManagerConfigurationProperties configurationProperties) {
        this.configurationProperties = configurationProperties;
    }

    /**
     * Creates a {@link ParameterManagerClient} instance.
     *
     * @param credentialsProvider      - Google Cloud Credentials Provider
     * @param transportChannelProvider - Transport Channel Provider
     * @return an instance using defaults.
     */
    @Singleton
    public ParameterManagerClient parameterManagerClient(
        @Named(Modules.PARAMETER_MANAGER) CredentialsProvider credentialsProvider,
        @Named(Modules.PARAMETER_MANAGER) TransportChannelProvider transportChannelProvider) {
        try {
            ParameterManagerSettings.Builder builder = ParameterManagerSettings.newBuilder();
            if (configurationProperties != null &&
                StringUtils.isNotEmpty(configurationProperties.getLocation())) {
                builder.setEndpoint(
                    String.format(REGIONAL_ENDPOINT, configurationProperties.getLocation()));
            }
            ParameterManagerSettings settings = builder.setCredentialsProvider(credentialsProvider)
                .setTransportChannelProvider(transportChannelProvider).build();

            return ParameterManagerClient.create(settings);
        } catch (IOException e) {
            throw new IllegalStateException("Could not instantiate ParameterManagerClient", e);
        }
    }

    /**
     * Returns a default {@link CredentialsProvider}, allows users to override it and provide
     * their own implementation.
     *
     * @param credentials default credentials, if not overridden by user should be provided by
     *                    {@link io.micronaut.gcp.credentials.GoogleCredentialsFactory}
     * @return A {@link FixedCredentialsProvider} holding the given credentials.
     */
    @Singleton
    @Named(Modules.PARAMETER_MANAGER)
    public CredentialsProvider credentialsProvider(GoogleCredentials credentials)
        throws IOException {
        return FixedCredentialsProvider.create(credentials);
    }

    /**
     * Returns the default {@link TransportChannelProvider}.
     *
     * @return default {@link TransportChannelProvider} TransportChannelProvider
     */
    @Singleton
    @Named(Modules.PARAMETER_MANAGER)
    public TransportChannelProvider transportChannelProvider() {
        return InstantiatingGrpcChannelProvider.newBuilder()
            .setHeaderProvider(new UserAgentHeaderProvider(Modules.PARAMETER_MANAGER)).build();
    }
}
