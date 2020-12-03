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
package io.micronaut.gcp.pubsublite.bind;

import io.micronaut.core.bind.annotation.AnnotatedArgumentBinder;

import java.lang.annotation.Annotation;

/**
 * An interface for PubSub argument binding based on an annotation.
 *
 * Based on {@link io.micronaut.gcp.pubsub.bind.PubSubAnnotatedArgumentBinder}
 *
 * @param <A> The annotation that must exist on the argument
 * @author Jacob Mims
 * @since 2.2.0
 */
public interface PubSubLiteAnnotatedArgumentBinder<A extends Annotation> extends
        AnnotatedArgumentBinder<A, Object, PubSubLiteConsumerState>, PubSubLiteArgumentBinder<Object> {
}
