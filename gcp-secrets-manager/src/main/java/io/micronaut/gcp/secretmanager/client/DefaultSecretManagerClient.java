package io.micronaut.gcp.secretmanager.client;

import com.google.cloud.secretmanager.v1.*;
import io.micronaut.context.annotation.Requires;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.condition.RequiresGoogleProjectId;
import io.reactivex.Single;

import javax.inject.Singleton;

@Singleton
@RequiresGoogleProjectId
@Requires(classes = SecretManagerServiceClient.class)
public class DefaultSecretManagerClient implements SecretManagerClient {

    private final SecretManagerServiceClient client;
    private final GoogleCloudConfiguration googleCloudConfiguration;

    public DefaultSecretManagerClient(SecretManagerServiceClient client, GoogleCloudConfiguration googleCloudConfiguration) {
        this.client = client;
        this.googleCloudConfiguration = googleCloudConfiguration;
    }

    @Override
    public Single<byte[]> fetchSecret(String secretId) {
        return fetchSecret(secretId, SecretManagerClient.LATEST);
    }

    @Override
    public Single<byte[]> fetchSecret(String secretId, String version) {
        return fetchSecret(secretId, version, googleCloudConfiguration.getProjectId());
    }

    @Override
    public Single<byte[]> fetchSecret(String secretId, String version, String projectId) {
        GetSecretRequest secretRequest = GetSecretRequest.newBuilder()
                .setName(SecretName.of(projectId, secretId).toString())
                .build();
        AccessSecretVersionRequest secretVersionRequest = AccessSecretVersionRequest.newBuilder()
                .setName(SecretVersionName.of(projectId, secretId, version).toString())
                .build();
        
        return null;
    }
}
