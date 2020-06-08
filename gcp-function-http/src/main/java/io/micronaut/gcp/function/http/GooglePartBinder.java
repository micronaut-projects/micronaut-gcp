/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.gcp.function.http;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.io.IOUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.bind.binders.AnnotatedRequestArgumentBinder;
import com.google.cloud.functions.HttpRequest.HttpPart;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.exceptions.HttpStatusException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Handles binding multipart requests using {@link Part}.
 *
 * @author graemerocher
 * @since 1.2.0
 * @param <T> The argument type
 */
@Internal
final class GooglePartBinder<T> implements AnnotatedRequestArgumentBinder<Part, T> {

    private final MediaTypeCodecRegistry codecRegistry;

    /**
     * Default constructor.
     * @param codecRegistry The codec registry.
     */
    GooglePartBinder(MediaTypeCodecRegistry codecRegistry) {
        this.codecRegistry = codecRegistry;
    }

    @Override
    public Class<Part> getAnnotationType() {
        return Part.class;
    }

    @Override
    public BindingResult<T> bind(ArgumentConversionContext<T> context, HttpRequest<?> source) {
        if (source instanceof GoogleFunctionHttpRequest) {
            GoogleFunctionHttpRequest<?> googleRequest = (GoogleFunctionHttpRequest<?>) source;
            final com.google.cloud.functions.HttpRequest nativeRequest = googleRequest.getNativeRequest();
            final Argument<T> argument = context.getArgument();
            final String partName = context.getAnnotationMetadata().stringValue(Part.class).orElse(argument.getName());
            final HttpPart part = nativeRequest.getParts().get(partName);
            if (part != null) {
                final Class<T> type = argument.getType();
                if (HttpPart.class.isAssignableFrom(type)) {
                    //noinspection unchecked
                    return () -> (Optional<T>) Optional.of(part);
                } else if (String.class.isAssignableFrom(type)) {
                    try (BufferedReader reader = part.getReader()) {
                        final String content = IOUtils.readText(reader);
                        return () -> (Optional<T>) Optional.of(content);
                    } catch (IOException e) {
                        throw new HttpStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Unable to read part [" + partName + "]: " + e.getMessage()
                        );
                    }
                } else if (byte[].class.isAssignableFrom(type)) {
                    try (InputStream is = part.getInputStream()) {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                        int nRead;
                        byte[] data = new byte[16384];

                        while ((nRead = is.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }
                        final byte[] content = buffer.toByteArray();
                        return () -> (Optional<T>) Optional.of(content);
                    } catch (IOException e) {
                        throw new HttpStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Unable to read part [" + partName + "]: " + e.getMessage()
                        );
                    }

                } else {
                    final MediaType contentType = part.getContentType().map(MediaType::new)
                            .orElse(null);
                    if (contentType != null) {
                        final MediaTypeCodec codec = codecRegistry.findCodec(contentType, type).orElse(null);
                        if (codec != null) {
                            try (InputStream inputStream = part.getInputStream()) {
                                final T content = codec.decode(argument, inputStream);
                                return () -> (Optional<T>) Optional.of(content);
                            } catch (IOException e) {
                                throw new HttpStatusException(
                                        HttpStatus.BAD_REQUEST,
                                        "Unable to read part [" + partName + "]: " + e.getMessage()
                                );
                            }
                        }
                    }
                }
            }
        }
        return BindingResult.UNSATISFIED;
    }
}
