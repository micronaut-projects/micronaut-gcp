package io.micronaut.gcp.secretmanager.client;

import io.reactivex.Flowable;
import io.reactivex.Maybe;

/**
 * This interface is intended to abstract interactions with {@link com.google.cloud.secretmanager.v1.SecretManagerServiceClient}, and instead of returning Google's {@link com.google.api.core.ApiFuture}
 * transform it on reactive extensions.
 *
 * @author Vinicius Carvalho
 * @since 3.2.0
 */
public interface SecretManagerClient {

    String LATEST = "latest";

    /**
     * Fetches a secret from the Secret Manager storage using the `gcp.projectId` project, and "latest" as the version.
     * @param secretId - name of the secret
     * @return String value of the secret or empty
     */
    Maybe<String> getSecret(String secretId);

    /**
     * Fetches a secret from the Secret Manager storage using the `gcp.projectId` project.
     * @param secretId - name of the secret
     * @param version - version of the secret
     * @return String value of the secret or empty
     */
    Maybe<String> getSecret(String secretId, String version);

    /**
     * Fetches a secret from the Secret Manager storage.
     * @param secretId - name of the secret
     * @param version - version of the secret
     * @param projectId - project identifier
     * @return String value of the secret or empty
     */
    Maybe<String> getSecret(String secretId, String version, String projectId);

}
