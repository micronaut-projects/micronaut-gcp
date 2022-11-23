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
package io.micronaut.gcp.http.client;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;

/**
 * Creates a GoogleAuthServiceConfig for each Service configured under
 * gcp.http.client.auth.services.*.audience. The audience can be configured per
 * service and the correct config bean is selected in {@code GoogleAuthFilter} via the service id
 * inside the corresponding request.
 *
 * Requires the user to set the {@code gcp.http.client.auth.services.*.audience} property with the
 * desired audience to create the corresponding config bean.
 *
 * @author kgreulich
 * @since 1.0.0
 */
@EachProperty(GoogleAuthServiceConfig.PREFIX)
public class GoogleAuthServiceConfig {

    public static final String PREFIX = "gcp.http.client.auth.services";
    private final String serviceId;

    private String audience;

    public GoogleAuthServiceConfig(
        @Parameter String serviceId
    ) {
        this.serviceId = serviceId;
    }

    /**
     * @return the audience service identifier
     */
    public String getServiceId() {
        return serviceId;
    }

    /**
     * @return the desired audience
     */
    public String getAudience() {
        return audience;
    }

    /**
     * @param audience set the desired audience
     */
    public void setAudience(final String audience) {
        this.audience = audience;
    }
}
