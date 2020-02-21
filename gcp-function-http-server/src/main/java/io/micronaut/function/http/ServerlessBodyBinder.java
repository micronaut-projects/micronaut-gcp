package io.micronaut.function.http;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.io.IOUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.bind.binders.AnnotatedRequestArgumentBinder;
import io.micronaut.http.bind.binders.DefaultBodyAnnotationBinder;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.exceptions.HttpStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Allows binding the body from a {@link ServerlessHttpRequest}.
 *
 * @param <T> The body type
 * @author graemerocher
 * @since 2.0.0
 */
@Internal
final class ServerlessBodyBinder<T> extends DefaultBodyAnnotationBinder<T> implements AnnotatedRequestArgumentBinder<Body, T> {
    private final MediaTypeCodecRegistry mediaTypeCodeRegistry;

    /**
     * Default constructor.
     * @param conversionService The conversion service
     * @param mediaTypeCodecRegistry The codec registry
     */
    ServerlessBodyBinder(
            ConversionService<?> conversionService,
            MediaTypeCodecRegistry mediaTypeCodecRegistry) {
        super(conversionService);
        this.mediaTypeCodeRegistry = mediaTypeCodecRegistry;
    }

    @Override
    public Class<Body> getAnnotationType() {
        return Body.class;
    }

    @Override
    public BindingResult<T> bind(ArgumentConversionContext<T> context, HttpRequest<?> source) {
        final Class<T> type = context.getArgument().getType();
        if (source instanceof ServerlessHttpRequest) {
            ServerlessHttpRequest<?, ?> functionHttpRequest = (ServerlessHttpRequest<?, ?>) source;
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

            } else {
                final MediaTypeCodec codec = mediaTypeCodeRegistry.findCodec(source.getContentType().orElse(MediaType.APPLICATION_JSON_TYPE), type)
                        .orElse(null);

                if (codec != null) {
                    try (InputStream inputStream = functionHttpRequest.getInputStream()) {
                        T content = codec.decode(type, inputStream);
                        return () -> Optional.of(content);
                    } catch (CodecException | IOException e) {
                        throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Unable to decode request body: " + e.getMessage());
                    }
                }

            }
        }
        return super.bind(context, source);
    }
}
