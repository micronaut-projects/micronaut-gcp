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
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.io.IOUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.bind.binders.AnnotatedRequestArgumentBinder;
import com.google.cloud.functions.HttpRequest.HttpPart;
import io.micronaut.http.body.MessageBodyHandlerRegistry;
import io.micronaut.http.body.MessageBodyReader;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.simple.SimpleHttpHeaders;

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

    private final MessageBodyHandlerRegistry messageBodyHandlerRegistry;
    private final ConversionService conversionService;

    /**
     * Default constructor.
     * @param messageBodyHandlerRegistry The message body handler registry
     * @param conversionService          The conversion service
     */
    GooglePartBinder(MessageBodyHandlerRegistry messageBodyHandlerRegistry,
                     ConversionService conversionService) {
        this.messageBodyHandlerRegistry = messageBodyHandlerRegistry;
        this.conversionService = conversionService;
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
                    MessageBodyReader<T> reader = null;
                    if (contentType != null) {
                        reader = messageBodyHandlerRegistry.findReader(argument, contentType).orElse(null);
                    }
                    if (reader == null) {
                        reader = messageBodyHandlerRegistry.findReader(argument).orElse(null);
                    }
                    if (reader != null) {
                        try (InputStream inputStream = part.getInputStream()) {
                            final SimpleHttpHeaders headers = new SimpleHttpHeaders(conversionService);
                            part.getHeaders().forEach((header, values) -> {
                                if (values != null) {
                                    values.forEach(value -> headers.add(header, value));
                                }
                            });
                            final T content = reader.read(argument, contentType, headers, inputStream);
                            return () -> Optional.ofNullable(content);
                        } catch (IOException | CodecException e) {
                            throw new HttpStatusException(
                                    HttpStatus.BAD_REQUEST,
                                    "Unable to read part [" + partName + "]: " + e.getMessage()
                            );
                        }
                    }
                }
            }
        }
        return BindingResult.UNSATISFIED;
    }
}
