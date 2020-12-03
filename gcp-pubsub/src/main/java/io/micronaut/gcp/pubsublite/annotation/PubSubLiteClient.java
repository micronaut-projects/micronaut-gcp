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
package io.micronaut.gcp.pubsublite.annotation;

import io.micronaut.aop.Introduction;
import io.micronaut.context.annotation.Type;
import io.micronaut.gcp.pubsublite.intercept.PubSubLiteClientIntroductionAdvice;

import javax.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Client for a PubSub Lite Topic.
 *
 * Based on {@link io.micronaut.gcp.pubsub.annotation.PubSubClient}
 *
 * @author Jacob Mims
 * @since 2.2.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Introduction
@Type(PubSubLiteClientIntroductionAdvice.class)
@Singleton
public @interface PubSubLiteClient {
    /**
     * Project number of the client the listener will utilize.
     * If null it will be inferred from the environment via {@link io.micronaut.gcp.GoogleCloudConfiguration}
     * @return the project number
     */
    long projectNumber() default 0;
}
