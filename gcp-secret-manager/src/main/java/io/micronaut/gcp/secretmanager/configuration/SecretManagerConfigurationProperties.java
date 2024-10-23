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
package io.micronaut.gcp.secretmanager.configuration;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.gcp.GoogleCloudConfiguration;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Configuration for SecretManager clients and config client integration.
 * @author Vinicius Carvalho
 * @since 3.4.0
 */
@ConfigurationProperties(SecretManagerConfigurationProperties.PREFIX)
@BootstrapContextCompatible
public class SecretManagerConfigurationProperties {
    public static final String PREFIX = GoogleCloudConfiguration.PREFIX + ".secret-manager";
    private static final boolean DEFAULT_DEFAULT_CONFIG_ENABLED = true;

    private Set<String> customConfigs = new LinkedHashSet<>();
    private Set<String> keys = new HashSet<>();
    private boolean defaultConfigEnabled = DEFAULT_DEFAULT_CONFIG_ENABLED;
    private String location;

    /**
     *
     * @return Custom config files to be included as property sources.
     */
    public Set<String> getCustomConfigs() {
        return customConfigs;
    }

    /**
     *
     * @param customConfigs configs to be set
     */
    public void setCustomConfigs(Set<String> customConfigs) {
        this.customConfigs = customConfigs;
    }

    /**
     *
     * @return Set of secrets to be loaded
     */
    public Set<String> getKeys() {
        return keys;
    }

    /**
     *
     * @param keys keys to be set
     */
    public void setKeys(Set<String> keys) {
        this.keys = keys;
    }

    /**
     * Whether to load the default config files (`application`, `application_${env}`, `[APPLICATION_NAME], `[APPLICATION_NAME]_${env}`). Default value: {@value #DEFAULT_DEFAULT_CONFIG_ENABLED}.
     * @return Whether to load the default config files.
     * @since 6.1.0
     */
    public boolean isDefaultConfigEnabled() {
        return defaultConfigEnabled;
    }

    /**
     *
     * @param defaultConfigEnabled Whether to load the default config files.
     * @since 6.1.0
     */
    public void setDefaultConfigEnabled(boolean defaultConfigEnabled) {
        this.defaultConfigEnabled = defaultConfigEnabled;
    }

    /**
     * Specifies the location of the regional secrets used to create a {@link com.google.cloud.secretmanager.v1.SecretManagerServiceClient} specific to the location endpoint.
     * If not provided, the client will be created using the global endpoint.
     * It must be one of the available location for the regional endpoints.
     * See <a href="https://cloud.google.com/secret-manager/docs/locations">Secret Manager locations</a> for more information.
     * @return Location of the regional secrets
     */
    public String getLocation() {
        return location;
    }

    /**
     *
     * @param location Sets the location of the regional secrets
     */
    public void setLocation(String location) {
        this.location = location;
    }
}
