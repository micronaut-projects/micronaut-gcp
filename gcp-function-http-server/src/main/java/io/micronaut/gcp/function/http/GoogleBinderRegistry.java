package io.micronaut.gcp.function.http;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.function.http.ServerlessBinderRegistry;
import io.micronaut.http.bind.DefaultRequestBinderRegistry;
import io.micronaut.http.bind.binders.RequestArgumentBinder;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Replaces(DefaultRequestBinderRegistry.class)
public class GoogleBinderRegistry extends ServerlessBinderRegistry {
    public GoogleBinderRegistry(MediaTypeCodecRegistry mediaTypeCodecRegistry, ConversionService conversionService, List<RequestArgumentBinder> binders) {
        super(mediaTypeCodecRegistry, conversionService, binders);
        this.byType.put(com.google.cloud.functions.HttpRequest.class, new GoogleRequestBinder());
        this.byType.put(com.google.cloud.functions.HttpResponse.class, new GoogleResponseBinder());
    }
}
