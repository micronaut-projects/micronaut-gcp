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
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Retain;
import io.micronaut.context.env.Environment;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.Modules;
import io.micronaut.gcp.UserAgentHeaderProvider;
import io.micronaut.gcp.pubsub.configuration.PubSubConfigurationProperties;
import org.jspecify.annotations.Nullable;
import org.threeten.bp.Duration;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Factory class to create default settings for PubSub Publisher and subscriber beans.
 *
 * <p>Development mode retains the transport channel provider, with the channel of the emulator, the credentials
 * provider and the executor provider across a restart of the application, until a change under
 * {@value GoogleCloudConfiguration#PREFIX} or {@value #EMULATOR_PREFIX} releases them.</p>
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 *
 */
@Factory
@Requires(classes = Publisher.class)
public class PubSubConfigurationFactory {

    /**
     * The prefix of the emulator configuration: {@code pubsub.emulator.host} names the emulator to connect to.
     *
     * @since 6.3.0
     */
    public static final String EMULATOR_PREFIX = "pubsub.emulator";

    private static final String EMULATOR_HOST = EMULATOR_PREFIX + ".host";

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
    @Retain(invalidatedBy = GoogleCloudConfiguration.PREFIX)
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
    @Requires(missingProperty = EMULATOR_HOST)
    @Retain(invalidatedBy = GoogleCloudConfiguration.PREFIX)
    public TransportChannelProvider transportChannelProvider() {
        return InstantiatingGrpcChannelProvider.newBuilder()
                .setHeaderProvider(new UserAgentHeaderProvider(Modules.PUBSUB))
                .setKeepAliveTime(Duration.ofMinutes(this.pubSubConfigurationProperties.getKeepAliveIntervalMinutes()))
                .build();
    }

    /**
     * @param environment - Micronaut Environment to fetch PUBSUB_EMULATOR_HOST value
     * @return a {@link TransportChannelProvider} that targets the PUBSUB_EMULATOR_HOST
     * @deprecated The bean is created by {@link #emulatorChannelProvider(String, DevelopmentThreads)}, which receives the host rather than the environment
     */
    @Deprecated(since = "6.3.0", forRemoval = true)
    public TransportChannelProvider localChannelProvider(Environment environment) {
        return emulatorChannelProvider(environment.getProperty(EMULATOR_HOST, String.class).orElseThrow(), null);
    }

    /**
     * The channel provider of the emulator, which shares one channel to it. Development mode retains it, with the
     * channel, across a restart of the application.
     *
     * @param host The host and port of the emulator, from {@code pubsub.emulator.host}
     * @param developmentThreads Present in development mode only, where it builds the channel on a thread whose stack
     * holds no class of the application, as the channel keeps the stack trace of where it was built
     * @return a {@link TransportChannelProvider} that targets the PUBSUB_EMULATOR_HOST
     * @since 6.3.0
     */
    @Singleton
    @Named(Modules.PUBSUB)
    @Requires(property = EMULATOR_HOST)
    @Retain(invalidatedBy = {GoogleCloudConfiguration.PREFIX, EMULATOR_PREFIX})
    public TransportChannelProvider emulatorChannelProvider(@Property(name = EMULATOR_HOST) String host, @Nullable DevelopmentThreads developmentThreads) {
        Supplier<ManagedChannel> channel = () -> ManagedChannelBuilder.forTarget(host).usePlaintext().build();
        return FixedTransportChannelProvider.create(GrpcTransportChannel.create(developmentThreads == null
            ? channel.get()
            : developmentThreads.outsideTheApplication("emulator-channel", channel)));
    }

    /**
     * Returns a default {@link CredentialsProvider}, allows users to override it and provide their own implementation.
     * Development mode retains it, with the credentials, across a restart of the application.
     *
     * @param credentials default credentials, if not overridden by user should be provided by {@link io.micronaut.gcp.credentials.GoogleCredentialsFactory}
     * @return A {@link FixedCredentialsProvider} holding the given credentials.
     */
    @Singleton
    @Named(Modules.PUBSUB)
    @Requires(missingProperty = EMULATOR_HOST)
    @Retain(invalidatedBy = GoogleCloudConfiguration.PREFIX)
    public CredentialsProvider credentialsProvider(GoogleCredentials credentials) {
        return FixedCredentialsProvider.create(credentials);
    }

    /**
     * Returns a {@link NoCredentialsProvider}. Useful for when running with an emulator instead of targeting GCP Pub/Sub service.
     * @return A {@link NoCredentialsProvider} with no credentials.
     */
    @Singleton
    @Named(Modules.PUBSUB)
    @Requires(property = EMULATOR_HOST)
    @Retain(invalidatedBy = {GoogleCloudConfiguration.PREFIX, EMULATOR_PREFIX})
    public CredentialsProvider noCredentialsProvider() {
        return NoCredentialsProvider.create();
    }

}
