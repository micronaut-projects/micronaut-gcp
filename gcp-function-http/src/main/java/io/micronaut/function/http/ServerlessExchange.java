package io.micronaut.function.http;

/**
 * Represents an HTTP exchange in a serverless context.
 *
 * @param <Req> The native request type
 * @param <Res> The native response type
 * @author graemerocher
 * @since 2.0.0
 */
public interface ServerlessExchange<Req, Res> {

    /**
     * @return The request object
     */
    ServerlessHttpRequest<Req, ? super Object> getRequest();

    /**
     * @return The response object
     */
    ServerlessHttpResponse<Res, ? super Object> getResponse();
}
