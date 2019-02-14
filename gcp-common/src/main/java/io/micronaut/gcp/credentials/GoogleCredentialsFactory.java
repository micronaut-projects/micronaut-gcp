package io.micronaut.gcp.credentials;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.CollectionUtils;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Factory
@Requires(classes = com.google.auth.oauth2.GoogleCredentials.class)
public class GoogleCredentialsFactory {

    private final GoogleCredentialsConfiguration configuration;
    private final AccessToken accessToken;

    public GoogleCredentialsFactory(GoogleCredentialsConfiguration configuration, @Nullable AccessToken accessToken) {
        this.configuration = configuration;
        this.accessToken = accessToken;
    }

    @Requires(missingBeans = GoogleCredentials.class)
    @Requires(classes = com.google.auth.oauth2.GoogleCredentials.class)
    @Singleton
    GoogleCredentials defaultGoogleCredentials() throws IOException {
        final List<URI> oauthScopes = configuration.getOauthScopes();
        GoogleCredentials creds;
        if (accessToken != null) {
            creds = GoogleCredentials.newBuilder().setAccessToken(accessToken).build();
        } else {
            creds = GoogleCredentials.getApplicationDefault();
        }
        if (CollectionUtils.isNotEmpty(oauthScopes)) {
            return creds
                    .createScoped(oauthScopes.stream().map(URI::toString).collect(Collectors.toList()));
        } else {
            return creds;
        }
    }
}
