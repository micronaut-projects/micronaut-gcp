package io.micronaut.gcp.credentials;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ConfigurationProperties("gcp.credentials")
@Context
public class GoogleCredentialsConfiguration {
    public static final List<URI> DEFAULT_SCOPES = Collections.emptyList();

    private List<URI> scopes = DEFAULT_SCOPES;

    private String path;

    private String encodedKey;

    public @Nonnull List<URI> getScopes() {
        return scopes;
    }

    public void setScopes(@Nullable List<URI> scopes) {
        this.scopes = scopes == null ? Collections.emptyList() : scopes;
    }

    public Optional<String> getLocation() {
        return Optional.ofNullable(path);
    }

    public void setLocation(String location) {
        this.path = path;
    }

    public Optional<String> getEncodedKey() {
        return Optional.ofNullable(encodedKey);
    }

    public void setEncodedKey(String encodedKey) {
        this.encodedKey = encodedKey;
    }
}
