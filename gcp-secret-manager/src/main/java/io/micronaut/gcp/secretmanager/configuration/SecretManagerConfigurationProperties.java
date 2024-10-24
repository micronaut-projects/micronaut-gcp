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
import io.micronaut.core.annotation.Nullable;
import io.micronaut.gcp.GoogleCloudConfiguration;
import jakarta.validation.constraints.Pattern;

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

    /**
     * Secret Manager Locations.
     * <a href="https://cloud.google.com/secret-manager/docs/locations">Secret Manger Locations</a>
     *
     */
    // Locations in Asia Pacific
    private static final String DELHI = "asia-south2";
    private static final String HONG_KONG = "asia-east2";
    private static final String JAKARTA = "asia-southeast2";
    private static final String MELBOURNE = "australia-southeast2";
    private static final String MUMBAI = "asia-south1";
    private static final String OSAKA = "asia-northeast2";
    private static final String SEOUL = "asia-northeast3";
    private static final String SINGAPORE = "asia-southeast1";
    private static final String SYDNEY = "australia-southeast1";
    private static final String TAIWAN = "asia-east1";
    private static final String TOKYO = "asia-northeast1";

    // Locations in Europe
    private static final String BELGIUM = "europe-west1";
    private static final String BERLIN = "europe-west10";
    private static final String FINLAND = "europe-north1";
    private static final String FRANKFURT = "europe-west3";
    private static final String LONDON = "europe-west2";
    private static final String MILAN = "europe-west8";
    private static final String NETHERLANDS = "europe-west4";
    private static final String PARIS = "europe-west9";
    private static final String TURIN = "europe-west12";
    private static final String WARSAW = "europe-central2";
    private static final String ZURICH = "europe-west6";

    // Locations in North America
    private static final String COLUMBUS = "us-east5";
    private static final String DALLAS = "us-south1";
    private static final String IOWA = "us-central1";
    private static final String LAS_VEGAS = "us-west4";
    private static final String LOS_ANGELES = "us-west2";
    private static final String MONTREAL = "northamerica-northeast1";
    private static final String NORTHERN_VIRGINIA = "us-east4";
    private static final String OREGON = "us-west1";
    private static final String SALT_LAKE_CITY = "us-west3";
    private static final String SOUTH_CAROLINA = "us-east1";
    private static final String TORONTO = "northamerica-northeast2";

    // Locations in South America
    private static final String SANTIAGO = "southamerica-west1";
    private static final String SAO_PAULO = "southamerica-east1";

    // Locations in Middle East
    private static final String DAMMAM = "me-central2";
    private static final String DOHA = "me-central1";
    private static final String TEL_AVIV = "me-west1";

    // Locations in Africa
    private static final String JOHANNESBURG = "africa-south1";

    private Set<String> customConfigs = new LinkedHashSet<>();
    private Set<String> keys = new HashSet<>();
    private boolean defaultConfigEnabled = DEFAULT_DEFAULT_CONFIG_ENABLED;

    @Pattern(regexp = DELHI + "|" + HONG_KONG + "|" + JAKARTA + "|" + MELBOURNE + "|" + MUMBAI + "|" + OSAKA + "|" + SEOUL + "|" + SINGAPORE + "|" + SYDNEY + "|" + TAIWAN + "|" + TOKYO
            + "|" + BELGIUM + "|" + BERLIN + "|" + FINLAND + "|" + FRANKFURT + "|" + LONDON + "|" + MILAN + "|" + NETHERLANDS + "|" + PARIS + "|" + TURIN + "|" + WARSAW + "|" + ZURICH
            + "|" + COLUMBUS + "|" + DALLAS + "|" + IOWA + "|" + LAS_VEGAS + "|" + LOS_ANGELES + "|" + MONTREAL + "|" + NORTHERN_VIRGINIA + "|" + OREGON + "|" + SALT_LAKE_CITY + "|" + SOUTH_CAROLINA + "|" + TORONTO
            + "|" + SANTIAGO + "|" + SAO_PAULO + "|" + DAMMAM + "|" + DOHA + "|" + TEL_AVIV + "|" + JOHANNESBURG)
    @Nullable
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
    @Nullable
    public String getLocation() {
        return location;
    }

    /**
     *
     * @param location Sets the location of the regional secrets
     */
    public void setLocation(@Nullable String location) {
        this.location = location;
    }
}
