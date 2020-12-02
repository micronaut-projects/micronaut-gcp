package io.micronaut.gcp.utils

import com.google.api.core.ApiFuture
import com.google.api.core.SettableApiFuture
import com.google.api.gax.rpc.ApiCallContext
import com.google.api.gax.rpc.UnaryCallable
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse
import com.google.cloud.secretmanager.v1.SecretPayload
import com.google.cloud.secretmanager.v1.SecretVersionName
import com.google.protobuf.ByteString

class SettableUnaryCallable extends UnaryCallable<AccessSecretVersionRequest, AccessSecretVersionResponse> {

    @Override
    ApiFuture<AccessSecretVersionResponse> futureCall(AccessSecretVersionRequest request, ApiCallContext context) {
        SecretVersionName secretVersionName = SecretVersionName.parse(request.getName())
        SettableApiFuture<AccessSecretVersionResponse> result = new SettableApiFuture<>()
        try {
            String contents = LocalFileResourceLoader.loadSecret(secretVersionName.getProject(), secretVersionName.getSecret())
            result.set(AccessSecretVersionResponse.newBuilder()
                    .setName(request.getName())
                    .setPayload(SecretPayload.newBuilder().setData(ByteString.copyFrom(contents.getBytes())).build())
                    .build())
        } catch(Exception e) {
            result.setException(new IllegalStateException("Could not find secret"))
        }
        return result
    }
}
