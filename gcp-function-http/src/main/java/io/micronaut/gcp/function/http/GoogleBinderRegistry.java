/*
 * Copyright 2017-2023 original authors
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

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.bind.binders.DefaultBodyAnnotationBinder;
import io.micronaut.servlet.http.ServletBinderRegistry;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.bind.DefaultRequestBinderRegistry;
import io.micronaut.http.bind.binders.RequestArgumentBinder;
import io.micronaut.http.codec.MediaTypeCodecRegistry;

import jakarta.inject.Singleton;
import java.util.List;

/**
 * Implementation of {@link ServletBinderRegistry} for Google.
 *
 * @param <T> The body type
 *
 * @author graemerocher
 * @since 1.2.0
 */
@Singleton
@Replaces(DefaultRequestBinderRegistry.class)
@Internal
class GoogleBinderRegistry<T> extends ServletBinderRegistry<T> {

    /**
     * Default constructor.
     *
     * @param mediaTypeCodecRegistry The media type codec registry
     * @param conversionService      The conversion service
     * @param binders                The binders
     * @param defaultBodyAnnotationBinder The delegate default body binder
     */
    GoogleBinderRegistry(
            MediaTypeCodecRegistry mediaTypeCodecRegistry,
            ConversionService conversionService,
            List<RequestArgumentBinder> binders,
            DefaultBodyAnnotationBinder<T> defaultBodyAnnotationBinder) {
        super(mediaTypeCodecRegistry, conversionService, binders, defaultBodyAnnotationBinder);
        this.byType.put(com.google.cloud.functions.HttpRequest.class, new GoogleRequestBinder());
        this.byType.put(com.google.cloud.functions.HttpResponse.class, new GoogleResponseBinder());
        this.byAnnotation.put(Part.class, new GooglePartBinder<T>(mediaTypeCodecRegistry));
    }
}
