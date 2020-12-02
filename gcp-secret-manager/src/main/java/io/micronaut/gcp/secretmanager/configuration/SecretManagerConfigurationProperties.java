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

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.gcp.GoogleCloudConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuration for SecretManager clients and config client integration.
 * @author Vinicius Carvalho
 * @since 3.2.0
 */
@ConfigurationProperties(SecretManagerConfigurationProperties.PREFIX)
public class SecretManagerConfigurationProperties {
    public static final String PREFIX = GoogleCloudConfiguration.PREFIX + ".secret-manager";

    private Set<String> customConfigs = new HashSet<>();
    private Set<String> keys = new HashSet<>();

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
}
