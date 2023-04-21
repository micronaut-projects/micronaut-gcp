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
package io.micronaut.gcp.pubsub.bind;

import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.ArgumentBinderRegistry;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;

import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Singleton
public class PubSubBinderRegistry implements ArgumentBinderRegistry<PubSubConsumerState> {

    private final PubSubDefaultArgumentBinder defaultBinder;
    private final Map<Class<? extends Annotation>, ArgumentBinder<?, PubSubConsumerState>> byAnnotation = new HashMap<>();
    private final Map<Integer, ArgumentBinder<?, PubSubConsumerState>> byType = new HashMap<>();

    /**
     * Default constructor.
     * @param defaultBinder The default binder to be used if there's no binder is found
     * @param binders List of registered {@link PubSubArgumentBinder} binders
     */
    public PubSubBinderRegistry(PubSubDefaultArgumentBinder defaultBinder,
                                PubSubArgumentBinder<?>... binders) {
        this.defaultBinder = defaultBinder;
        if (ArrayUtils.isNotEmpty(binders)) {
            for (PubSubArgumentBinder<?> binder : binders) {
                if (binder instanceof PubSubAnnotatedArgumentBinder<?> annotatedArgumentBinder) {
                    byAnnotation.putIfAbsent(annotatedArgumentBinder.getAnnotationType(), binder);
                } else if (binder instanceof PubSubTypeArgumentBinder<?> typeBinder) {
                    byType.put(typeBinder.argumentType().typeHashCode(), typeBinder);
                }
            }
        }
    }

    @Override
    public <T> Optional<ArgumentBinder<T, PubSubConsumerState>> findArgumentBinder(Argument<T> argument) {
        Optional<Class<? extends Annotation>> opt = argument.getAnnotationMetadata().getAnnotationTypeByStereotype(Bindable.class);
        if (opt.isPresent()) {
            Class<? extends Annotation> annotationType = opt.get();
            ArgumentBinder binder = byAnnotation.get(annotationType);
            if (binder != null) {
                return Optional.of(binder);
            }
        } else {
            ArgumentBinder binder = byType.get(argument.typeHashCode());
            if (binder != null) {
                return Optional.of(binder);
            }
        }
        return Optional.of((ArgumentBinder<T, PubSubConsumerState>) defaultBinder);
    }
}
