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

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.EnvironmentPropertySource;
import io.micronaut.context.env.PropertySource;
import io.micronaut.context.env.PropertySourceLoader;
import io.micronaut.context.env.PropertySourceReader;
import io.micronaut.core.util.StringUtils;
import io.micronaut.discovery.config.ConfigurationClient;
import io.micronaut.gcp.secretmanager.client.SecretManagerClient;
import io.micronaut.gcp.secretmanager.client.VersionedSecret;
import io.micronaut.gcp.secretmanager.configuration.SecretManagerConfigurationProperties;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    private static final List<PropertySourceLoader> READERS = ServiceLoader.load(PropertySourceLoader.class)
            .stream().map(ServiceLoader.Provider::get).toList();
    private final SecretManagerClient secretManagerClient;
    private final SecretManagerConfigurationProperties configurationProperties;

    public SecretManagerConfigurationClient(SecretManagerClient secretManagerClient, SecretManagerConfigurationProperties configurationProperties) {
        this.secretManagerClient = secretManagerClient;
        this.configurationProperties = configurationProperties;
    }

    @Override
    public Publisher<PropertySource> getPropertySources(Environment environment) {
        return Flux.concat(resolveEnvironmentSecrets(environment), resolveSecretKeys());
    }

    private Publisher<PropertySource> resolveEnvironmentSecrets(Environment environment) {

        return Flux.fromIterable(configCandidates(environment).entrySet())
                .flatMap(env ->
                        Mono.from(secretManagerClient.getSecret(env.getValue()))
                                .mapNotNull(secret -> fromSecret(secret, env.getKey()))
                );
    }

    /**
     * Resolve keys from Secret Manager into a single "secret-manager-keys" PropertySource. Keys are all converted to snake case prior to insertion to allow the following mapping to happen:
     *
     * DB_PASSWORD -> db.password
     * dbPassword -> (DB_PASSWORD) -> db.password
     * @return
     */
    private Publisher<PropertySource> resolveSecretKeys() {
        return Flux.fromIterable(configurationProperties.getKeys())
                .flatMap(secretManagerClient::getSecret)
                .filter(Objects::nonNull)
                .collectMap(versionedSecret -> "sm." + versionedSecret.getName().replaceAll(CAMEL_CASE_REGEX, CAMEL_CASE_REPLACE).toUpperCase(),
                        versionedSecret -> (Object) new String(versionedSecret.getContents(), StandardCharsets.UTF_8).replaceAll("\\n", "").trim())
                .map(m -> PropertySource.of("secret-manager-keys", m, PropertySource.PropertyConvention.ENVIRONMENT_VARIABLE));
    }

    /**
     * Gather application and project named configurations by environments and custom configurations.
     * @param environment Active application environment
     * @return a map of all possible combinations of files with their position to be queried. For each active environment.
     */
    private Map<Integer, String> configCandidates(Environment environment) {
        Map<Integer, String> candidates = new HashMap<>();
        int priority = EnvironmentPropertySource.POSITION + 150;

        if (configurationProperties.isDefaultConfigEnabled()) {
            Optional<String> applicationName = environment.getProperty("micronaut.application.name", String.class);
            candidates.put(EnvironmentPropertySource.POSITION + 101, "application");
            applicationName.ifPresent(s -> candidates.put(EnvironmentPropertySource.POSITION + 102, s));

            for (String e : environment.getActiveNames()) {
                candidates.put(++priority, "application_" + e);
                if (applicationName.isPresent()) {
                    candidates.put(++priority, applicationName.get() + "_" + e);
                }
            }
        }

        for (String name: configurationProperties.getCustomConfigs()) {
            //NOTE: User defined configuration have higher priority than environments
            // with the last one having the highest priority.
            candidates.put(++priority, name);
        }
        return candidates;
    }

    /**
     * GCP Secret Manager unfortunately lacks support of file extensions. There's a new feature being tested
     * to allow {@link com.google.cloud.secretmanager.v1.AccessSecretVersionResponse} to contain labels, and
     * we could use a `Content-Type` label to define the type of file.
     * So, the code loops through the provided readers, and the first that can read the file (when a wrong file type is read an exception is swallowed )
     * returns a Property Source based on the Map of the parsed file.
     * @param secret Configuration to be parsed
     * @param priority Property Source position
     * @return Mapped PropertySource
     */
    private PropertySource fromSecret(VersionedSecret secret, int priority) {
        Map<String, Object> data = new HashMap<>();

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
