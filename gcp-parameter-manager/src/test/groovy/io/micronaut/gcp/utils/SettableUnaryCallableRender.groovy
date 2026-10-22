package io.micronaut.gcp.utils

import com.google.api.core.ApiFuture
import com.google.api.core.SettableApiFuture
import com.google.api.gax.rpc.ApiCallContext
import com.google.api.gax.rpc.UnaryCallable
import com.google.cloud.parametermanager.v1.ParameterVersionName
import com.google.cloud.parametermanager.v1.RenderParameterVersionRequest
import com.google.cloud.parametermanager.v1.RenderParameterVersionResponse
import com.google.protobuf.ByteString
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class SettableUnaryCallableRender extends UnaryCallable<RenderParameterVersionRequest, RenderParameterVersionResponse> {
    final Logger logger = LoggerFactory.getLogger(SettableUnaryCallableRender)

    @Override
    ApiFuture<RenderParameterVersionResponse> futureCall(RenderParameterVersionRequest request, ApiCallContext context) {
        ParameterVersionName parameterVersionName = ParameterVersionName.parse(request.getName())
        SettableApiFuture<RenderParameterVersionResponse> result = new SettableApiFuture<>()
        try {
            String contents;
            if (parameterVersionName.getLocation() != null && parameterVersionName.getLocation() != "global") {
                contents = LocalFileResourceLoader.loadRegionalParameter(parameterVersionName.getProject(), parameterVersionName.getLocation(), parameterVersionName.getParameter(), parameterVersionName.getParameterVersion())
            } else {
                contents = LocalFileResourceLoader.loadParameter(parameterVersionName.getProject(), parameterVersionName.getParameter(), parameterVersionName.getParameterVersion())
            }
            result.set(RenderParameterVersionResponse.newBuilder()
                    .setRenderedPayload(ByteString.copyFrom(contents.getBytes())).build())
        } catch (Exception e) {
            result.setException(new IllegalStateException("Could not find parameter"))
        }
        return result
    }
}
