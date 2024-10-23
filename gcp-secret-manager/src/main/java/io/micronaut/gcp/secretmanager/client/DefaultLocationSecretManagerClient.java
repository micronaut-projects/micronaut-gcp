/*
 * Copyright 2017-2024 original authors
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

import com.google.api.core.ApiFuture;
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.secretmanager.configuration.SecretManagerConfigurationProperties;
import io.micronaut.scheduling.TaskExecutors;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Default regional implementation of {@link SecretManagerClient}.
 * @author Alfatah Bheda
 */
@Singleton
@BootstrapContextCompatible
@Requires(classes = SecretManagerServiceClient.class)
@Requires(property = "gcp.secret-manager.location")
public class DefaultLocationSecretManagerClient implements SecretManagerClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLocationSecretManagerClient.class);
    private final SecretManagerServiceClient client;
    private final GoogleCloudConfiguration googleCloudConfiguration;
    private final ExecutorService executorService;
    private final SecretManagerConfigurationProperties configurationProperties;

    public DefaultLocationSecretManagerClient(
            SecretManagerServiceClient client,
            GoogleCloudConfiguration googleCloudConfiguration,
            @Nullable @Named(TaskExecutors.IO) ExecutorService executorService,
            SecretManagerConfigurationProperties configurationProperties) {
        this.client = client;
        this.googleCloudConfiguration = googleCloudConfiguration;
        this.executorService = executorService != null ? executorService : Executors.newSingleThreadExecutor()  ;
        this.configurationProperties = configurationProperties;
    }

    @Override
    public Mono<VersionedSecret> getSecret(String secretId) {
        return getSecret(secretId, LATEST , googleCloudConfiguration.getProjectId());
    }

    @Override
    public Mono<VersionedSecret> getSecret(String secretId, String version) {
        return getSecret(secretId, version, googleCloudConfiguration.getProjectId());
    }

    @Override
    public Mono<VersionedSecret> getSecret(String secretId, String version, String projectId) {
        String location = configurationProperties.getLocation();
        LOGGER.debug("Fetching secret: projects/{}/locations/{}/secrets/{}/{}", projectId, location, secretId, version);
        SecretVersionName secretVersionName = SecretVersionName.ofProjectLocationSecretSecretVersionName(projectId, location, secretId, version);
        AccessSecretVersionRequest request = AccessSecretVersionRequest.newBuilder()
                .setName(secretVersionName.toString())
                .build();
        final Mono<AccessSecretVersionResponse> mono = Mono.create((sink) -> {
            final ApiFuture<AccessSecretVersionResponse> future = client
                    .accessSecretVersionCallable().futureCall(request);
            future.addListener(() -> {
                try {
                    final AccessSecretVersionResponse result = future.get();
                    sink.success(result);
                } catch (Throwable e) {
                    sink.error(e);
                }
            }, executorService);
        });

        return mono
                .map(response -> new VersionedSecret(secretId, projectId, version, response.getPayload().getData().toByteArray(), location))
                .onErrorResume(throwable -> Mono.empty());
    }
}
