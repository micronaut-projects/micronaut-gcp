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
package io.micronaut.gcp.pubsub.intercept;

import com.google.cloud.pubsub.v1.Publisher;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.gcp.pubsub.annotation.PubSubClient;
import io.micronaut.gcp.pubsub.annotation.Topic;
import io.micronaut.gcp.pubsub.support.PublisherFactory;

import javax.inject.Singleton;

/**
 *
 * @author Vinicius Carvalho
 * @since 2.0
 */
@Singleton
public class PubSubClientIntroductionAdvice implements MethodInterceptor<Object, Object> {

    private final PublisherFactory publisherFactory;

    public PubSubClientIntroductionAdvice(PublisherFactory publisherFactory) {
        this.publisherFactory = publisherFactory;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {

        if (context.hasAnnotation(Topic.class)) {
            AnnotationValue<Topic> topic = context.getAnnotation(Topic.class);
            return null;
        } else {
            return context.proceed();
        }

    }
}
