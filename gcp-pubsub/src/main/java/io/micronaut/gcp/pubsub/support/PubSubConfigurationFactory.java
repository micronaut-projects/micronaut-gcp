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
package io.micronaut.gcp.pubsub.support;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.core.FixedExecutorProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import io.grpc.ManagedChannelBuilder;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.Modules;
import io.micronaut.gcp.UserAgentHeaderProvider;
import io.micronaut.gcp.pubsub.configuration.PubSubConfigurationProperties;
import org.threeten.bp.Duration;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.Executors;

/**
 * Factory class to create default settings for PubSub Publisher and subscriber beans.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 *
 */
@Factory
@Requires(classes = Publisher.class)
public class PubSubConfigurationFactory {

    private final PubSubConfigurationProperties pubSubConfigurationProperties;

    private final GoogleCloudConfiguration googleCloudConfiguration;

    public PubSubConfigurationFactory(PubSubConfigurationProperties pubSubConfigurationProperties, GoogleCloudConfiguration googleCloudConfiguration) {
        this.pubSubConfigurationProperties = pubSubConfigurationProperties;
        this.googleCloudConfiguration = googleCloudConfiguration;
    }

    /**
     *
     * @return default {@link ExecutorProvider}
     */
    @Singleton
    public ExecutorProvider publisherExecutorProvider() {
        //TODO needs to provide better scheduled executor
        return FixedExecutorProvider.create(Executors.newScheduledThreadPool(1));
    }

    /**
     *
     * @return default {@link TransportChannelProvider}TransportChannelProvider
     */
    @Singleton
    @Named(Modules.PUBSUB)
    @Requires(missingProperty = "pubsub.emulator.host")
    public TransportChannelProvider transportChannelProvider() {
        return InstantiatingGrpcChannelProvider.newBuilder()
                .setHeaderProvider(new UserAgentHeaderProvider(Modules.PUBSUB))
                .setKeepAliveTime(Duration.ofMinutes(this.pubSubConfigurationProperties.getKeepAliveIntervalMinutes()))
                .build();
    }

    /**
     * @param environment - Micronaut Environment to fetch PUBSUB_EMULATOR_HOST value
     * @return a {@link TransportChannelProvider} that targets the PUBSUB_EMULATOR_HOST
     */
    @Singleton
    @Named(Modules.PUBSUB)
    @Requires(property = "pubsub.emulator.host")
    public TransportChannelProvider localChannelProvider(Environment environment) {
        String host = environment.getProperty("pubsub.emulator.host", String.class).get();
        return FixedTransportChannelProvider.create(GrpcTransportChannel.create(ManagedChannelBuilder.forTarget(host).usePlaintext().build()));
    }

    /**
     * Returns a default {@link CredentialsProvider}, allows users to override it and provide their own implementation.
     * @param credentials default credentials, if not overridden by user should be provided by {@link io.micronaut.gcp.credentials.GoogleCredentialsFactory}
     * @return A {@link FixedCredentialsProvider} holding the given credentials.
     */
    @Singleton
    @Named(Modules.PUBSUB)
    @Requires(missingProperty = "pubsub.emulator.host")
    public CredentialsProvider credentialsProvider(GoogleCredentials credentials) {
        return FixedCredentialsProvider.create(credentials);
    }

    /**
     * Returns a {@link NoCredentialsProvider}. Useful for when running with an emulator instead of targeting GCP Pub/Sub service.
     * @return A {@link NoCredentialsProvider} with no credentials.
     */
    @Singleton
    @Named(Modules.PUBSUB)
    @Requires(property = "pubsub.emulator.host")
    public CredentialsProvider noCredentialsProvider() {
        return NoCredentialsProvider.create();
    }

}
