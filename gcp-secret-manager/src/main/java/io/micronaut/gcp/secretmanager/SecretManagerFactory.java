package io.micronaut.gcp.secretmanager;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.gcp.UserAgentHeaderProvider;
import io.micronaut.gcp.condition.RequiresGoogleProjectId;

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
    public SecretManagerServiceClient secretManagerServiceClient() {
        try {
            SecretManagerServiceSettings settings = SecretManagerServiceSettings.newBuilder()
                    .setCredentialsProvider(this.credentialsProvider)
                    .setTransportChannelProvider(InstantiatingGrpcChannelProvider.newBuilder()
                    .setHeaderProvider(new UserAgentHeaderProvider("secret-emanager")).build()).build();
            return SecretManagerServiceClient.create(settings);
        } catch (IOException e) {
            throw new IllegalStateException("Could not instantiate SecretManagerServiceClient", e);
        }
    }

}
