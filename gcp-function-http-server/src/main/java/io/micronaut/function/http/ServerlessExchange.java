package io.micronaut.function.http;

import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;

/**
 * Represents an HTTP exchange in a Serverless context.
 *
 * @author graemerocher
 * @since 1.2.0
 */
public class ServerlessExchange implements ServerlessRequest{

    private final HttpRequest<? super Object> request;
    private final MutableHttpResponse<? super Object> response;

    /**
     * Default constructor.
     * @param request The request
     * @param response The response
     */
    public ServerlessExchange(HttpRequest<? super Object> request, MutableHttpResponse<? super Object> response) {
        ArgumentUtils.requireNonNull("request", request);
        ArgumentUtils.requireNonNull("response", response);
        this.request = request;
        this.response = response;
    }

    /**
     * @return The request object
     */
    public HttpRequest<? super Object> getRequest() {
        return request;
    }

    /**
     * @return The response object
     */
    public MutableHttpResponse<? super Object> getResponse() {
        return response;
    }
}
