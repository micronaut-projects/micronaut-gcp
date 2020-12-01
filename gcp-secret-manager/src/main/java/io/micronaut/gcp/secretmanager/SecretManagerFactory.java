package io.micronaut.gcp.secretmanager;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.gcp.Modules;
import io.micronaut.gcp.UserAgentHeaderProvider;
import io.micronaut.gcp.condition.RequiresGoogleProjectId;
import org.threeten.bp.Duration;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * Factory to create SecretManager clients.
 *
 * @author Vinicius Carvalho
 * @since 3.2.0
 */
@Factory
@Requires(classes = {SecretManagerServiceClient.class})
@RequiresGoogleProjectId
public class SecretManagerFactory {

    private final CredentialsProvider credentialsProvider;

    public SecretManagerFactory(GoogleCredentials credentials) {
        this.credentialsProvider = FixedCredentialsProvider.create(credentials);
    }


    /**
     * Creates a {@link SecretManagerServiceClient} instance.
     * @return an instance using defaults.
     */
    @Singleton
    public SecretManagerServiceClient secretManagerServiceClient(@Named(Modules.SECRET_MANAGER) CredentialsProvider credentialsProvider,
                                                                 @Named(Modules.SECRET_MANAGER) TransportChannelProvider transportChannelProvider) {
        try {
            SecretManagerServiceSettings settings = SecretManagerServiceSettings.newBuilder()
                    .setCredentialsProvider(credentialsProvider)
                    .setTransportChannelProvider(transportChannelProvider)
                    .build();
            return SecretManagerServiceClient.create(settings);
        } catch (IOException e) {
            throw new IllegalStateException("Could not instantiate SecretManagerServiceClient", e);
        }
    }

    /**
     * Returns a default {@link CredentialsProvider}, allows users to override it and provide their own implementation.
     * @param credentials default credentials, if not overridden by user should be provided by {@link io.micronaut.gcp.credentials.GoogleCredentialsFactory}
     * @return A {@link FixedCredentialsProvider} holding the given credentials.
     */
    @Singleton
    @Named(Modules.SECRET_MANAGER)
    public CredentialsProvider credentialsProvider(GoogleCredentials credentials) {
        return FixedCredentialsProvider.create(credentials);
    }

    /**
     *
     * @return default {@link TransportChannelProvider}TransportChannelProvider
     */
    @Singleton
    @Named(Modules.SECRET_MANAGER)
    public TransportChannelProvider transportChannelProvider() {
        return InstantiatingGrpcChannelProvider.newBuilder()
                .setHeaderProvider(new UserAgentHeaderProvider(Modules.SECRET_MANAGER))
                .build();
    }

}
