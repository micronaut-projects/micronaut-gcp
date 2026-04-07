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
package io.micronaut.gcp.secretmanager.imports;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.auth.http.HttpTransportFactory;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ConnectionString;
import io.micronaut.core.util.StringUtils;
import io.micronaut.discovery.config.RetryablePropertySourceImporter;
import io.micronaut.gcp.credentials.GoogleCredentialsConfiguration;
import io.micronaut.gcp.secretmanager.SecretManagerFactory;
import io.micronaut.gcp.secretmanager.client.DefaultSecretManagerClient;
import io.micronaut.gcp.secretmanager.client.SecretManagerClient;
import io.micronaut.gcp.secretmanager.client.VersionedSecret;
import io.micronaut.gcp.secretmanager.configuration.SecretManagerConfigurationProperties;
import io.micronaut.retry.RetryPolicy;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

/**
 * Property source importer for Google Secret Manager.
 *
 * @since 5.0
 */
@Internal
public final class SecretManagerPropertySourceImporter extends RetryablePropertySourceImporter<SecretManagerImportDeclaration> {

    public static final String PROVIDER = "gcp-secret-manager";
    private static final String PATH = "path";
    private static final String FORMAT = "format";
    private static final String CREDENTIALS_LOCATION = "credentials-location";
    private static final String ENCODED_KEY = "encoded-key";
    private static final String PROJECT_ID = "project-id";
    private static final String LOCATION = "location";
    private static final SecretManagerImporterClientFactory CLIENT_FACTORY = new SecretManagerImporterClientFactory();

    @Override
    public String getProvider() {
        return PROVIDER;
    }

    @Override
    protected SecretManagerImportDeclaration newImportDeclaration(ConnectionString connectionString, RetryPolicy retryPolicy) {
        String rawPath = connectionString.getPath();
        if (StringUtils.isEmpty(rawPath) && !connectionString.getHosts().isEmpty()) {
            // For URIs like gcp-secret-manager://application, ConnectionString parses the
            // secret name as the first host. Fall back to that when the path is empty.
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
        String normalizedCredentialsLocation = StringUtils.isNotEmpty(credentialsLocation) ? credentialsLocation : null;
        String normalizedEncodedKey = StringUtils.isNotEmpty(encodedKey) ? encodedKey : null;
        validateCredentials(normalizedCredentialsLocation, normalizedEncodedKey);
        validateProjectId(StringUtils.isNotEmpty(projectId) ? projectId : null);
        return new SecretManagerImportDeclaration(
            path,
            connectionString.isOptional(),
            StringUtils.isNotEmpty(format) ? format : null,
            normalizedCredentialsLocation,
            normalizedEncodedKey,
            StringUtils.isNotEmpty(projectId) ? projectId : null,
            StringUtils.isNotEmpty(location) ? location : null
        );
    }

    @Override
    protected SecretManagerImportDeclaration newImportDeclaration(ConvertibleValues<Object> values, RetryPolicy retryPolicy) {
        String path = validatePath(values.get(PATH, String.class).orElse(null));
        boolean optional = values.get("optional", Boolean.class).orElse(false);
        String format = values.get(FORMAT, String.class).orElse(null);
        String credentialsLocation = values.get(CREDENTIALS_LOCATION, String.class).orElse(null);
        String encodedKey = values.get(ENCODED_KEY, String.class).orElse(null);
        String projectId = values.get(PROJECT_ID, String.class).orElse(null);
        String location = values.get(LOCATION, String.class).orElse(null);
        String normalizedCredentialsLocation = StringUtils.isNotEmpty(credentialsLocation) ? credentialsLocation : null;
        String normalizedEncodedKey = StringUtils.isNotEmpty(encodedKey) ? encodedKey : null;
        validateCredentials(normalizedCredentialsLocation, normalizedEncodedKey);
        validateProjectId(StringUtils.isNotEmpty(projectId) ? projectId : null);
        return new SecretManagerImportDeclaration(
            path,
            optional,
            StringUtils.isNotEmpty(format) ? format : null,
            normalizedCredentialsLocation,
            normalizedEncodedKey,
            StringUtils.isNotEmpty(projectId) ? projectId : null,
            StringUtils.isNotEmpty(location) ? location : null
        );
    }

    @Override
    protected Optional<PropertySource> importRetryablePropertySource(ImportContext<SecretManagerImportDeclaration> context) {
        ArgumentUtils.requireNonNull("context", context);
        SecretManagerImportDeclaration declaration = context.importDeclaration();
        SecretManagerConfigurationProperties configurationProperties = new SecretManagerConfigurationProperties();
        configurationProperties.setLocation(declaration.location());
        VersionedSecret secret = fetchSecret(context, declaration, configurationProperties);
        if (secret == null) {
            return Optional.empty();
        }
        String sourceName = context.connectionString() != null ? context.getCanonicalLocation() : PROVIDER + ":" + declaration.path();
        String extension = StringUtils.isNotEmpty(declaration.format()) ? declaration.format() : inferExtension(secret.getName());
        return context.importPropertySource(new String(secret.getContents(), java.nio.charset.StandardCharsets.UTF_8), sourceName, extension, context.parentOrigin());
    }

    private VersionedSecret fetchSecret(ImportContext<SecretManagerImportDeclaration> context,
                                        SecretManagerImportDeclaration declaration,
                                        SecretManagerConfigurationProperties configurationProperties) {
        try {
            GoogleCredentials credentials = buildGoogleCredentials(declaration);
            SecretManagerFactory factory = new SecretManagerFactory(configurationProperties);
            CredentialsProvider credentialsProvider = factory.credentialsProvider(credentials);
            TransportChannelProvider transportChannelProvider = factory.transportChannelProvider();
            try (SecretManagerServiceClient client = CLIENT_FACTORY.create(configurationProperties, credentialsProvider, transportChannelProvider)) {
                SecretManagerClient secretManagerClient = new DefaultSecretManagerClient(
                    client,
                    googleCloudConfiguration(declaration),
                    (ExecutorService) null,
                    configurationProperties
                );
                return Mono.from(secretManagerClient.getSecret(declaration.path())).block();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not instantiate SecretManagerServiceClient for config import", e);
        }
    }

    private GoogleCredentials buildGoogleCredentials(SecretManagerImportDeclaration declaration) throws IOException {
        GoogleCredentialsConfiguration credentialsConfiguration = new GoogleCredentialsConfiguration();
        credentialsConfiguration.setLocation(declaration.credentialsLocation());
        credentialsConfiguration.setEncodedKey(declaration.encodedKey());
        HttpTransportFactory transportFactory = NetHttpTransport::new;
        GoogleCredentials credentials;
        if (credentialsConfiguration.getLocation().isPresent()) {
            try (java.io.FileInputStream fis = new java.io.FileInputStream(credentialsConfiguration.getLocation().get())) {
                credentials = GoogleCredentials.fromStream(fis, transportFactory);
            }
        } else if (credentialsConfiguration.getEncodedKey().isPresent()) {
            byte[] bytes = java.util.Base64.getDecoder().decode(credentialsConfiguration.getEncodedKey().get());
            try (java.io.ByteArrayInputStream is = new java.io.ByteArrayInputStream(bytes)) {
                credentials = GoogleCredentials.fromStream(is, transportFactory);
            }
        } else {
            credentials = GoogleCredentials.getApplicationDefault(transportFactory);
        }
        List<String> scopes = credentialsConfiguration.getScopes().isEmpty()
            ? List.of("https://www.googleapis.com/auth/cloud-platform")
            : credentialsConfiguration.getScopes().stream().map(URI::toString).toList();
        if (credentials.createScopedRequired()) {
            return credentials.createScoped(scopes);
        }
        return credentials;
    }

    private String inferExtension(String path) {
        int index = path.lastIndexOf('.');
        if (index > -1 && index < path.length() - 1) {
            return path.substring(index + 1);
        }
        return "yml";
    }

    private io.micronaut.gcp.GoogleCloudConfiguration googleCloudConfiguration(SecretManagerImportDeclaration declaration) {
        io.micronaut.gcp.GoogleCloudConfiguration configuration = new io.micronaut.gcp.GoogleCloudConfiguration();
        configuration.setProjectId(declaration.projectId());
        return configuration;
    }

    private static String validatePath(String path) {
        if (!StringUtils.hasText(path)) {
            throw new IllegalArgumentException("Google Secret Manager config import path cannot be blank");
        }
        return path;
    }

    private static void validateCredentials(@Nullable String credentialsLocation, @Nullable String encodedKey) {
        if (credentialsLocation != null && encodedKey != null) {
            throw new IllegalArgumentException("Please specify only one of credentials-location or encoded-key for gcp-secret-manager imports");
        }
    }

    private static void validateProjectId(@Nullable String projectId) {
        if (!StringUtils.hasText(projectId)) {
            throw new IllegalArgumentException("Google Secret Manager imports require project-id to be specified");
        }
    }
}
