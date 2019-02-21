package io.micronaut.gcp.credentials;

import com.google.auth.oauth2.GoogleCredentials;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Factory
@Requires(classes = com.google.auth.oauth2.GoogleCredentials.class)
public class GoogleCredentialsFactory {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleCredentialsFactory.class);

    private final GoogleCredentialsConfiguration configuration;

    public GoogleCredentialsFactory(GoogleCredentialsConfiguration configuration) {
        this.configuration = configuration;
    }

    @Requires(missingBeans = GoogleCredentials.class)
    @Requires(classes = com.google.auth.oauth2.GoogleCredentials.class)
    @Primary
    @Singleton
    GoogleCredentials defaultGoogleCredentials() throws IOException {
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
