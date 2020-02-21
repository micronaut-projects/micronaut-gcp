package io.micronaut.function.http;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequestWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @param <N> The native request type
 * @param <B> The body type
 */
public class ServerlessRequestAndBody<N, B> extends HttpRequestWrapper<B> implements ServerlessHttpRequest<N, B> {

    private final B body;

    /**
     * @param delegate The Http Request
     */
    public ServerlessRequestAndBody(ServerlessHttpRequest<N, B> delegate, B body) {
        super(delegate);
        this.body = Objects.requireNonNull(body, "Body cannot be null");
    }

    @Override
    public Optional<B> getBody() {
        return Optional.of(body);
    }

    @Override
    public <T> Optional<T> getBody(Class<T> type) {
        return ConversionService.SHARED.convert(body, type);
    }

    @Override
    public <T> Optional<T> getBody(Argument<T> type) {
        return ConversionService.SHARED.convert(body, type);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        throw new IllegalStateException("Body already read");
    }

    @Override
    public BufferedReader getReader() throws IOException {
        throw new IllegalStateException("Body already read");
    }

    @Override
    public N getNativeRequest() {
        return ((ServerlessHttpRequest<N, B>) getDelegate()).getNativeRequest();
    }
}
