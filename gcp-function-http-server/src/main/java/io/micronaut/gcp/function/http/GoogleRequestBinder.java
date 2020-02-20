package io.micronaut.gcp.function.http;

import com.google.cloud.functions.HttpRequest;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;

import java.util.Optional;

public class GoogleRequestBinder implements TypedRequestArgumentBinder<HttpRequest> {

    public static final Argument<HttpRequest> TYPE = Argument.of(HttpRequest.class);

    @Override
    public Argument<HttpRequest> argumentType() {
        return TYPE;
    }

    @Override
    public BindingResult<HttpRequest> bind(ArgumentConversionContext<HttpRequest> context, io.micronaut.http.HttpRequest<?> source) {
        if (source instanceof GoogleFunctionHttpRequest) {
            GoogleFunctionHttpRequest googleFunctionHttpRequest = (GoogleFunctionHttpRequest) source;
            return () -> Optional.of(googleFunctionHttpRequest.getGoogleRequest());
        }
        return BindingResult.UNSATISFIED;
    }
}
