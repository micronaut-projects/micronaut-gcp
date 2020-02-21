package io.micronaut.function.http;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.io.IOUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class ServerlessRequestBinder implements TypedRequestArgumentBinder<HttpRequest> {

    private final MediaTypeCodecRegistry mediaTypeCodecRegistry;

    public ServerlessRequestBinder(MediaTypeCodecRegistry mediaTypeCodecRegistry) {
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
                        } catch (IOException e) {
                            throw new CodecException("Error decoding request body: " + e.getMessage(), e);
                        }
                    }
                }

            }

        }
        return () -> Optional.of(source);
    }
}
