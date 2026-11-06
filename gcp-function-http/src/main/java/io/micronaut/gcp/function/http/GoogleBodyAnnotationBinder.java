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
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.bind.binders.BodyArgumentBinder;
import io.micronaut.http.bind.binders.DefaultBodyAnnotationBinder;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.multipart.StreamingFileUpload;

import java.util.Optional;

/**
 * Binds completed file uploads from Google multipart parts and delegates other body binding.
 *
 * @param <T> The argument type
 */
@Internal
final class GoogleBodyAnnotationBinder<T> implements BodyArgumentBinder<T> {

    private final DefaultBodyAnnotationBinder<T> delegate;

    GoogleBodyAnnotationBinder(DefaultBodyAnnotationBinder<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public BindingResult<T> bind(ArgumentConversionContext<T> context, HttpRequest<?> source) {
        if (source instanceof GoogleFunctionHttpRequest && isFileUpload(context)) {
            GoogleFunctionHttpRequest<?> googleRequest = (GoogleFunctionHttpRequest<?>) source;
            String partName = context.getAnnotationMetadata()
                    .stringValue(Body.class)
                    .filter(value -> !value.isEmpty())
                    .orElse(context.getArgument().getName());
            Optional<T> fileUpload = Optional.ofNullable(googleRequest.getNativeRequest().getParts().get(partName))
                    .flatMap(part -> GoogleMultipartSupport.bindFileUpload(context.getArgument(), partName, part, googleRequest.getIoExecutor()));
            if (fileUpload.isPresent()) {
                return () -> fileUpload;
            }
        }
        return delegate.bind(context, source);
    }

    private boolean isFileUpload(ArgumentConversionContext<T> context) {
        Class<T> type = context.getArgument().getType();
        return CompletedFileUpload.class.isAssignableFrom(type) || StreamingFileUpload.class.isAssignableFrom(type);
    }
}
