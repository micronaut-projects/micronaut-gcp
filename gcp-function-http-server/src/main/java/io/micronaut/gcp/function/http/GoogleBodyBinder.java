package io.micronaut.gcp.function.http;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.io.IOUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.bind.binders.AnnotatedRequestArgumentBinder;
import io.micronaut.http.bind.binders.DefaultBodyAnnotationBinder;
import io.micronaut.http.codec.MediaTypeCodecRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Internal
class GoogleBodyBinder<T> extends DefaultBodyAnnotationBinder<T> implements AnnotatedRequestArgumentBinder<Body, T> {
    private final MediaTypeCodecRegistry mediaTypeCodeRegistry;

    GoogleBodyBinder(ConversionService<?> conversionService, MediaTypeCodecRegistry mediaTypeCodecRegistry) {
        super(conversionService);
        this.mediaTypeCodeRegistry = mediaTypeCodecRegistry;
    }

    @Override
    public Class<Body> getAnnotationType() {
        return Body.class;
    }

    @Override
    public BindingResult<T> bind(ArgumentConversionContext<T> context, HttpRequest<?> source) {
        if (source instanceof GoogleFunctionHttpRequest) {
            GoogleFunctionHttpRequest<?> functionHttpRequest = (GoogleFunctionHttpRequest<?>) source;
            final Class<T> type = context.getArgument().getType();
            if (CharSequence.class.isAssignableFrom(type)) {
                try (InputStream inputStream = functionHttpRequest.getInputStream()) {
                    final String content = IOUtils.readText(new BufferedReader(new InputStreamReader(inputStream, source.getCharacterEncoding())));
                    return () -> (Optional<T>) Optional.of(content);
                } catch (IOException e) {
                    return new BindingResult<T>() {
                        @Override
                        public Optional<T> getValue() {
                            return Optional.empty();
                        }

                        @Override
                        public List<ConversionError> getConversionErrors() {
                            return Collections.singletonList(
                                    () -> e
                            );
                        }
                    };
                }

            }
        }
        return super.bind(context, source);
    }
}
