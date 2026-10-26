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
package io.micronaut.gcp.parametermanager;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.EnvironmentPropertySource;
import io.micronaut.context.env.PropertySource;
import io.micronaut.context.env.PropertySourceReader;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.util.StringUtils;
import io.micronaut.discovery.config.ConfigurationClient;
import io.micronaut.gcp.parametermanager.client.ParameterManagerAccessClient;
import io.micronaut.gcp.parametermanager.client.VersionedParameter;
import io.micronaut.gcp.parametermanager.configuration.ParameterManagerConfigurationProperties;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Distributed configuration client implementation that fetches application configuration files
 * from Google Cloud Parameter Manager.
 *
 * @author Dhaval Bhensdadiya
 * @since 6.0.0
 */
@Singleton
@BootstrapContextCompatible
@Requires(property = ConfigurationClient.ENABLED, value = StringUtils.TRUE,
    defaultValue = StringUtils.FALSE)
public class ParameterManagerConfigurationClient implements ConfigurationClient {

    private static final String CAMEL_CASE_REGEX = "([a-z])([A-Z]+)";
    private static final String CAMEL_CASE_REPLACE = "$1_$2";
    private static final String DESCRIPTION = "GCP Parameter Manager Config Client";
    private static final String LATEST = "latest";
    private static final String PROPERTY_SOURCE_SUFFIX = " (GCP ParameterManager)";
    private final ParameterManagerAccessClient parameterManagerAccessClient;
    private final ParameterManagerConfigurationProperties parameterManagerConfigurationProperties;

    /**
     * Constructor for the {@link ParameterManagerConfigurationClient}.
     *
     * @param parameterManagerAccessClient            - The wrapper client
     *                                                {@link ParameterManagerAccessClient} to
     *                                                communicate with GCP Parameter Manager.
     * @param parameterManagerConfigurationProperties - The Configuration for Parameter Manager
     *                                                client.
     */
    public ParameterManagerConfigurationClient(
        ParameterManagerAccessClient parameterManagerAccessClient,
        ParameterManagerConfigurationProperties parameterManagerConfigurationProperties) {
        this.parameterManagerAccessClient = parameterManagerAccessClient;
        this.parameterManagerConfigurationProperties = parameterManagerConfigurationProperties;
    }

    /**
     * Returns a description of this configuration client.
     *
     * @return description string.
     */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * Resolves property sources for a given environment asynchronously.
     *
     * @param environment - The Micronaut environment.
     * @return list of resolved property sources.
     */
    @Override
    public Publisher<PropertySource> getPropertySources(Environment environment) {
        List<PropertySourceReader> readers = List.copyOf(environment.getPropertySourceLoaders());
        return Flux.concat(resolveParameterConfigs(readers), resolveParameterKeys());
    }

    /**
     * Resolves configurations from Parameter Manager into a reactive stream of
     * {@link PropertySource}.
     *
     * @return Flux of property sources.
     */
    private Publisher<PropertySource> resolveParameterConfigs(List<PropertySourceReader> readers) {
        return Flux.fromIterable(configCandidates().entrySet()).flatMap(env -> {
            ParsedParameter parsedParameter = parseNameAndVersion(env.getValue());
            return Mono.from(parameterManagerAccessClient.getRenderedParameter(parsedParameter.name,
                    parsedParameter.version))
                .mapNotNull(parameter -> fromParameter(parameter, env.getKey(), readers));
        });
    }

    /**
     * Resolves the keys from Parameter Manager into a single "parameter-manager-keys"
     * PropertySource.
     * Keys are all converted to snake case prior to insertion to allow the following mapping to
     * happen:
     * DB_PASSWORD -> db.password
     * dbPassword -> (DB_PASSWORD) -> db.password
     *
     * @return Flux of property sources.
     */
    private Publisher<PropertySource> resolveParameterKeys() {
        return Flux.fromIterable(parameterManagerConfigurationProperties.getKeys())
            .flatMap(parameter -> {
                ParsedParameter parsedParameter = parseNameAndVersion(parameter);
                return parameterManagerAccessClient.getRenderedParameter(parsedParameter.name,
                    parsedParameter.version);
            }).filter(Objects::nonNull).collectMap(versionedParameter -> keyPropertyName(versionedParameter.getName()),
                versionedParameter -> (Object) keyPropertyValue(versionedParameter)).map(
                m -> PropertySource.of("parameter-manager-keys", m,
                    PropertySource.PropertyConvention.ENVIRONMENT_VARIABLE,
                    PropertySource.Origin.of("GCP Parameter Manager")));
    }

    /**
     * Converts a parameter name into the property name used for a key: {@code DB_PASSWORD} and
     * {@code dbPassword} both become {@code pm.DB_PASSWORD}.
     *
     * @param parameterName - The Parameter Manager parameter name.
     * @return The property name.
     */
    static String keyPropertyName(String parameterName) {
        return "pm." + parameterName.replaceAll(CAMEL_CASE_REGEX, CAMEL_CASE_REPLACE).toUpperCase();
    }

    /**
     * Returns the value of a key exactly as stored. Newlines and surrounding whitespace are kept.
     *
     * @param parameter - The rendered parameter.
     * @return The decoded value.
     */
    static String keyPropertyValue(VersionedParameter parameter) {
        return new String(parameter.getContents(), StandardCharsets.UTF_8);
    }

    /**
     * Gather custom configurations stored in the Parameter Manager.
     *
     * @return a map of all possible candidate configurations.
     */
    private Map<Integer, String> configCandidates() {
        Map<Integer, String> candidates = new HashMap<>();
        int priority = EnvironmentPropertySource.POSITION + 150;

        for (String name : parameterManagerConfigurationProperties.getCustomConfigs()) {
            candidates.put(++priority, name);
        }
        return candidates;
    }

    /**
     * Converts a {@link VersionedParameter} into a Micronaut {@link PropertySource}.
     * This method loops through the provided readers, and the first that can read the file (when
     * a wrong file type is read an exception is swallowed) returns a Property Source based on
     * the Map of the parsed file.
     *
     * @param parameter - The {@link VersionedParameter} fetched from GCP Parameter Manager to be
     *                  parsed.
     * @param priority  - The priority to assign to the resulting {@link PropertySource}
     * @param readers   - The readers captured from the environment of the load that requested
     *                  this parameter.
     * @return Mapped PropertySource.
     */
    private PropertySource fromParameter(VersionedParameter parameter, int priority,
                                         List<PropertySourceReader> readers) {
        Map<String, Object> data = new HashMap<>();

        for (PropertySourceReader reader : readers) {
            try {
                data.putAll(reader.read(parameter.getName(), parameter.getContents()));
                if (!data.isEmpty()) {
                    break;
                }
            } catch (Exception e) {
                // Sileting ignoring failure to parse non compatible formats while iterating through readers 
            }
        }
        return PropertySource.of(parameter.getName() + PROPERTY_SOURCE_SUFFIX, data, priority);
    }

    /**
     * Parses a parameter string in the form "parameter_name[/parameter_version]" into a
     * {@link ParsedParameter} object. When no version is given, {@code latest} is used.
     * Examples:
     * "my-param/1"    -> name="my-param", version="1"
     * "my-param/ver1" -> name="my-param", version="ver1"
     * "my-param"      -> name="my-param", version="latest"
     *
     * @param raw - the raw string for the parameter name, optionally containing a version.
     * @return a {@link ParsedParameter} object containing name and version
     * @throws ConfigurationException if the input is invalid or empty
     */
    static ParsedParameter parseNameAndVersion(String raw) {
        String trimmed = raw.trim();
        if (trimmed.isEmpty()) {
            throw new ConfigurationException("Parameter reference must not be empty");
        }

        int idx = trimmed.lastIndexOf('/');

        if (idx < 0) {
            return new ParsedParameter(trimmed, LATEST);
        }

        String name = trimmed.substring(0, idx);
        String version = trimmed.substring(idx + 1);

        if (name.isBlank()) {
            throw new ConfigurationException("Parameter name must not be empty: " + raw);
        }
        if (name.contains("/")) {
            throw new ConfigurationException("Parameter name must not contain '/': " + raw);
        }

        if (version.isBlank()) {
            throw new ConfigurationException("Parameter version must not be empty: " + raw);
        }

        return new ParsedParameter(name, version);
    }

    /**
     * Record class to hold the parsed parameter name and version.
     *
     * @param name    - The name of the Parameter.
     * @param version - The version of the Parameter.
     */
    record ParsedParameter(String name, String version) {
    }
}
