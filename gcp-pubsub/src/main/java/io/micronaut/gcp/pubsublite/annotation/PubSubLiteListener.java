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

import io.micronaut.messaging.annotation.MessageListener;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a bean will be consuming PubSub Messages.
 *
 * Based on {@link io.micronaut.gcp.pubsub.annotation.PubSubListener}.
 *
 * @author Jacob Mims
 * @since 2.2.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@MessageListener
public @interface PubSubLiteListener {
    /**
     * Project number of the subscriptions the listener will utilize.
     * If null it will be inferred from the environment via {@link io.micronaut.gcp.GoogleCloudConfiguration}
     * @return the project number
     */
    long projectNumber() default 0;
}
