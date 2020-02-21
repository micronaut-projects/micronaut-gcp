package io.micronaut.function.http;

import io.micronaut.http.MutableHttpResponse;

/**
 *
 * @param <N> The native response type
 * @param <B> The body type
 */
public interface ServerlessHttpResponse<N, B> extends MutableHttpResponse<B> {

    /**
     * @return The native response type.
     */
    N getNativeResponse();
}
