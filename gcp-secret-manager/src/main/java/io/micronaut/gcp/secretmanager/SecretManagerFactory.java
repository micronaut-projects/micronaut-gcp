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
package io.micronaut.gcp.secretmanager;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.gcp.Modules;
import io.micronaut.gcp.UserAgentHeaderProvider;

import io.micronaut.gcp.secretmanager.configuration.SecretManagerConfigurationProperties;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.io.IOException;

/**
 * Factory to create SecretManager clients.
 *
 * @author Vinicius Carvalho
 * @since 3.4.0
 */
@Factory
@Requires(classes = {SecretManagerServiceClient.class})
@BootstrapContextCompatible
public class SecretManagerFactory {

    private static final String REGIONAL_ENDPOINT = "secretmanager.%s.rep.googleapis.com:443";
    private final SecretManagerConfigurationProperties configurationProperties;

    /**
     *
     * @param configurationProperties SecretManager Configuration Properties
     */
    @Inject
    public SecretManagerFactory(SecretManagerConfigurationProperties configurationProperties) {
        this.configurationProperties = configurationProperties;
    }

    @Deprecated
    public SecretManagerFactory() {
        this(new SecretManagerConfigurationProperties());
    }

    /**
     * Creates a {@link SecretManagerServiceClient} instance.
     *
     * @param credentialsProvider - Google Cloud Credentials Provider
     * @param transportChannelProvider - TransportChannel Provider
     * @return an instance using defaults.
     */
    @Singleton
    public SecretManagerServiceClient secretManagerServiceClient(@Named(Modules.SECRET_MANAGER) CredentialsProvider credentialsProvider,
                                                                 @Named(Modules.SECRET_MANAGER) TransportChannelProvider transportChannelProvider) {
        try {
            SecretManagerServiceSettings.Builder builder = SecretManagerServiceSettings.newBuilder();
            if (configurationProperties != null && StringUtils.isNotEmpty(configurationProperties.getLocation())) {
                builder.setEndpoint(String.format(REGIONAL_ENDPOINT, configurationProperties.getLocation()));
            }
            SecretManagerServiceSettings settings = builder
                    .setCredentialsProvider(credentialsProvider)
                    .setTransportChannelProvider(transportChannelProvider)
                    .build();
            return SecretManagerServiceClient.create(settings);
        } catch (IOException e) {
            throw new IllegalStateException("Could not instantiate SecretManagerServiceClient", e);
        }
    }

    /**
     * Returns a default {@link CredentialsProvider}, allows users to override it and provide their own implementation.
     *
     * @param credentials default credentials, if not overridden by user should be provided by {@link io.micronaut.gcp.credentials.GoogleCredentialsFactory}
     * @return A {@link FixedCredentialsProvider} holding the given credentials.
     */
    @Singleton
    @Named(Modules.SECRET_MANAGER)
    public CredentialsProvider credentialsProvider(GoogleCredentials credentials) throws IOException {
        return FixedCredentialsProvider.create(credentials);
    }

    /**
     * @return default {@link TransportChannelProvider}TransportChannelProvider
     */
    @Singleton
    @Named(Modules.SECRET_MANAGER)
    public TransportChannelProvider transportChannelProvider() {
        return InstantiatingGrpcChannelProvider.newBuilder()
                .setHeaderProvider(new UserAgentHeaderProvider(Modules.SECRET_MANAGER))
                .build();
    }

}
