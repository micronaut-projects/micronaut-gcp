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

import java.lang.annotation.*;

/**
 * Represents a PubSub subscription. Methods annotated with this annotation
 * will be invoked when a new message is received by a {@link com.google.cloud.pubsub.v1.MessageReceiver}
 * that is bound to the annotated method.
 *
 * @author Vinicius Carvalho
 * @since 2.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Subscription {
    /**
     * The name of the subscription, it could be a simple name such as "animals" or
     * a FQN such as {@code projects/<project_name>/subscriptions/<subscription_name>}.
     * @return the subscription name
     */
    String value();

    /**
     * Defines the Content-Type to be used for message deserialization.
     * There's no default, if not set and the message does not contain a Content-Type header deserialization will fail.
     * @return contentType to use
     */
    String contentType() default "";

    /**
     * Defines the name of a particular configuration used for a Subscriber.
     * Configurations can be set via gcp.pubsub.subscriber.*
     * @return configuration name to use for {@link com.google.cloud.pubsub.v1.Subscriber}
     *
     */
    String configuration() default "";
}
