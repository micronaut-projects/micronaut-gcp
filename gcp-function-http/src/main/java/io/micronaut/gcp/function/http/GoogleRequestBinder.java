package io.micronaut.gcp.function.http;

import com.google.cloud.functions.HttpRequest;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;

import java.util.Optional;

/**
 * Request binder for the Google HttpRequest object.
 *
 * @author graemerocher
 * @since 1.2.0
 */
@Internal
final class GoogleRequestBinder implements TypedRequestArgumentBinder<HttpRequest> {

    private static final Argument<HttpRequest> TYPE = Argument.of(HttpRequest.class);

    @Override
    public Argument<HttpRequest> argumentType() {
        return TYPE;
    }

    @Override
    public BindingResult<HttpRequest> bind(ArgumentConversionContext<HttpRequest> context, io.micronaut.http.HttpRequest<?> source) {
        if (source instanceof GoogleFunctionHttpRequest) {
            GoogleFunctionHttpRequest googleFunctionHttpRequest = (GoogleFunctionHttpRequest) source;
            return () -> Optional.of(googleFunctionHttpRequest.getNativeRequest());
        }
        return BindingResult.UNSATISFIED;
    }
}
