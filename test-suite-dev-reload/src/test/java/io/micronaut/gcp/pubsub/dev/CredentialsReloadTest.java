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
package io.micronaut.gcp.pubsub.dev;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.auth.oauth2.GoogleCredentials;
import io.micronaut.context.ApplicationContext;
import io.micronaut.dev.tck.ReloadHarness;
import io.micronaut.dev.tck.ReloadTck;
import io.micronaut.gcp.Modules;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Restarts an application that targets Pub/Sub itself, rather than the emulator, with the credentials of a service
 * account, through the development runtime. Development mode keeps the credentials, with the access token they cache,
 * the credentials provider and the transport channel provider across the restart, until a change under
 * {@code gcp.credentials} releases them. Nothing connects to Google Cloud: the beans are only created.
 */
class CredentialsReloadTest {

    private static final String SERVICE = """
        package example;

        import jakarta.inject.Singleton;

        @Singleton
        public class Service {
            public String name() {
                return "%s";
            }
        }
        """;

    @TempDir
    Path project;

    @Test
    void aRestartKeepsTheCredentialsUntilTheirConfigurationChanges() throws Exception {
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            Map<String, String> configuration = configuration();
            configuration.forEach(harness::property);
            harness.source("example.Service", SERVICE.formatted("first"));
            harness.start();
            ApplicationContext first = harness.context();
            GoogleCredentials credentials = first.getBean(GoogleCredentials.class);
            CredentialsProvider credentialsProvider = first.getBean(CredentialsProvider.class, Qualifiers.byName(Modules.PUBSUB));
            TransportChannelProvider channelProvider = first.getBean(TransportChannelProvider.class, Qualifiers.byName(Modules.PUBSUB));
            assertSame(credentials, credentialsProvider.getCredentials());
            first = null;

            harness.source("example.Service", SERVICE.formatted("second"));
            harness.reload();
            assertEquals(2, harness.generation());
            for (Object retained : List.of(credentials, credentialsProvider, channelProvider)) {
                ReloadTck.assertRetained(harness, retained);
            }
            assertSame(credentials, harness.context().getBean(GoogleCredentials.class));
            assertSame(credentialsProvider, harness.context().getBean(CredentialsProvider.class, Qualifiers.byName(Modules.PUBSUB)));

            // other credentials, together with a class, so the application restarts
            StringBuilder changed = new StringBuilder();
            configuration().forEach((key, value) -> changed.append(key).append('=').append(value).append('\n'));
            harness.resource("application.properties", changed.toString());
            harness.source("example.Service", SERVICE.formatted("third"));
            harness.reload();
            assertEquals(3, harness.generation());
            assertNotSame(credentials, harness.context().getBean(GoogleCredentials.class));
            assertNotSame(credentialsProvider, harness.context().getBean(CredentialsProvider.class, Qualifiers.byName(Modules.PUBSUB)));
            assertSame(harness.context().getBean(GoogleCredentials.class),
                harness.context().getBean(CredentialsProvider.class, Qualifiers.byName(Modules.PUBSUB)).getCredentials());
            credentials = null;
            credentialsProvider = null;
            channelProvider = null;

            ReloadTck.assertRetiredGenerationsCollected(harness);
        }
    }

    /**
     * The configuration of a service account of its own, whose key is generated: the credentials are read from it,
     * and nothing asks Google Cloud for a token.
     */
    private static Map<String, String> configuration() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        String privateKey = "-----BEGIN PRIVATE KEY-----\\n"
            + Base64.getEncoder().encodeToString(generator.generateKeyPair().getPrivate().getEncoded())
            + "\\n-----END PRIVATE KEY-----\\n";
        String serviceAccount = """
            {
              "type": "service_account",
              "project_id": "dev-project",
              "private_key_id": "dev-key",
              "private_key": "%s",
              "client_email": "dev@dev-project.iam.gserviceaccount.com",
              "client_id": "1234567890",
              "token_uri": "https://oauth2.googleapis.com/token"
            }
            """.formatted(privateKey);
        Map<String, String> configuration = new LinkedHashMap<>();
        configuration.put("gcp.project-id", "dev-project");
        configuration.put("gcp.credentials.encoded-key", Base64.getEncoder().encodeToString(serviceAccount.getBytes(StandardCharsets.UTF_8)));
        return configuration;
    }
}
