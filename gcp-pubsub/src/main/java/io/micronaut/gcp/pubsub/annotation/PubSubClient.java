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
package io.micronaut.gcp.pubsub.annotation;

import io.micronaut.aop.Introduction;
import io.micronaut.context.annotation.Type;
import io.micronaut.gcp.pubsub.intercept.PubSubClientIntroductionAdvice;

import javax.inject.Singleton;
import java.lang.annotation.*;

/**
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Introduction
@Type(PubSubClientIntroductionAdvice.class)
@Singleton
public @interface PubSubClient {
     /**
     * @return GCP Project Id, if null it will be inferred from the environment via {@link io.micronaut.gcp.GoogleCloudConfiguration}
     */
    String project() default "";



}
