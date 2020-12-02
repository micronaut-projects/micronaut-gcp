package io.micronaut.gcp.utils

import com.google.api.core.ApiFuture
import com.google.api.core.SettableApiFuture
import com.google.api.gax.rpc.ApiCallContext
import com.google.api.gax.rpc.UnaryCallable
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretPayload
import com.google.cloud.secretmanager.v1.SecretVersionName
import com.google.cloud.secretmanager.v1.stub.SecretManagerServiceStub
import com.google.protobuf.ByteString
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.gcp.secretmanager.SecretManagerFactory
import spock.lang.Specification
import spock.mock.DetachedMockFactory
import spock.mock.MockFactory

import javax.inject.Singleton

@Factory
class LocalSecretManagerServiceClientFactory extends Specification {


    @Singleton
    @Replaces(value = SecretManagerServiceClient, factory = SecretManagerFactory)
    SecretManagerServiceClient secretManagerServiceClient(){
        def stub = Mock(SecretManagerServiceStub)
        stub.accessSecretVersionCallable() >> new SettableUnaryCallable()
        return SecretManagerServiceClient.create(stub)
    }

}

