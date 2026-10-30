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

import io.micronaut.core.async.annotation.SingleResult;
import org.reactivestreams.Publisher;

/**
 * This interface is intended to abstract interactions with
 * {@link com.google.cloud.parametermanager.v1.ParameterManagerClient}, and instead of returning
 * Google's {@link com.google.api.core.ApiFuture}
 * transform it on reactive extensions.
 *
 * @author Dhaval Bhensdadiya
 * @since 6.0.0
 */
public interface ParameterManagerAccessClient {

    /**
     * Fetches a parameter from the Parameter Manager using the `gcp.projectId` project.
     *
     * @param parameterName - name of the parameter
     * @param version       - version of the parameter
     * @return String value of the parameter or empty
     */
    @SingleResult
    Publisher<VersionedParameter> getParameter(String parameterName, String version);

    /**
     * Fetches a parameter from the Parameter Manager.
     *
     * @param parameterName - name of the parameter
     * @param version       - version of the parameter
     * @param projectId     - project identifier
     * @return String value of the parameter or empty
     */
    @SingleResult
    Publisher<VersionedParameter> getParameter(String parameterName, String version,
                                               String projectId);

    /**
     * Renders a parameter from the Parameter Manager using the `gcp.projectId` project.
     *
     * @param parameterName - name of the parameter
     * @param version       - version of the parameter
     * @return String value of the parameter or empty
     */
    @SingleResult
    Publisher<VersionedParameter> getRenderedParameter(String parameterName, String version);

    /**
     * Renders a parameter from the Parameter Manager.
     *
     * @param parameterName - name of the parameter
     * @param version       - version of the parameter
     * @param projectId     - project identifier
     * @return String value of the parameter or empty
     */
    @SingleResult
    Publisher<VersionedParameter> getRenderedParameter(String parameterName, String version,
                                                       String projectId);
}
