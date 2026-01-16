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
import jakarta.validation.constraints.Pattern;
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

    /**
     * Parameter Manager Locations.
     * <a href="https://docs.cloud.google.com/secret-manager/docs/locations#parameter_manager_locations">Parameter Manager Locations</a>
     */
    // Locations in Asia Pacific
    private static final String TAIWAN = "asia-east1";
    private static final String HONG_KONG = "asia-east2";
    private static final String TOKYO = "asia-northeast1";
    private static final String OSAKA = "asia-northeast2";
    private static final String SEOUL = "asia-northeast3";
    private static final String MUMBAI = "asia-south1";
    private static final String DELHI = "asia-south2";
    private static final String SINGAPORE = "asia-southeast1";
    private static final String JAKARTA = "asia-southeast2";
    private static final String SYDNEY = "australia-southeast1";
    private static final String MELBOURNE = "australia-southeast2";

    // Locations in Europe
    private static final String WARSAW = "europe-central2";
    private static final String FINLAND = "europe-north1";
    private static final String STOCKHOLM = "europe-north2";
    private static final String MADRID = "europe-southwest1";
    private static final String BELGIUM = "europe-west1";
    private static final String BERLIN = "europe-west10";
    private static final String TURIN = "europe-west12";
    private static final String LONDON = "europe-west2";
    private static final String FRANKFURT = "europe-west3";
    private static final String NETHERLANDS = "europe-west4";
    private static final String ZURICH = "europe-west6";
    private static final String MILAN = "europe-west8";
    private static final String PARIS = "europe-west9";

    // Locations in North America
    private static final String MONTREAL = "northamerica-northeast1";
    private static final String TORONTO = "northamerica-northeast2";
    private static final String MEXICO = "northamerica-south1";
    private static final String IOWA = "us-central1";
    private static final String SOUTH_CAROLINA = "us-east1";
    private static final String NORTHERN_VIRGINIA = "us-east4";
    private static final String COLUMBUS = "us-east5";
    private static final String DALLAS = "us-south1";
    private static final String OREGON = "us-west1";
    private static final String LOS_ANGELES = "us-west2";
    private static final String SALT_LAKE_CITY = "us-west3";
    private static final String LAS_VEGAS = "us-west4";

    // Locations in South America
    private static final String SAO_PAULO = "southamerica-east1";
    private static final String SANTIAGO = "southamerica-west1";

    // Locations in Middle East
    private static final String DOHA = "me-central1";
    private static final String DAMMAM = "me-central2";
    private static final String TEL_AVIV = "me-west1";

    // Locations in Africa
    private static final String JOHANNESBURG = "africa-south1";

    @Pattern(regexp = TAIWAN + "|" + HONG_KONG + "|" + TOKYO + "|" + OSAKA + "|" + SEOUL + "|" + MUMBAI + "|" + DELHI + "|" + SINGAPORE + "|" + JAKARTA + "|" + SYDNEY + "|" + MELBOURNE
        + "|" + WARSAW + "|" + FINLAND + "|" + STOCKHOLM + "|" + MADRID + "|" + BELGIUM + "|" + BERLIN + "|" + TURIN + "|" + LONDON + "|" + FRANKFURT + "|" + NETHERLANDS + "|" + ZURICH + "|" + MILAN + "|" + PARIS
        + "|" + MONTREAL + "|" + TORONTO + "|" + MEXICO + "|" + IOWA + "|" + SOUTH_CAROLINA + "|" + NORTHERN_VIRGINIA + "|" + COLUMBUS + "|" + DALLAS + "|" + OREGON + "|" + LOS_ANGELES + "|" + SALT_LAKE_CITY + "|" + LAS_VEGAS
        + "|" + SAO_PAULO + "|" + SANTIAGO + "|" + DOHA + "|" + DAMMAM + "|" + TEL_AVIV + "|" + JOHANNESBURG)
    @Nullable
    private String location;

    private Set<String> customConfigs = new LinkedHashSet<>();
    private Set<String> keys = new HashSet<>();

    /**
     * Specifies the location of the regional parameters used to create a
     * {@link com.google.cloud.parametermanager.v1.ParameterManagerClient} specific to the
     * location endpoint.
     * If not provided, the client will be created using the global endpoint.
     * It must be one of the available location for the regional endpoints.
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
