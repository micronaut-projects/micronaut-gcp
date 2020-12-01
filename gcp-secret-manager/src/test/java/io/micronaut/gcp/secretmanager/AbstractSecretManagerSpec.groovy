package io.micronaut.gcp.secretmanager

import com.google.api.core.ApiFuture
import com.google.api.core.SettableApiFuture
import com.google.api.gax.rpc.ApiCallContext
import com.google.api.gax.rpc.UnaryCallable
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretPayload
import com.google.cloud.secretmanager.v1beta1.AccessSecretVersionRequest
import io.micronaut.context.annotation.Replaces
import io.micronaut.gcp.secretmanager.utils.LocalFileResourceLoader
import io.micronaut.test.annotation.MockBean
import spock.lang.Specification

abstract class AbstractSecretManagerSpec extends Specification{

    @Replaces(SecretManagerServiceClient)
    @MockBean
    SecretManagerServiceClient secretManagerClient(){
        def secretManagerClient = Mock(SecretManagerServiceClient)
        secretManagerClient.accessSecretVersionCallable() >> new SettableUnaryCallable()
        return secretManagerClient
    }
}

class SettableUnaryCallable extends UnaryCallable<AccessSecretVersionRequest, AccessSecretVersionResponse> {

    @Override
    ApiFuture<AccessSecretVersionResponse> futureCall(AccessSecretVersionRequest request, ApiCallContext context) {
        String contents = LocalFileResourceLoader.loadSecret(request.getName())
        SettableApiFuture<AccessSecretVersionResponse> result = new SettableApiFuture<>()
        result.set(AccessSecretVersionResponse.newBuilder()
                .setName(request.getName())
                .setPayload(SecretPayload.parseFrom(contents.getBytes()))
                .build())
        return result
    }
}
