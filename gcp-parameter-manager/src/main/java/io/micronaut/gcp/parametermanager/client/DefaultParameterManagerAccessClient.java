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
package io.micronaut.gcp.parametermanager.client;

import com.google.api.core.ApiFuture;
import com.google.cloud.parametermanager.v1.*;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.parametermanager.configuration.ParameterManagerConfigurationProperties;
import io.micronaut.scheduling.TaskExecutors;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Default implementation of {@link ParameterManagerAccessClient}.
 *
 * @author Dhaval Bhensdadiya
 * @since 6.0.0
 */
@Singleton
@BootstrapContextCompatible
@Requires(classes = ParameterManagerClient.class)
public class DefaultParameterManagerAccessClient implements ParameterManagerAccessClient {

    private static final Logger LOG =
        LoggerFactory.getLogger(DefaultParameterManagerAccessClient.class);
    private final ParameterManagerClient client;
    private final GoogleCloudConfiguration googleCloudConfiguration;
    private final ExecutorService executorService;
    private final ParameterManagerConfigurationProperties configurationProperties;

    /**
     * Constructor for the {@link DefaultParameterManagerAccessClient}.
     *
     * @param client                   - The client for the GCP Parameter Manager.
     * @param googleCloudConfiguration - The Google Cloud Configuration.
     * @param executorService          - optional {@link ExecutorService} for executing blocking
     *                                 tasks; may be null.
     * @param configurationProperties  - The Configuration for Parameter Manager client.
     */
    @Inject
    public DefaultParameterManagerAccessClient(ParameterManagerClient client,
                                               GoogleCloudConfiguration googleCloudConfiguration,
                                               @Nullable @Named(TaskExecutors.BLOCKING)
                                               ExecutorService executorService,
                                               ParameterManagerConfigurationProperties configurationProperties) {
        this.client = client;
        this.googleCloudConfiguration = googleCloudConfiguration;
        this.executorService =
            executorService != null ? executorService : Executors.newSingleThreadExecutor();
        this.configurationProperties = configurationProperties;
    }

    @Override
    public Mono<VersionedParameter> getParameter(String parameterName, String version) {
        return getParameter(parameterName, version, googleCloudConfiguration.getProjectId());
    }

    @Override
    public Mono<VersionedParameter> getParameter(String parameterName, String version,
                                                 String projectId) {
        if (LOG.isDebugEnabled()) {
            if (StringUtils.isNotEmpty(configurationProperties.getLocation())) {
                LOG.debug("Fetching Parameter: projects/{}/locations/{}/parameters/{}/versions/{}",
                    projectId, configurationProperties.getLocation(), parameterName, version);
            } else {
                LOG.debug(
                    "Fetching Parameter: projects/{}/locations/global/parameters/{}/versions/{}",
                    projectId, parameterName, version);
            }
        }

        ParameterVersionName parameterVersionName =
            getParameterVersionName(projectId, parameterName, version);
        GetParameterVersionRequest request =
            GetParameterVersionRequest.newBuilder().setName(parameterVersionName.toString())
                .build();

        final Mono<ParameterVersion> mono = Mono.create((sink) -> {
            final ApiFuture<ParameterVersion> future =
                client.getParameterVersionCallable().futureCall(request);
            future.addListener(() -> {
                try {
                    final ParameterVersion result = future.get();

                    // Check for disabled Parameter Version to raise exception
                    if (result.getDisabled()) {
                        sink.error(new IllegalStateException("Parameter version is disabled"));
                        return;
                    }

                    sink.success(result);
                } catch (Throwable e) {
                    sink.error(e);
                }
            }, executorService);
        });

        return mono.map(
                response -> getVersionedParameter(projectId, parameterName, version, response))
            .onErrorResume(e -> {
                LOG.warn("Error while fetching the Parameter {}: {}", parameterVersionName,
                    e.getMessage());
                return Mono.empty();
            });
    }

    @Override
    public Mono<VersionedParameter> getRenderedParameter(String parameterName, String version) {
        return getRenderedParameter(parameterName, version,
            googleCloudConfiguration.getProjectId());
    }

    @Override
    public Mono<VersionedParameter> getRenderedParameter(String parameterName, String version,
                                                         String projectId) {
        if (LOG.isDebugEnabled()) {
            if (StringUtils.isNotEmpty(configurationProperties.getLocation())) {
                LOG.debug("Rendering Parameter: projects/{}/locations/{}/parameters/{}/versions/{}",
                    projectId, configurationProperties.getLocation(), parameterName, version);
            } else {
                LOG.debug(
                    "Rendering Parameter: projects/{}/locations/global/parameters/{}/versions/{}",
                    projectId, parameterName, version);
            }
        }

        ParameterVersionName parameterVersionName =
            getParameterVersionName(projectId, parameterName, version);
        RenderParameterVersionRequest request =
            RenderParameterVersionRequest.newBuilder().setName(parameterVersionName.toString())
                .build();

        final Mono<RenderParameterVersionResponse> mono = Mono.create((sink) -> {
            final ApiFuture<RenderParameterVersionResponse> future =
                client.renderParameterVersionCallable().futureCall(request);
            future.addListener(() -> {
                try {
                    final RenderParameterVersionResponse result = future.get();
                    sink.success(result);
                } catch (Throwable e) {
                    sink.error(e);
                }
            }, executorService);
        });

        return mono.map(
                response -> getRenderedVersionedParameter(projectId, parameterName, version,
                    response))
            .onErrorResume(e -> {
                LOG.warn("Error while rendering the Parameter {}: {}", parameterVersionName,
                    e.getMessage());
                return Mono.empty();
            });
    }

    /**
     * Helper method to construct a
     * {@link com.google.cloud.parametermanager.v1.ParameterVersionName}.
     *
     * @param projectId     - The GCP project ID.
     * @param parameterName - The name of the parameter.
     * @param version       - The version of the parameter.
     * @return The {@link ParameterVersionName} for the given inputs.
     */
    private ParameterVersionName getParameterVersionName(String projectId, String parameterName,
                                                         String version) {
        return StringUtils.isEmpty(configurationProperties.getLocation()) ?
            ParameterVersionName.of(projectId, "global", parameterName, version) :
            ParameterVersionName.of(projectId, configurationProperties.getLocation(), parameterName,
                version);
    }

    /**
     * Helper method to convert {@link com.google.cloud.parametermanager.v1.ParameterVersion}
     * into a {@link VersionedParameter}.
     *
     * @param projectId     - The GCP project ID.
     * @param parameterName - The name of the parameter.
     * @param version       - The version of the parameter.
     * @param response      - The {@link com.google.cloud.parametermanager.v1.ParameterVersion}
     *                     containing parameter data.
     * @return A {@link VersionedParameter} object containing the parameter value.
     */
    private VersionedParameter getVersionedParameter(String projectId, String parameterName,
                                                     String version, ParameterVersion response) {
        return StringUtils.isEmpty(configurationProperties.getLocation()) ?
            new VersionedParameter(projectId, "global", parameterName, version,response.getPayload().getData().toByteArray()) :
            new VersionedParameter(projectId, configurationProperties.getLocation(), parameterName,
                version, response.getPayload().getData().toByteArray());
    }

    /**
     * Helper method to convert
     * {@link com.google.cloud.parametermanager.v1.RenderParameterVersionResponse} into a
     * {@link VersionedParameter}.
     *
     * @param projectId     - The GCP project ID.
     * @param parameterName - The name of the parameter.
     * @param version       - The version of the parameter.
     * @param response      - The
     * {@link com.google.cloud.parametermanager.v1.RenderParameterVersionResponse} containing
     *                      rendered parameter data.
     * @return A {@link VersionedParameter} object containing the parameter value.
     */
    private VersionedParameter getRenderedVersionedParameter(String projectId, String parameterName,
                                                             String version,
                                                             RenderParameterVersionResponse response) {
        return StringUtils.isEmpty(configurationProperties.getLocation()) ?
            new VersionedParameter(projectId, "global", parameterName, version,
                response.getRenderedPayload().toByteArray()) :
            new VersionedParameter(projectId, configurationProperties.getLocation(), parameterName,
                version, response.getRenderedPayload().toByteArray());
    }
}
