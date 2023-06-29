package io.micronaut.http.server.tck.gcp.function;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.gcp.function.http.GoogleHttpResponse;
import io.micronaut.gcp.function.http.HttpFunction;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.tck.ServerUnderTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("java:S2187") // Suppress because despite its name, this is not a Test
public class GcpFunctionHttpServerUnderTest implements ServerUnderTest {

    private static final Logger LOG = LoggerFactory.getLogger(GcpFunctionHttpServerUnderTest.class);

    private final HttpFunction function;

    public GcpFunctionHttpServerUnderTest(Map<String, Object> properties) {
        properties.put("micronaut.server.context-path", "/");
        properties.put("endpoints.refresh.enabled", StringUtils.FALSE);
        properties.put("endpoints.health.service-ready-indicator-enabled", StringUtils.FALSE);
        this.function = new HttpFunction(
            ApplicationContext.builder(Environment.FUNCTION, Environment.GOOGLE_COMPUTE, Environment.TEST)
                .properties(properties)
                .deduceEnvironment(false)
                .start()
        );
    }

    @Override
    public <I, O> HttpResponse<O> exchange(HttpRequest<I> request, Argument<O> bodyType) {
        HttpResponse<O> response = new HttpResponseAdaptor<>(function.invoke(request), bodyType);
        if (response.getStatus().getCode() >= 400) {
            LOG.error("Response body: {}", response.getBody(String.class).orElse(null));
            throw new HttpClientResponseException("error " + response.getStatus().getReason() + " (" + response.getStatus().getCode() + ")", response);
        }
        return response;
    }

    @Override
    public <I, O, E> HttpResponse<O> exchange(HttpRequest<I> request, Argument<O> bodyType, Argument<E> errorType) {
        return exchange(request, bodyType);
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return function.getApplicationContext();
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
        public int code() {
            return googleHttpResponse.getStatusCode();
        }

        @Override
        public String reason() {
            return getStatus().getReason();
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
            if (bodyType != null && bodyType.isAssignableFrom(byte[].class)) {
                return (Optional<O>) Optional.of(googleHttpResponse.getBodyAsBytes());
            } else {
                return (Optional<O>) Optional.of(googleHttpResponse.getBodyAsText());
            }
        }
    }
}
