/*
 * Copyright 2017-2024 original authors
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

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Executable;
import io.micronaut.messaging.annotation.MessageMapping;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
@Executable
public @interface PushSubscription {

    /**
     * The name of the subscription, it could be a simple name such as "animals" or
     * a FQN such as {@code projects/<project_name>/subscriptions/<subscription_name>}.
     * @return the subscription name
     */
    @AliasFor(annotation = MessageMapping.class, member = "value")
    String value();

    /**
     * Defines the Content-Type to be used for message deserialization.
     * Defaults to application/json.
     * @return contentType to use
     */
    String contentType() default "application/json";
}
