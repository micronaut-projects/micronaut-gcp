package io.micronaut.gcp.function.http;

import com.google.cloud.functions.HttpResponse;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;

import java.util.Optional;

/**
 * Request binder for the Google HttpResponse object.
 *
 * @author graemerocher
 * @since 1.2.0
 */
@Internal
final class GoogleResponseBinder implements TypedRequestArgumentBinder<HttpResponse> {

    static final Argument<HttpResponse> TYPE = Argument.of(HttpResponse.class);

    @Override
    public Argument<HttpResponse> argumentType() {
        return TYPE;
    }

    @Override
    public BindingResult<HttpResponse> bind(ArgumentConversionContext<HttpResponse> context, HttpRequest<?> source) {
        if (source instanceof GoogleFunctionHttpRequest) {
            GoogleFunctionHttpRequest googleFunctionHttpRequest = (GoogleFunctionHttpRequest) source;
            return () -> Optional.of(googleFunctionHttpRequest.getGoogleResponse().getNativeResponse());
        }
        return BindingResult.UNSATISFIED;
    }
}
