/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.gcp.function.http;

import com.google.cloud.functions.HttpRequest;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.bind.binders.TypedRequestArgumentBinder;

import java.util.Optional;

/**
 * Request binder for the Google HttpRequest object.
 *
 * @author graemerocher
 * @since 1.2.0
 */
@Internal
final class GoogleRequestBinder implements TypedRequestArgumentBinder<HttpRequest> {

    private static final Argument<HttpRequest> TYPE = Argument.of(HttpRequest.class);

    @Override
    public Argument<HttpRequest> argumentType() {
        return TYPE;
    }

    @Override
    public BindingResult<HttpRequest> bind(ArgumentConversionContext<HttpRequest> context, io.micronaut.http.HttpRequest<?> source) {
        if (source instanceof GoogleFunctionHttpRequest) {
            GoogleFunctionHttpRequest googleFunctionHttpRequest = (GoogleFunctionHttpRequest) source;
            return () -> Optional.of(googleFunctionHttpRequest.getNativeRequest());
        }
        return BindingResult.UNSATISFIED;
    }
}
