package io.micronaut.gcp.secretmanager.client;

import com.google.cloud.secretmanager.v1.*;
import io.micronaut.context.annotation.Requires;
import io.micronaut.gcp.GoogleCloudConfiguration;
import io.micronaut.gcp.condition.RequiresGoogleProjectId;
import io.reactivex.Flowable;
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
    public Flowable<VersionedSecret> fetchSecret(String secretId) {
        return fetchSecret(secretId, SecretManagerClient.LATEST);
    }

    @Override
    public Flowable<VersionedSecret> fetchSecret(String secretId, String version) {
        return fetchSecret(secretId, version, googleCloudConfiguration.getProjectId());
    }

    @Override
    public Flowable<VersionedSecret> fetchSecret(String secretId, String version, String projectId) {

        GetSecretRequest secretRequest = GetSecretRequest.newBuilder()
                .setName(SecretName.of(projectId, secretId).toString())
                .build();

        AccessSecretVersionRequest secretVersionRequest = AccessSecretVersionRequest.newBuilder()
                .setName(SecretVersionName.of(projectId, secretId, version).toString())
                .build();

        return Flowable.defer(() ->
                Flowable.fromFuture(client.getSecretCallable().futureCall(secretRequest))
                        .flatMap(secret -> Flowable.fromFuture(client.accessSecretVersionCallable().futureCall(secretVersionRequest))
                                .map(response -> new VersionedSecret(secretId, response.getPayload().getData().toByteArray(), version, secret.getLabelsMap()))))
                .onErrorResumeNext(Flowable.empty());

    }
}
