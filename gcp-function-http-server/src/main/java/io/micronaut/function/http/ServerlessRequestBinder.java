package io.micronaut.function.http;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.io.IOUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.exceptions.HttpStatusException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * A {@link io.micronaut.http.bind.binders.RequestArgumentBinder} that can bind the HTTP request
 * for a {@link ServerlessHttpRequest} including resolving any type arguments for the body.
 *
 * @author graemerocher
 * @since 2.0.0
 */
@Internal
class ServerlessRequestBinder implements TypedRequestArgumentBinder<HttpRequest> {

    private final MediaTypeCodecRegistry mediaTypeCodecRegistry;

    /**
     * Default constructor.
     *
     * @param mediaTypeCodecRegistry The media type code registry
     */
    ServerlessRequestBinder(MediaTypeCodecRegistry mediaTypeCodecRegistry) {
        this.mediaTypeCodecRegistry = mediaTypeCodecRegistry;
    }

    @Override
    public Argument<HttpRequest> argumentType() {
        return Argument.of(HttpRequest.class);
    }

    @Override
    public BindingResult<HttpRequest> bind(ArgumentConversionContext<HttpRequest> context, HttpRequest<?> source) {
        if (source instanceof ServerlessHttpRequest) {
            ServerlessHttpRequest<?, ?> serverlessHttpRequest = (ServerlessHttpRequest<?, ?>) source;
            final Argument<?> bodyType = context.getArgument().getFirstTypeVariable().orElse(null);
            if (bodyType != null) {
                final Class<?> bodyJavaType = bodyType.getType();
                if (CharSequence.class.isAssignableFrom(bodyJavaType)) {
                    try (BufferedReader reader = serverlessHttpRequest.getReader()) {
                        final String content = IOUtils.readText(reader);
                        return () -> Optional.of(
                                new ServerlessRequestAndBody(serverlessHttpRequest, content)
                        );
                    } catch (IOException e) {
                        return () -> Optional.of(
                                new ServerlessRequestAndBody(serverlessHttpRequest, "")
                        );
                    }
                } else {

                    final MediaType mediaType = source.getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
                    final MediaTypeCodec codec = mediaTypeCodecRegistry.findCodec(mediaType, bodyJavaType)
                            .orElse(null);

                    if (codec != null) {
                        try (InputStream reader = serverlessHttpRequest.getInputStream()) {
                            final Object result = codec.decode(bodyType, reader);
                            return () -> Optional.of(
                                    new ServerlessRequestAndBody(serverlessHttpRequest, result)
                            );
                        } catch (IOException | CodecException e) {
                            if (ServerlessHttpHandler.LOG.isDebugEnabled()) {
                                ServerlessHttpHandler.LOG.debug("Error decoding media type: " + mediaType, e);
                            }
                            throw new HttpStatusException(HttpStatus.BAD_REQUEST, "Error decoding request body: " + e.getMessage());
                        }
                    }
                }

            }

        }
        return () -> Optional.of(source);
    }
}
