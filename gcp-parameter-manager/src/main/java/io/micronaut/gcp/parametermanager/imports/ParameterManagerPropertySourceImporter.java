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
package io.micronaut.gcp.parametermanager.imports;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.auth.http.HttpTransportFactory;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import io.micronaut.context.env.PropertySource;
import io.micronaut.context.env.PropertySource.Origin;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ConnectionString;
import io.micronaut.core.util.StringUtils;
import io.micronaut.discovery.config.RetryablePropertySourceImporter;
import io.micronaut.gcp.credentials.GoogleCredentialsConfiguration;
import io.micronaut.gcp.parametermanager.ParameterManagerFactory;
import io.micronaut.gcp.parametermanager.client.VersionedParameter;
import io.micronaut.gcp.parametermanager.configuration.ParameterManagerConfigurationProperties;
import io.micronaut.retry.RetryPolicy;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

/**
 * Property source importer for Google Cloud Parameter Manager.
 *
 * @since 6.1.0
 */
@Internal
public final class ParameterManagerPropertySourceImporter extends RetryablePropertySourceImporter<ParameterManagerImportDeclaration> {

    public static final String PROVIDER = "gcp-parameter-manager";
    private static final Logger LOG = LoggerFactory.getLogger(ParameterManagerPropertySourceImporter.class);
    private static final String DEFAULT_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
    private static final String PATH = "path";
    private static final String FORMAT = "format";
    private static final String CREDENTIALS_LOCATION = "credentials-location";
    private static final String ENCODED_KEY = "encoded-key";
    private static final String PROJECT_ID = "project-id";
    private static final String LOCATION = "location";
    private static final String VERSION = "version";
    private static final String GCP_CREDENTIALS_LOCATION = "gcp.credentials.location";
    private static final String GCP_CREDENTIALS_ENCODED_KEY = "gcp.credentials.encoded-key";
    private static final String GCP_PROJECT_ID = "gcp.project-id";
    private static final String GCP_PROJECT_ID_CAMEL_CASE = "gcp.projectId";
    private static final ParameterManagerImporterClientFactory CLIENT_FACTORY = new ParameterManagerImporterClientFactory();

    @Override
    public String getProvider() {
        return PROVIDER;
    }

    @Override
    protected ParameterManagerImportDeclaration newImportDeclaration(ConnectionString connectionString, RetryPolicy retryPolicy) {
        String rawPath = connectionString.getPath();
        if (StringUtils.isEmpty(rawPath) && !connectionString.getHosts().isEmpty()) {
            // For URIs like gcp-parameter-manager://application, ConnectionString parses the
            // parameter name as the first host. Fall back to that when the path is empty.
            rawPath = connectionString.getHosts().get(0).host();
        }
        if (rawPath != null && rawPath.startsWith("/")) {
            rawPath = rawPath.substring(1);
        }
        String path = validatePath(rawPath);
        String format = connectionString.getOptions().get(FORMAT);
        String encodedKey = connectionString.getPassword().orElse(connectionString.getOptions().get(ENCODED_KEY));
        String credentialsLocation = connectionString.getUsername().orElse(connectionString.getOptions().get(CREDENTIALS_LOCATION));
        String projectId = connectionString.getOptions().get(PROJECT_ID);
        String location = connectionString.getOptions().get(LOCATION);
        String version = validateVersion(connectionString.getOptions().get(VERSION));
        String normalizedCredentialsLocation = StringUtils.isNotEmpty(credentialsLocation) ? credentialsLocation : null;
        String normalizedEncodedKey = StringUtils.isNotEmpty(encodedKey) ? encodedKey : null;
        validateCredentials(normalizedCredentialsLocation, normalizedEncodedKey);
        return new ParameterManagerImportDeclaration(
            path,
            connectionString.isOptional(),
            StringUtils.isNotEmpty(format) ? format : null,
            normalizedCredentialsLocation,
            normalizedEncodedKey,
            StringUtils.isNotEmpty(projectId) ? projectId : null,
            StringUtils.isNotEmpty(location) ? location : null,
            version
        );
    }

    @Override
    protected ParameterManagerImportDeclaration newImportDeclaration(ConvertibleValues<Object> values, RetryPolicy retryPolicy) {
        String path = validatePath(values.get(PATH, String.class).orElse(null));
        boolean optional = values.get("optional", Boolean.class).orElse(false);
        String format = values.get(FORMAT, String.class).orElse(null);
        String credentialsLocation = values.get(CREDENTIALS_LOCATION, String.class).orElse(null);
        String encodedKey = values.get(ENCODED_KEY, String.class).orElse(null);
        String projectId = values.get(PROJECT_ID, String.class).orElse(null);
        String location = values.get(LOCATION, String.class).orElse(null);
        String version = validateVersion(values.get(VERSION, String.class).orElse(null));
        String normalizedCredentialsLocation = StringUtils.isNotEmpty(credentialsLocation) ? credentialsLocation : null;
        String normalizedEncodedKey = StringUtils.isNotEmpty(encodedKey) ? encodedKey : null;
        validateCredentials(normalizedCredentialsLocation, normalizedEncodedKey);
        return new ParameterManagerImportDeclaration(
            path,
            optional,
            StringUtils.isNotEmpty(format) ? format : null,
            normalizedCredentialsLocation,
            normalizedEncodedKey,
            StringUtils.isNotEmpty(projectId) ? projectId : null,
            StringUtils.isNotEmpty(location) ? location : null,
            version
        );
    }

    @Override
    protected Optional<PropertySource> importRetryablePropertySource(ImportContext<ParameterManagerImportDeclaration> context) {
        ArgumentUtils.requireNonNull("context", context);
        ParameterManagerImportDeclaration declaration = resolveDeclaration(context, context.importDeclaration());
        ParameterManagerConfigurationProperties configurationProperties = new ParameterManagerConfigurationProperties();
        configurationProperties.setLocation(declaration.location());
        VersionedParameter parameter = fetchParameter(declaration, configurationProperties);
        String sourceName = context.connectionString() != null ? context.getCanonicalLocation() : PROVIDER + ":" + declaration.path();
        String extension = StringUtils.isNotEmpty(declaration.format()) ? declaration.format() : inferExtension(parameter.getName());
        String content = new String(parameter.getContents(), StandardCharsets.UTF_8);
        Origin origin = context.parentOrigin();
        return context.importPropertySource(content, sourceName, extension, origin != null ? origin : Origin.of(sourceName));
    }

    private VersionedParameter fetchParameter(ParameterManagerImportDeclaration declaration,
                                              ParameterManagerConfigurationProperties configurationProperties) {
        try {
            GoogleCredentials credentials = buildGoogleCredentials(declaration);
            ParameterManagerFactory factory = new ParameterManagerFactory(configurationProperties);
            CredentialsProvider credentialsProvider = factory.credentialsProvider(credentials);
            TransportChannelProvider transportChannelProvider = factory.transportChannelProvider();
            try (ParameterManagerClient client = CLIENT_FACTORY.create(configurationProperties, credentialsProvider, transportChannelProvider)) {
                String projectId = declaration.projectId();
                return ParameterManagerParameterAccessor.renderParameter(client, projectId, declaration.path(), declaration.version(), configurationProperties);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not instantiate ParameterManagerClient for config import", e);
        }
    }

    private GoogleCredentials buildGoogleCredentials(ParameterManagerImportDeclaration declaration) throws IOException {
        GoogleCredentialsConfiguration credentialsConfiguration = new GoogleCredentialsConfiguration();
        credentialsConfiguration.setLocation(declaration.credentialsLocation());
        credentialsConfiguration.setEncodedKey(declaration.encodedKey());
        HttpTransportFactory transportFactory = NetHttpTransport::new;
        GoogleCredentials credentials;
        Optional<String> credentialsLocation = credentialsConfiguration.getLocation();
        Optional<String> encodedKey = credentialsConfiguration.getEncodedKey();
        if (credentialsLocation.isPresent()) {
            try (FileInputStream fis = new FileInputStream(credentialsLocation.orElseThrow())) {
                credentials = GoogleCredentials.fromStream(fis, transportFactory);
            }
        } else if (encodedKey.isPresent()) {
            byte[] bytes = Base64.getDecoder().decode(encodedKey.orElseThrow());
            try (ByteArrayInputStream is = new ByteArrayInputStream(bytes)) {
                credentials = GoogleCredentials.fromStream(is, transportFactory);
            }
        } else {
            credentials = GoogleCredentials.getApplicationDefault();
        }
        List<String> scopes = credentialsConfiguration.getScopes().isEmpty()
            ? List.of(DEFAULT_SCOPE)
            : credentialsConfiguration.getScopes().stream().map(URI::toString).toList();
        if (credentials.createScopedRequired()) {
            return credentials.createScoped(scopes);
        }
        return credentials;
    }

    private ParameterManagerImportDeclaration resolveDeclaration(ImportContext<ParameterManagerImportDeclaration> context,
                                                                  ParameterManagerImportDeclaration declaration) {
        String credentialsLocation = declaration.credentialsLocation();
        String encodedKey = declaration.encodedKey();
        String projectId = declaration.projectId();
        if (!StringUtils.hasText(credentialsLocation) && context.environment().containsProperty(GCP_CREDENTIALS_LOCATION)) {
            credentialsLocation = context.environment().getRequiredProperty(GCP_CREDENTIALS_LOCATION, String.class);
        }
        if (!StringUtils.hasText(encodedKey) && context.environment().containsProperty(GCP_CREDENTIALS_ENCODED_KEY)) {
            encodedKey = context.environment().getRequiredProperty(GCP_CREDENTIALS_ENCODED_KEY, String.class);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolving gcp-parameter-manager import for path [{}] with visible bootstrap keys gcp.project-id={}, gcp.projectId={}, gcp.credentials.location={}, gcp.credentials.encoded-key={}",
                declaration.path(),
                context.environment().containsProperty(GCP_PROJECT_ID),
                context.environment().containsProperty(GCP_PROJECT_ID_CAMEL_CASE),
                context.environment().containsProperty(GCP_CREDENTIALS_LOCATION),
                context.environment().containsProperty(GCP_CREDENTIALS_ENCODED_KEY));
        }
        if (!StringUtils.hasText(projectId)) {
            if (context.environment().containsProperty(GCP_PROJECT_ID)) {
                projectId = context.environment().getRequiredProperty(GCP_PROJECT_ID, String.class);
            } else if (context.environment().containsProperty(GCP_PROJECT_ID_CAMEL_CASE)) {
                projectId = context.environment().getRequiredProperty(GCP_PROJECT_ID_CAMEL_CASE, String.class);
            }
        }
        validateCredentials(StringUtils.hasText(credentialsLocation) ? credentialsLocation : null, StringUtils.hasText(encodedKey) ? encodedKey : null);
        validateProjectId(StringUtils.hasText(projectId) ? projectId : null);
        return new ParameterManagerImportDeclaration(
            declaration.path(),
            declaration.optional(),
            declaration.format(),
            StringUtils.hasText(credentialsLocation) ? credentialsLocation : null,
            StringUtils.hasText(encodedKey) ? encodedKey : null,
            StringUtils.hasText(projectId) ? projectId : null,
            declaration.location(),
            declaration.version()
        );
    }

    private String inferExtension(String path) {
        int index = path.lastIndexOf('.');
        if (index > -1 && index < path.length() - 1) {
            return path.substring(index + 1);
        }
        return "yml";
    }

    private static String validatePath(String path) {
        if (!StringUtils.hasText(path)) {
            throw new IllegalArgumentException("Google Cloud Parameter Manager config import path cannot be blank");
        }
        return path;
    }

    private static String validateVersion(@Nullable String version) {
        if (!StringUtils.hasText(version)) {
            throw new IllegalArgumentException("Google Cloud Parameter Manager imports require version to be specified; Parameter Manager does not support 'latest' as a version");
        }
        return version;
    }

    private static void validateCredentials(@Nullable String credentialsLocation, @Nullable String encodedKey) {
        if (credentialsLocation != null && encodedKey != null) {
            throw new IllegalArgumentException("Please specify only one of credentials-location or encoded-key for gcp-parameter-manager imports");
        }
    }

    private static void validateProjectId(@Nullable String projectId) {
        if (!StringUtils.hasText(projectId)) {
            throw new IllegalArgumentException("Google Cloud Parameter Manager imports require project-id to be specified");
        }
    }
}
