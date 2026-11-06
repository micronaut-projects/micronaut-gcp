/*
 * Copyright 2017-2026 original authors
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
import io.micronaut.core.io.buffer.ByteArrayBufferFactory;
import io.micronaut.core.io.buffer.ReadBufferFactory;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.body.CloseableByteBody;
import io.micronaut.http.body.stream.InputStreamByteBody;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.multipart.FormFieldMetadata;
import io.micronaut.http.multipart.RawFormField;
import io.micronaut.http.multipart.StreamingFileUpload;

import com.google.cloud.functions.HttpRequest.HttpPart;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.Executor;

/**
 * Utilities for adapting Google multipart parts to Micronaut multipart types.
 */
@Internal
final class GoogleMultipartSupport {

    private GoogleMultipartSupport() {
    }

    static <T> Optional<T> bindFileUpload(Argument<T> argument, String partName, HttpPart part, Executor executor) {
        Class<T> type = argument.getType();
        if (CompletedFileUpload.class.isAssignableFrom(type)) {
            CompletedFileUpload fileUpload = completedFileUpload(partName, part);
            //noinspection unchecked
            return (Optional<T>) Optional.of(fileUpload);
        }
        if (StreamingFileUpload.class.isAssignableFrom(type)) {
            StreamingFileUpload fileUpload = streamingFileUpload(partName, part, executor);
            //noinspection unchecked
            return (Optional<T>) Optional.of(fileUpload);
        }
        return Optional.empty();
    }

    static CompletedFileUpload completedFileUpload(String partName, HttpPart part) {
        FormFieldMetadata metadata = formFieldMetadata(partName, part);
        try (InputStream inputStream = part.getInputStream()) {
            return CompletedFileUpload.ofMemory(metadata, ReadBufferFactory.getJdkFactory().copyOf(inputStream));
        } catch (IOException e) {
            throw unreadablePart(partName, e);
        }
    }

    static StreamingFileUpload streamingFileUpload(String partName, HttpPart part, Executor executor) {
        try {
            CloseableByteBody byteBody = InputStreamByteBody.create(
                    part.getInputStream(),
                    definedSize(part),
                    executor,
                    ByteArrayBufferFactory.INSTANCE
            );
            return new StreamingFileUpload(new RawFormField(formFieldMetadata(partName, part), byteBody), executor);
        } catch (IOException e) {
            throw unreadablePart(partName, e);
        }
    }

    private static FormFieldMetadata formFieldMetadata(String partName, HttpPart part) {
        MediaType contentType = part.getContentType().map(MediaType::of).orElse(null);
        return new FormFieldMetadata(partName, part.getFileName().orElse(null), contentType);
    }

    private static OptionalLong definedSize(HttpPart part) {
        long contentLength = part.getContentLength();
        return contentLength >= 0 ? OptionalLong.of(contentLength) : OptionalLong.empty();
    }

    private static HttpStatusException unreadablePart(String partName, IOException e) {
        return new HttpStatusException(
                HttpStatus.BAD_REQUEST,
                "Unable to read part [" + partName + "]: " + e.getMessage()
        );
    }
}
