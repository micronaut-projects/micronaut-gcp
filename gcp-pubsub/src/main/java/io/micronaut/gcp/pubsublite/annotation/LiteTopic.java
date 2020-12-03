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

import io.micronaut.http.MediaType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a pubsub lite topic to be used by classes annotated with {@link PubSubLiteClient}.
 *
 * Based on {@link io.micronaut.gcp.pubsub.annotation.Topic}.
 *
 * @author Jacob Mims
 * @since 2.2.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface LiteTopic {

    /**
     * A fully qualified topic name, properly formatted and including the project number.
     * ie., projects/<project_number>/locations/<location>/topics/<subscription_name>
     * @return The FQN of the topic.
     */
    String value() default "";

    /**
     * Set the name of the topic used to publish messages. An example of a name is "animals".
     * @return The name of the topic to publish messages to
     */
    String name() default "";

    /**
     * The location of the pubsub lite topic. An example of a valid location is "us-central1-a".
     * @return GCP location of the topic to publish messages to.
     */
    String location() default "us-central1-a";

    /**
     * Defines the Content-Type to be used for message serialization.
     * Defaults to application/json if not set by the user.
     * @return contentType to use
     */
    String contentType() default MediaType.APPLICATION_JSON;

    /**
     * Defines the name of a particular configuration used for a Publisher.
     * Configurations can be set via gcp.pubsub.subscriber.*
     * @return configuration name to use for {@link com.google.cloud.pubsub.v1.Publisher}
     *
     */
    String configuration() default "";
}
