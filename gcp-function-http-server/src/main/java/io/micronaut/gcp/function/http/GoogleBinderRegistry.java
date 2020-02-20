package io.micronaut.gcp.function.http;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.bind.DefaultRequestBinderRegistry;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.http.bind.binders.RequestArgumentBinder;
import io.micronaut.http.codec.MediaTypeCodecRegistry;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
@Replaces(DefaultRequestBinderRegistry.class)
class GoogleBinderRegistry implements RequestBinderRegistry {

    private static final String BINDABLE_ANN = Bindable.class.getName();
    private final DefaultRequestBinderRegistry defaultRegistry;
    private final Map<Class<? extends Annotation>, RequestArgumentBinder> byAnnotation = new LinkedHashMap<>(5);
    private final Map<Class<?>, RequestArgumentBinder> byType = new LinkedHashMap<>(5);

    public GoogleBinderRegistry(
            MediaTypeCodecRegistry mediaTypeCodecRegistry,
            ConversionService conversionService,
            List<RequestArgumentBinder> binders) {
        this.defaultRegistry = new DefaultRequestBinderRegistry(conversionService, binders);
        this.byAnnotation.put(Body.class, new GoogleBodyBinder(conversionService, mediaTypeCodecRegistry));
        this.byType.put(com.google.cloud.functions.HttpRequest.class, new GoogleRequestBinder());
        this.byType.put(com.google.cloud.functions.HttpResponse.class, new GoogleResponseBinder());
    }

    @Override
    public <T> Optional<ArgumentBinder<T, HttpRequest<?>>> findArgumentBinder(Argument<T> argument, HttpRequest<?> source) {
        final Class<? extends Annotation> annotation = argument.getAnnotationMetadata().getAnnotationTypeByStereotype(BINDABLE_ANN).orElse(null);
        if (annotation != null) {
            final RequestArgumentBinder binder = byAnnotation.get(annotation);
            if (binder != null) {
                return Optional.of(binder);
            }
        }

        final RequestArgumentBinder requestArgumentBinder = byType.get(argument.getType());
        if (requestArgumentBinder != null) {
            return Optional.of(requestArgumentBinder);
        } else {
            return this.defaultRegistry.findArgumentBinder(argument, source);
        }
    }
}
