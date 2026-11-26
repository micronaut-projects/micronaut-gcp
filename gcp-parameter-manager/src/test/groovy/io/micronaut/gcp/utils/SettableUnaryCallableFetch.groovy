package io.micronaut.gcp.utils

import com.google.api.core.ApiFuture
import com.google.api.core.SettableApiFuture
import com.google.api.gax.rpc.ApiCallContext
import com.google.api.gax.rpc.UnaryCallable
import com.google.cloud.parametermanager.v1.GetParameterVersionRequest
import com.google.cloud.parametermanager.v1.ParameterVersion
import com.google.cloud.parametermanager.v1.ParameterVersionName
import com.google.cloud.parametermanager.v1.ParameterVersionPayload
import com.google.protobuf.ByteString
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SettableUnaryCallableFetch extends UnaryCallable<GetParameterVersionRequest, ParameterVersion> {
    final Logger logger = LoggerFactory.getLogger(SettableUnaryCallableFetch)

    @Override
    ApiFuture<ParameterVersion> futureCall(GetParameterVersionRequest request, ApiCallContext context) {
        ParameterVersionName parameterVersionName = ParameterVersionName.parse(request.getName())
        SettableApiFuture<ParameterVersion> result = new SettableApiFuture<>()
        try {
            String contents;
            if (parameterVersionName.getLocation() != null && parameterVersionName.getLocation() != "global") {
                contents = LocalFileResourceLoader.loadRegionalParameter(parameterVersionName.getProject(), parameterVersionName.getLocation(), parameterVersionName.getParameter(), parameterVersionName.getParameterVersion())
            } else {
                contents = LocalFileResourceLoader.loadParameter(parameterVersionName.getProject(), parameterVersionName.getParameter(), parameterVersionName.getParameterVersion())
            }
            result.set(ParameterVersion.newBuilder()
                    .setPayload(ParameterVersionPayload.newBuilder().setData(ByteString.copyFrom(contents.getBytes())).build())
                    .build())
        } catch (Exception e) {
            result.setException(new IllegalStateException("Could not find parameter"))
        }
        return result
    }
}
