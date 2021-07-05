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
package io.micronaut.gcp.secretmanager;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.*;
import io.micronaut.context.env.yaml.YamlPropertySourceLoader;
import io.micronaut.core.util.StringUtils;
import io.micronaut.discovery.config.ConfigurationClient;
import io.micronaut.gcp.secretmanager.client.SecretManagerClient;
import io.micronaut.gcp.secretmanager.client.VersionedSecret;
import io.micronaut.gcp.secretmanager.configuration.SecretManagerConfigurationProperties;
import io.micronaut.jackson.env.JsonPropertySourceLoader;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Vinicius Carvalho
 * @since 3.4.0
 *
 * Distributed configuration client implementation that fetches application configuration files from Google Cloud Secret Manager.
 */
@Singleton
@BootstrapContextCompatible
@Requires(property = ConfigurationClient.ENABLED, value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
public class SecretManagerConfigurationClient implements ConfigurationClient {

    private static final String CAMEL_CASE_REGEX = "([a-z])([A-Z]+)";
    private static final String CAMEL_CASE_REPLACE = "$1_$2";
    private static final String DESCRIPTION = "GCP Secret Manager Config Client";
    private static final String PROPERTY_SOURCE_SUFFIX = " (GCP SecretManager)";
    private static final List<PropertySourceReader> READERS = Arrays.asList(
            new YamlPropertySourceLoader(),
            new PropertiesPropertySourceLoader(),
            new JsonPropertySourceLoader());
    private final SecretManagerClient secretManagerClient;
    private final SecretManagerConfigurationProperties configurationProperties;
    private final Logger logger = LoggerFactory.getLogger(SecretManagerConfigurationClient.class);

    public SecretManagerConfigurationClient(SecretManagerClient secretManagerClient, SecretManagerConfigurationProperties configurationProperties) {
        this.secretManagerClient = secretManagerClient;
        this.configurationProperties = configurationProperties;
    }

    @Override
    public Publisher<PropertySource> getPropertySources(Environment environment) {
        return Flowable.concat(resolveEnvironmentSecrets(environment), resolveSecretKeys());
    }

    private Publisher<PropertySource> resolveEnvironmentSecrets(Environment environment) {
        return Flowable.fromIterable(configCandidates(environment))
                .map(s -> secretManagerClient.getSecret(s))
                .flatMap(Maybe::toFlowable)
                .filter(Objects::nonNull)
                .map(this::fromSecret);
    }

    /**
     * Resolve keys from Secret Manager into a single "secret-manager-keys" PropertySource. Keys are all converted to snake case prior to insertion to allow the following mapping to happen:
     *
     * DB_PASSWORD -> db.password
     * dbPassword -> (DB_PASSWORD) -> db.password
     * @return
     */
    private Publisher<PropertySource> resolveSecretKeys() {
        return Flowable.fromIterable(configurationProperties.getKeys())
                .map(k -> secretManagerClient.getSecret(k))
                .flatMap(Maybe::toFlowable)
                .filter(Objects::nonNull)
                .toMap(versionedSecret -> "sm." + versionedSecret.getName().replaceAll(CAMEL_CASE_REGEX, CAMEL_CASE_REPLACE).toUpperCase(),
                        versionedSecret -> (Object) new String(versionedSecret.getContents(), StandardCharsets.UTF_8).replaceAll("\\n", "").trim())
                .map(m -> PropertySource.of("secret-manager-keys", m, PropertySource.PropertyConvention.ENVIRONMENT_VARIABLE))
                .toFlowable();
    }

    /**
     * @param environment
     * @return a collection of all possible combinations of files to be queried. For each active environment.
     */
    private Set<String> configCandidates(Environment environment) {
        Optional<String> applicationName = environment.getProperty("micronaut.application.name", String.class);
        Set<String> activeEnv = environment.getActiveNames();
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add("application");
        if (applicationName.isPresent()) {
            candidates.add(applicationName.get());
        }
        for (String e : activeEnv) {
            candidates.add("application_" + e);
            if (applicationName.isPresent()) {
                candidates.add(applicationName.get() + "_" + e);
            }
        }
        candidates.addAll(configurationProperties.getCustomConfigs());
        return candidates;
    }

    /**
     * GCP Secret Manager unfortunately lacks support of file extensions. There's a new feature being tested
     * to allow {@link com.google.cloud.secretmanager.v1.AccessSecretVersionResponse} to contain labels, and
     * we could use a `Content-Type` label to define the type of file.
     * So, the code loops through the provided readers, and the first that can read the file (when a wrong file type is read an exception is swallowed )
     * returns a Property Source based on the Map of the parsed file.
     * @param secret
     * @return Mapped PropertySource
     */
    private PropertySource fromSecret(VersionedSecret secret) {
        Map<String, Object> data = new HashMap<>();
        int priority = EnvironmentPropertySource.POSITION + 100;
        for (PropertySourceReader reader : READERS) {
            try {
                data.putAll(reader.read(secret.getName(), secret.getContents()));
                if (!data.isEmpty()) {
                    break;
                }
            } catch (Exception e) {
            }
        }
        return PropertySource.of(secret.getName() + PROPERTY_SOURCE_SUFFIX, data, priority);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}
