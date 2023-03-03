package io.micronaut.http.server.tck.gcp.function;

import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.gcp.function.http.test.InvokerHttpServer;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.server.tck.ServerUnderTest;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class GcpFunctionHttpServerUnderTest implements ServerUnderTest {

    private final InvokerHttpServer server;
    private HttpClient httpClient;
    private BlockingHttpClient client;

    public GcpFunctionHttpServerUnderTest(Map<String, Object> properties) {
        server = ApplicationContext.run(InvokerHttpServer.class, properties);
    }

    @Override
    public <I, O> HttpResponse<O> exchange(HttpRequest<I> request, Argument<O> bodyType) {
        return getBlockingHttpClient().exchange(request, bodyType);
    }

    @Override
    public <I, O> HttpResponse<O> exchange(HttpRequest<I> request) {
        return getBlockingHttpClient().exchange(request);
    }

    @Override
    public <I, O> HttpResponse<O> exchange(HttpRequest<I> request, Class<O> bodyType) {
        return getBlockingHttpClient().exchange(request, bodyType);
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return server.getApplicationContext();
    }

    @Override
    public void close() throws IOException {
        server.close();
    }

    @Override
    @NonNull
    public Optional<Integer> getPort() {
        return Optional.of(server.getPort());
    }

    @NonNull
    private HttpClient getHttpClient() {
        if (httpClient == null) {
            this.httpClient = getApplicationContext().createBean(HttpClient.class, server.getURL());
        }
        return httpClient;
    }

    @NonNull
    private BlockingHttpClient getBlockingHttpClient() {
        if (client == null) {
            this.client = getHttpClient().toBlocking();
        }
        return client;
    }
}
