package io.micronaut.gcp.secretmanager.client;

import io.reactivex.Single;

/**
 * This interface is intended to abstract interactions with {@link com.google.cloud.secretmanager.v1.SecretManagerServiceClient}, and instead of returning Google's {@link com.google.api.core.ApiFuture}
 * transform it on reactive extensions common interfaces such as {@link io.reactivex.Single}.
 *
 * @author Vinicius Carvalho
 * @since 3.2.0
 */
public interface SecretManagerClient {

    String LATEST = "latest";

    /**
     * Fetches a secret from the Secret Manager storage using the `gcp.projectId` project, uses "latest" as the version.
     * @param secretId - name of the secret
     * @return The byte contents of the secret
     */
    Single<VersionedSecret> fetchSecret(String secretId);

    /**
     * Fetches a secret from the Secret Manager storage using the `gcp.projectId` project.
     * @param secretId - name of the secret
     * @param version - version of the secret
     * @return The byte contents of the secret
     */
    Single<VersionedSecret> fetchSecret(String secretId, String version);

    /**
     * Fetches a secret from the Secret Manager storage.
     * @param secretId - name of the secret
     * @param version - version of the secret
     * @param projectId - project identifier
     * @return The byte contents of the secret
     */
    Single<VersionedSecret> fetchSecret(String secretId, String version, String projectId);

}
