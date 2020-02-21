package io.micronaut.function.http;

import com.google.cloud.functions.HttpMessage;
import io.micronaut.http.HttpRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @param <N> The native request type
 * @param <B> The body type
 */
public interface ServerlessHttpRequest<N, B> extends HttpRequest<B> {
    /**
     * Returns an {@link InputStream} that can be used to read the body of this HTTP request.
     * Every call to this method on the same {@link HttpMessage} will return the same object.
     * This method is typically used to read binary data. If the body is text, the
     * {@link #getReader()} method is more appropriate.
     *
     * @throws IOException if a valid {@link InputStream} cannot be returned for some reason.
     * @throws IllegalStateException if {@link #getReader()} has already been called on this instance.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns a {@link BufferedReader} that can be used to read the text body of this HTTP request.
     * Every call to this method on the same {@link HttpMessage} will return the same object.
     *
     * @throws IOException if a valid {@link BufferedReader} cannot be returned for some reason.
     * @throws IllegalStateException if {@link #getInputStream()} has already been called on this
     *     instance.
     */
    BufferedReader getReader() throws IOException;

    /**
     * @return The native request type
     */
    N getNativeRequest();
}
