package io.micronaut.gcp.function.http;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.servlet.http.ServletBinderRegistry;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.bind.DefaultRequestBinderRegistry;
import io.micronaut.http.bind.binders.RequestArgumentBinder;
import io.micronaut.http.codec.MediaTypeCodecRegistry;

import javax.inject.Singleton;
import java.util.List;

/**
 * Implementation of {@link ServletBinderRegistry} for Google.
 *
 * @author graemerocher
 * @since 1.2.0
 */
@Singleton
@Replaces(DefaultRequestBinderRegistry.class)
@Internal
class GoogleBinderRegistry extends ServletBinderRegistry {

    /**
     * Defautl constructor.
     *
     * @param mediaTypeCodecRegistry The media type codec registry
     * @param conversionService      The conversion service
     * @param binders                The binders
     */
    GoogleBinderRegistry(
            MediaTypeCodecRegistry mediaTypeCodecRegistry,
            ConversionService conversionService,
            List<RequestArgumentBinder> binders) {
        super(mediaTypeCodecRegistry, conversionService, binders);
        this.byType.put(com.google.cloud.functions.HttpRequest.class, new GoogleRequestBinder());
        this.byType.put(com.google.cloud.functions.HttpResponse.class, new GoogleResponseBinder());
        this.byAnnotation.put(Part.class, new GooglePartBinder(mediaTypeCodecRegistry));
    }
}
