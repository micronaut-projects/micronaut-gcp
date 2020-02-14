package io.micronaut.function.http;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;

public interface ServerlessRequest {

    /**
     * @return The request object
     */
    HttpRequest<? super Object> getRequest();

    /**
     * @return The response object
     */
    MutableHttpResponse<? super Object> getResponse();
}
