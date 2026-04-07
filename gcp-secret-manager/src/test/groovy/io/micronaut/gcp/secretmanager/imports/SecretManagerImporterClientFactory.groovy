package io.micronaut.gcp.secretmanager.imports

import com.google.api.gax.core.CredentialsProvider
import com.google.api.gax.rpc.TransportChannelProvider
import com.google.api.gax.rpc.UnaryCallable
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.stub.SecretManagerServiceStub
import io.micronaut.gcp.secretmanager.configuration.SecretManagerConfigurationProperties
import io.micronaut.gcp.utils.SettableUnaryCallable

import java.util.concurrent.TimeUnit

class SecretManagerImporterClientFactory {

    SecretManagerServiceClient create(SecretManagerConfigurationProperties configurationProperties,
                                      CredentialsProvider credentialsProvider,
                                      TransportChannelProvider transportChannelProvider) {
        return SecretManagerServiceClient.create(new TestSecretManagerServiceStub())
    }

    private static final class TestSecretManagerServiceStub extends SecretManagerServiceStub {
        @Override
        UnaryCallable<AccessSecretVersionRequest, AccessSecretVersionResponse> accessSecretVersionCallable() {
            return new SettableUnaryCallable()
        }

        @Override
        void close() {
        }

        @Override
        void shutdown() {
        }

        @Override
        boolean isShutdown() {
            return false
        }

        @Override
        boolean isTerminated() {
            return false
        }

        @Override
        void shutdownNow() {
        }

        @Override
        boolean awaitTermination(long duration, TimeUnit unit) {
            return true
        }
    }
}
