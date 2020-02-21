package io.micronaut.function.http;

import io.micronaut.http.MutableHttpResponse;

/**
 * Models a serverless HTTP response, allowing access to the native response.
 *
 * @param <N> The native response type
 * @param <B> The body type
 * @author graemerocher
 * @since 2.0.0
 */
public interface ServerlessHttpResponse<N, B> extends MutableHttpResponse<B> {

    /**
     * @return The native response type.
     */
    N getNativeResponse();
}
