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
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;
import io.micronaut.http.multipart.StreamingFileUpload;

import java.util.Optional;

/**
 * Binds unannotated streaming file upload arguments from Google multipart parts.
 */
@Internal
final class GoogleStreamingFileUploadBinder implements TypedRequestArgumentBinder<StreamingFileUpload> {

    @Override
    public Argument<StreamingFileUpload> argumentType() {
        return Argument.of(StreamingFileUpload.class);
    }

    @Override
    public BindingResult<StreamingFileUpload> bind(ArgumentConversionContext<StreamingFileUpload> context, HttpRequest<?> source) {
        if (source instanceof GoogleFunctionHttpRequest) {
            GoogleFunctionHttpRequest<?> googleRequest = (GoogleFunctionHttpRequest<?>) source;
            String partName = context.getArgument().getName();
            return () -> Optional.ofNullable(googleRequest.getNativeRequest().getParts().get(partName))
                    .flatMap(part -> GoogleMultipartSupport.bindFileUpload(context.getArgument(), partName, part, googleRequest.getIoExecutor()));
        }
        return BindingResult.UNSATISFIED;
    }
}
