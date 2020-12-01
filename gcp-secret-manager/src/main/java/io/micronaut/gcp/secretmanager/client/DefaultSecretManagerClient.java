package io.micronaut.gcp.secretmanager.client;

import com.google.cloud.secretmanager.v1.*;
import io.micronaut.context.annotation.Requires;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.condition.RequiresGoogleProjectId;
import io.reactivex.Maybe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

@Singleton
@RequiresGoogleProjectId
@Requires(classes = SecretManagerServiceClient.class)
public class DefaultSecretManagerClient implements SecretManagerClient {

    private final SecretManagerServiceClient client;
    private final GoogleCloudConfiguration googleCloudConfiguration;
    private final Logger logger = LoggerFactory.getLogger(SecretManagerClient.class);

    public DefaultSecretManagerClient(SecretManagerServiceClient client, GoogleCloudConfiguration googleCloudConfiguration) {
        this.client = client;
        this.googleCloudConfiguration = googleCloudConfiguration;
    }

    @Override
    public Maybe<String> getSecret(String secretId) {
        return getSecret(secretId, LATEST ,this.googleCloudConfiguration.getProjectId());
    }

    @Override
    public Maybe<String> getSecret(String secretId, String version) {
        return getSecret(secretId, version, this.googleCloudConfiguration.getProjectId());
    }

    @Override
    public Maybe<String> getSecret(String secretId, String version, String projectId) {
        SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, version);
        AccessSecretVersionRequest request = AccessSecretVersionRequest.newBuilder()
                .setName(secretVersionName.toString())
                .build();
        return Maybe.fromFuture(client.accessSecretVersionCallable().futureCall(request))
                .map(response -> response.getPayload().getData().toStringUtf8())
                .onErrorResumeNext(Maybe.empty());
    }
}
