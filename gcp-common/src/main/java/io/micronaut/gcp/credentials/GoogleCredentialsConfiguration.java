package io.micronaut.gcp.credentials;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties("gcp.credentials")
@Context
public class GoogleCredentialsConfiguration {

    public static final List<URI> DEFAULT_SCOPES = Collections.singletonList(URI.create("https://www.googleapis.com/auth/trace.append"));

    private List<URI> oauthScopes = DEFAULT_SCOPES;

    public @Nonnull List<URI> getOauthScopes() {
        return oauthScopes;
    }

    public void setOauthScopes(@Nullable List<URI> oauthScopes) {
        this.oauthScopes = oauthScopes == null ? Collections.emptyList() : oauthScopes;
    }
}
