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

import io.micronaut.http.MediaType;

import java.lang.annotation.*;

/**
 * Represents a pubsub topic to be used by classes annotated with {@link PubSubClient}.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Topic {

    /**
     * Set the name of the topic used to publish messages. Valid names are simple names such as "animals" or
     * FQN names such as {@code projects/<project_name>/topics/<topic_name>}
     * @return The name of the topic to publish messages to
     */
    String value();

    /**
     * Defines the Content-Type to be used for message serialization.
     * Defaults to application/json if not set by the user.
     * @return contentType to use
     */
    String contentType() default MediaType.APPLICATION_JSON;
}
