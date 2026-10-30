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
package io.micronaut.gcp.parametermanager.configuration;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.gcp.GoogleCloudConfiguration;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Configuration for ParameterManager clients and config client integration.
 *
 * @author Dhaval Bhensdadiya
 * @since 6.0.0
 */
@ConfigurationProperties(ParameterManagerConfigurationProperties.PREFIX)
@BootstrapContextCompatible
public class ParameterManagerConfigurationProperties {
    public static final String PREFIX = GoogleCloudConfiguration.PREFIX + ".parameter-manager";

    @Nullable
    private String location;

    private Set<String> customConfigs = new LinkedHashSet<>();
    private Set<String> keys = new HashSet<>();

    /**
     * Specifies the location of the regional parameters used to create a
     * {@link com.google.cloud.parametermanager.v1.ParameterManagerClient} specific to the
     * location endpoint.
     * If not provided, the client will be created using the global endpoint.
     * It must be a location supported by Parameter Manager, such as {@code us-east1} or {@code us}.
     * See
     * <a href="https://docs.cloud.google.com/secret-manager/docs/locations#parameter_manager_locations">Parameter Manager locations</a> for more information.
     *
     * @return Location of the regional parameters.
     */
    @Nullable
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of the regional parameters.
     *
     * @param location - Location of the regional parameters.
     */
    public void setLocation(@Nullable String location) {
        this.location = location;
    }

    /**
     * Specifies the custom config files to be included as property sources.
     *
     * @return Set of custom config files to be included as property sources.
     */
    public Set<String> getCustomConfigs() {
        return customConfigs;
    }

    /**
     * Sets the custom config files to be included as property sources.
     *
     * @param customConfigs - configs to be set.
     */
    public void setCustomConfigs(Set<String> customConfigs) {
        this.customConfigs = customConfigs;
    }

    /**
     * Specifies the parameters to be loaded as property sources.
     *
     * @return Set of parameters to be loaded as property sources.
     */
    public Set<String> getKeys() {
        return keys;
    }

    /**
     * Sets the parameters to be loaded as property sources.
     *
     * @param keys - keys to be set.
     */
    public void setKeys(Set<String> keys) {
        this.keys = keys;
    }
}
