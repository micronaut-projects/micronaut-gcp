package io.micronaut.http.server.tck.gcp.function;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.type.Argument;
import io.micronaut.gcp.function.http.GoogleHttpResponse;
import io.micronaut.gcp.function.http.HttpFunction;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.server.tck.ServerUnderTest;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("java:S2187") // Suppress because despite its name, this is not a Test
public class GcpFunctionHttpServerUnderTest implements ServerUnderTest {

    private final HttpFunction function;

    public GcpFunctionHttpServerUnderTest(Map<String, Object> properties) {
        properties.put("micronaut.server.context-path", "/");
        this.function = new HttpFunction(ApplicationContext.builder(Environment.FUNCTION, Environment.GOOGLE_COMPUTE, Environment.TEST)
            .properties(properties)
            .deduceEnvironment(false)
            .start());
    }

    @Override
    public <I, O> HttpResponse<O> exchange(HttpRequest<I> request, Argument<O> bodyType) {
        HttpResponse<O> response = new HttpResponseAdaptor<>(function.invoke(request), bodyType);
        if (response.getStatus().getCode() >= 400) {
            throw new HttpClientResponseException("error", response);
        }
        return response;
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return this.function.getApplicationContext();
    }

    @Override
    public void close() throws IOException {
        if (function != null) {
            function.close();
        }
    }

    @Override
    @NonNull
    public Optional<Integer> getPort() {
        // This port is used in the CorsSimpleRequestTests
        return Optional.of(1234);
    }

    static class HttpResponseAdaptor<O> implements HttpResponse<O> {

        final GoogleHttpResponse googleHttpResponse;
        private final Argument<O> bodyType;

        HttpResponseAdaptor(GoogleHttpResponse googleHttpResponse, Argument<O> bodyType) {
            this.googleHttpResponse = googleHttpResponse;
            this.bodyType = bodyType;
        }

        @Override
        public HttpStatus getStatus() {
            return googleHttpResponse.getStatus();
        }

        @Override
        @NonNull
        public HttpHeaders getHeaders() {
            return googleHttpResponse.getHttpHeaders();
        }

        @Override
        @NonNull
        public MutableConvertibleValues<Object> getAttributes() {
            return null;
        }

        @Override
        @NonNull
        public Optional<O> getBody() {
            if (bodyType == null) {
                return Optional.empty();
            }
            if (bodyType.isAssignableFrom(String.class)) {
                return (Optional<O>) Optional.of(googleHttpResponse.getBodyAsText());
            }
            return googleHttpResponse.getBody(bodyType);
        }
    }
}
