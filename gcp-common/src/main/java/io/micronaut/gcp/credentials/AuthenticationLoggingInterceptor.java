/*
 * Copyright 2017-2023 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.gcp.credentials;

import com.google.auth.RequestMetadataCallback;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.MutableArgumentValue;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * An interceptor for managed instances of {@link com.google.auth.oauth2.GoogleCredentials} that logs certain types of
 * authentication errors that the GCP libraries handle silently as infinitely retryable events.
 *
 * @author Jeremy Grelle
 * @since 5.2.0
 */
@Singleton
public class AuthenticationLoggingInterceptor implements MethodInterceptor<Object, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationLoggingInterceptor.class);
    private static final String LOGGED_AUTHENTICATION_METHOD = "getRequestMetadata";

    /**
     * Intercepts the "getRequestMetadata" call and logs any retryable errors before allowing the GCP library to continue
     * its normal retry algorithm.
     *
     * @param context The method invocation context
     * @return the result of the method invocation
     */
    @Override
    public @Nullable Object intercept(MethodInvocationContext<Object, Object> context) {
        if (!context.getExecutableMethod().getMethodName().equals(LOGGED_AUTHENTICATION_METHOD)) {
            return context.proceed();
        }
        Map<String, MutableArgumentValue<?>> params = context.getParameters();
        params.entrySet().stream().filter(entry -> entry.getValue().getType().equals(RequestMetadataCallback.class))
            .findFirst()
            .ifPresent(entry -> {
                @SuppressWarnings("unchecked") MutableArgumentValue<RequestMetadataCallback> argValue = (MutableArgumentValue<RequestMetadataCallback>) entry.getValue();
                RequestMetadataCallback callback = argValue.getValue();
                argValue.setValue(new LoggingRequestMetadataCallback(callback));
            });
        return context.proceed();
    }

    /**
     * A wrapper {@link RequestMetadataCallback} implementation that logs failures with a warning before proceeding with
     * the original callback.
     */
    private static final class LoggingRequestMetadataCallback implements RequestMetadataCallback {

        private final RequestMetadataCallback callback;

        private LoggingRequestMetadataCallback(RequestMetadataCallback callback) {
            this.callback = callback;
        }

        @Override
        public void onSuccess(Map<String, List<String>> metadata) {
            this.callback.onSuccess(metadata);
        }

        @Override
        public void onFailure(Throwable ex) {
            if (ex instanceof IOException) {
                LOG.warn("A failure occurred while attempting to build credential metadata for a GCP API request. The GCP libraries treat this as " +
                    "a retryable error, but misconfigured credentials can keep it from ever succeeding.", ex);
            }
            this.callback.onFailure(ex);
        }
    }
}
