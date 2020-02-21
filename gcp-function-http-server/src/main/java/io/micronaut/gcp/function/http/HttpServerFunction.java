package io.micronaut.gcp.function.http;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.env.Environment;
import io.micronaut.function.http.DefaultServerlessExchange;
import io.micronaut.function.http.ServerlessExchange;
import io.micronaut.function.http.ServerlessHttpHandler;

import javax.annotation.Nonnull;

/**
 * Entry point into the Micronaut + GCP integration.
 *
 * @author graemerocher
 * @since 1.2.0
 */
public class HttpServerFunction extends ServerlessHttpHandler<HttpRequest, HttpResponse> implements HttpFunction {


    @Override
    protected ServerlessExchange<HttpRequest, HttpResponse> createExchange(HttpRequest request, HttpResponse response) {
        final GoogleFunctionHttpResponse<Object> res =
                new GoogleFunctionHttpResponse<>(response, getMediaTypeCodecRegistry());
        final GoogleFunctionHttpRequest<Object> req =
                new GoogleFunctionHttpRequest<>(request, res, getMediaTypeCodecRegistry());

        return new DefaultServerlessExchange<>(req, res);
    }


    @Nonnull
    @Override
    protected ApplicationContextBuilder newApplicationContextBuilder() {
        final ApplicationContextBuilder builder = super.newApplicationContextBuilder();
        builder.deduceEnvironment(false);
        builder.environments(Environment.GOOGLE_COMPUTE);
        return builder;
    }
}
