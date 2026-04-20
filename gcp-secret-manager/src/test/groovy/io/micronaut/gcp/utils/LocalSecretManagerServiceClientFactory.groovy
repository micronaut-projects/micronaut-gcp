package io.micronaut.gcp.utils

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.stub.SecretManagerServiceStub
import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.secretmanager.SecretManagerFactory
import jakarta.inject.Singleton
import spock.lang.Specification

@Factory
@BootstrapContextCompatible
@Requires(property = 'spec.name')
@Requires(property = 'spec.name', notEquals = 'SecretManagerClientIntegrationSpec')
class LocalSecretManagerServiceClientFactory extends Specification {

    @Singleton
    @Replaces(value = SecretManagerServiceClient, factory = SecretManagerFactory)
    SecretManagerServiceClient secretManagerServiceClient(){
        def stub = Mock(SecretManagerServiceStub)
        stub.accessSecretVersionCallable() >> new SettableUnaryCallable()
        return SecretManagerServiceClient.create(stub)
    }
}
