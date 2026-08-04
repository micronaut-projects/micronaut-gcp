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

import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterVersionName;
import com.google.cloud.parametermanager.v1.RenderParameterVersionRequest;
import com.google.cloud.parametermanager.v1.RenderParameterVersionResponse;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.StringUtils;
import io.micronaut.gcp.parametermanager.client.VersionedParameter;
import io.micronaut.gcp.parametermanager.configuration.ParameterManagerConfigurationProperties;

/**
 * Helper methods for building Parameter Manager render requests and mapping responses.
 *
 * @since 6.1.0
 */
@Internal
public final class ParameterManagerParameterAccessor {

    private static final String GLOBAL = "global";

    private ParameterManagerParameterAccessor() {
    }

    public static ParameterVersionName parameterVersionName(String projectId,
                                                             String parameterName,
                                                             String version,
                                                             ParameterManagerConfigurationProperties configurationProperties) {
        return StringUtils.isEmpty(configurationProperties.getLocation())
            ? ParameterVersionName.of(projectId, GLOBAL, parameterName, version)
            : ParameterVersionName.of(projectId, configurationProperties.getLocation(), parameterName, version);
    }

    public static VersionedParameter renderParameter(ParameterManagerClient client,
                                                      String projectId,
                                                      String parameterName,
                                                      String version,
                                                      ParameterManagerConfigurationProperties configurationProperties) {
        ParameterVersionName parameterVersionName = parameterVersionName(projectId, parameterName, version, configurationProperties);
        RenderParameterVersionRequest request = RenderParameterVersionRequest.newBuilder()
            .setName(parameterVersionName.toString())
            .build();
        RenderParameterVersionResponse response = client.renderParameterVersion(request);
        String location = StringUtils.isEmpty(configurationProperties.getLocation()) ? GLOBAL : configurationProperties.getLocation();
        return new VersionedParameter(projectId, location, parameterName, version, response.getRenderedPayload().toByteArray());
    }
}
