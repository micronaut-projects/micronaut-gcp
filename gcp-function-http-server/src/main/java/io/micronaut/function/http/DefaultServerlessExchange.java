package io.micronaut.function.http;

import io.micronaut.core.util.ArgumentUtils;

/**
 * Represents an HTTP exchange in a Serverless context.
 *
 * @param <Req> The native request type
 * @param <Res> The native response type
 * @author graemerocher
 * @since 2.0.0
 */
public class DefaultServerlessExchange<Req, Res> implements ServerlessExchange<Req, Res> {

    private final ServerlessHttpRequest<Req, ? super Object> request;
    private final ServerlessHttpResponse<Res, ? super Object> response;

    /**
     * Default constructor.
     *
     * @param request  The request
     * @param response The response
     */
    public DefaultServerlessExchange(
            ServerlessHttpRequest<Req, ? super Object> request,
            ServerlessHttpResponse<Res, ? super Object> response) {
        ArgumentUtils.requireNonNull("request", request);
        ArgumentUtils.requireNonNull("response", response);
        this.request = request;
        this.response = response;
    }

    /**
     * @return The request object
     */
    public ServerlessHttpRequest<Req, ? super Object> getRequest() {
        return request;
    }

    /**
     * @return The response object
     */
    public ServerlessHttpResponse<Res, ? super Object> getResponse() {
        return response;
    }
}
