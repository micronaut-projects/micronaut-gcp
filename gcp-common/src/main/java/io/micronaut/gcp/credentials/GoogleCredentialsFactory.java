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
package io.micronaut.gcp.credentials;

import com.google.auth.oauth2.GoogleCredentials;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.util.ArgumentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A factory for creating {@link GoogleCredentials}.
 *
 * @author graemerocher
 * @author Ray Tsang
 * @since 1.0
 */
@Factory
@Requires(classes = com.google.auth.oauth2.GoogleCredentials.class)
@BootstrapContextCompatible
public class GoogleCredentialsFactory {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleCredentialsFactory.class);

    private final GoogleCredentialsConfiguration configuration;

    /**
     * Default constructor.
     * @param configuration The configuration
     */
    public GoogleCredentialsFactory(@Nonnull GoogleCredentialsConfiguration configuration) {
        ArgumentUtils.requireNonNull("configuration", configuration);
        this.configuration = configuration;
    }

    /**
     * Method used to return the default {@link GoogleCredentials} and provide it as a bean.
     *
     * It will determine which credential in the following way:
     * <ol>
     *     <li>If <pre>gcp.credentials.location</pre> is specified, use its location</li>
     *     <li>Otherwise, if <pre>gcp.credentials.encodedKey</pre> is specified, decode it and use its content</li>
     *     <li>None of the 2 properties were specified, use Application Default credential resolution. See
     *     <a href="https://github.com/googleapis/google-cloud-java#authentication">Google Cloud Java authentication</a>.
     *     This will resolve credential in the following order:
     *       <ol>
     *           <li>The credentials file pointed to by the <pre>GOOGLE_APPLICATION_CREDENTIALS</pre> environment variable</li>
     *           <li>Credentials provided by the Google Cloud SDK <pre>gcloud auth application-default login</pre> command</li>
     *           <li>Google App Engine built-in credentials when running inside of Google App Engine</li>
     *           <li>Google Cloud Shell built-in credentials when running inside of Google Cloud Shell</li>
     *           <li>Google Compute Engine built-in credentials when running inside of Google Compute Engine or Kubernetes Engine</li>
     *       </ol>
     *     </li>
     * </ol>
     *
     * @return The {@link GoogleCredentials}
     * @throws IOException An exception if an error occurs
     */
    @Requires(missingBeans = GoogleCredentials.class)
    @Requires(classes = com.google.auth.oauth2.GoogleCredentials.class)
    @Primary
    @Singleton
    protected GoogleCredentials defaultGoogleCredentials() throws IOException {
        final List<String> scopes = configuration.getScopes().stream()
                .map(URI::toString).collect(Collectors.toList());

        GoogleCredentials credentials;
        if (configuration.getLocation().isPresent() && configuration.getEncodedKey().isPresent()) {
            throw new ConfigurationException("Please specify only one of gcp.credentials.location or gcp.credentials.encodedKey");
        } else if (configuration.getLocation().isPresent()) {
            LOG.info("Google Credentials from gcp.credentials.location = " + configuration.getLocation());
            FileInputStream fis = new FileInputStream(configuration.getLocation().get());
            credentials = GoogleCredentials.fromStream(fis);
            fis.close();
        } else if (configuration.getEncodedKey().isPresent()) {
            LOG.info("Google Credentials from gcp.credentials.encodedKey");
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] bytes = decoder.decode(configuration.getEncodedKey().get());
            ByteArrayInputStream is = new ByteArrayInputStream(bytes);
            credentials = GoogleCredentials.fromStream(is);
            is.close();
        } else {
            LOG.info("Google Credentials from Application Default Credentials");
            credentials = GoogleCredentials.getApplicationDefault();
        }

        return credentials.createScoped(scopes);
    }
}
