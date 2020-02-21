package io.micronaut.function.http;

import io.micronaut.core.io.service.ServiceDefinition;
import io.micronaut.core.io.service.SoftServiceLoader;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponseFactory;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.server.exceptions.InternalServerException;

/**
 * An implementation of the {@link HttpResponseFactory} case that retrieves the
 * response object from the current request bound to the current thread.
 *
 * @author graemerocher
 * @since 2.0.0
 */
public class ServerlessResponseFactory implements HttpResponseFactory {
    private static final HttpResponseFactory ALTERNATE;

    static {
        final SoftServiceLoader<HttpResponseFactory> factories = SoftServiceLoader.load(HttpResponseFactory.class);
        HttpResponseFactory alternate = null;
        for (ServiceDefinition<HttpResponseFactory> factory : factories) {
            if (factory.isPresent() && !factory.getName().equals(ServerlessResponseFactory.class.getName())) {
                alternate = factory.load();
                break;
            }
        }

        ALTERNATE = alternate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> MutableHttpResponse<T> ok(T body) {
        final HttpRequest<Object> req = ServerRequestContext.currentRequest().orElse(null);
        if (req instanceof ServerlessExchange) {
            final MutableHttpResponse response = ((ServerlessExchange) req).getResponse();
            return response.status(HttpStatus.OK).body(body);
        } else {
            if (ALTERNATE != null) {
                return ALTERNATE.ok(body);
            } else {
                throw new InternalServerException("No request present");
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> MutableHttpResponse<T> status(HttpStatus status, String reason) {
        final HttpRequest<Object> req = ServerRequestContext.currentRequest().orElse(null);
        if (req instanceof ServerlessExchange) {
            final MutableHttpResponse response = ((ServerlessExchange) req).getResponse();
            return response.status(status, reason);
        } else {
            if (ALTERNATE != null) {
                return ALTERNATE.status(status, reason);
            } else {
                throw new InternalServerException("No request present");
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> MutableHttpResponse<T> status(HttpStatus status, T body) {
        final HttpRequest<Object> req = ServerRequestContext.currentRequest().orElse(null);
        if (req instanceof ServerlessExchange) {
            final MutableHttpResponse response = ((ServerlessExchange) req).getResponse();
            return response.body(body).status(status);
        } else {
            if (ALTERNATE != null) {
                return ALTERNATE.status(status, body);
            } else {
                throw new InternalServerException("No request present");
            }
        }
    }
}
