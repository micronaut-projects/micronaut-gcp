package io.micronaut.gcp.credentials;

import com.google.api.gax.core.GoogleCredentialsProvider;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Factory
@Requires(classes = com.google.auth.oauth2.GoogleCredentials.class)
public class GoogleCredentialsFactory {

    private final GoogleCredentialsConfiguration configuration;

    public GoogleCredentialsFactory(GoogleCredentialsConfiguration configuration) {
        this.configuration = configuration;
    }

    @Requires(missingBeans = GoogleCredentialsProvider.class)
    @Requires(classes = com.google.auth.oauth2.GoogleCredentials.class)
    @Singleton
    GoogleCredentialsProvider defaultGoogleCredentials() throws IOException {
        final List<URI> oauthScopes = configuration.getOauthScopes();
        return GoogleCredentialsProvider.newBuilder()
                .setScopesToApply(oauthScopes.stream().map(URI::toString).collect(Collectors.toList()))
                .build();
    }
}
