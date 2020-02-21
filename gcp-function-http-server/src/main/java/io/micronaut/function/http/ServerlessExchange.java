package io.micronaut.function.http;


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
