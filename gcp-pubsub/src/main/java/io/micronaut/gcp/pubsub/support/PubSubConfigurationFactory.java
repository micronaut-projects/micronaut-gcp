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
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.pubsub.v1.Publisher;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.UserAgentHeaderProvider;
import io.micronaut.gcp.pubsub.configuration.PubSubConfigurationProperties;
import org.threeten.bp.Duration;

import javax.inject.Singleton;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

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
     * @return default {@link TransportChannelProvider}ansportChannelProvider
     */
    @Singleton
    public TransportChannelProvider transportChannelProvider() {
        return InstantiatingGrpcChannelProvider.newBuilder()
                .setHeaderProvider(new UserAgentHeaderProvider("pubsub"))
                .setKeepAliveTime(Duration.ofMinutes(this.pubSubConfigurationProperties.getKeepAliveIntervalMinutes()))
                .build();
    }

    /**
     * Returns a default {@link CredentialsProvider}, allows users to override it and provide their own implementation.
     * @param credentials default credentials, if not overriden by user should be provided by {@link io.micronaut.gcp.credentials.GoogleCredentialsFactory}
     * @return A {@link FixedCredentialsProvider} holding the given credentials.
     */
    @Singleton
    public CredentialsProvider credentialsProvider(GoogleCredentials credentials) {
        return FixedCredentialsProvider.create(credentials);
    }

    /**
     * A helper method for applying properties to settings builders for purpose of seeing if at least
     * one setting was set.
     *
     * @param prop     the property on which to operate
     * @param consumer the function to give the property
     * @param <T>      the type of the property
     * @return a function that accepts a boolean of if there is a next property and returns a boolean indicating if the
     * propety was set
     */
    private <T> Function<Boolean, Boolean> ifNotNull(T prop, Consumer<T> consumer) {

        return (next) -> {
            boolean wasSet = next;
            if (prop != null) {
                consumer.accept(prop);
                wasSet = true;
            }
            return wasSet;
        };
    }
}
