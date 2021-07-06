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

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.context.annotation.Executable;
import io.micronaut.http.MediaType;
import io.micronaut.messaging.annotation.MessageMapping;

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
@Executable
public @interface Topic {

    /**
     * Set the name of the topic used to publish messages. Valid names are simple names such as "animals" or
     * FQN names such as {@code projects/<project_name>/topics/<topic_name>}
     * @return The name of the topic to publish messages to
     */
    @AliasFor(annotation = MessageMapping.class, member = "value")
    String value();

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

    /**
     * Sets the endpoint that PubSub will use to store messages.
     * If not specified PubSub stores messages on the nearest endpoint to the client.
     * This is useful when regulations for data locality such as GDPR are in place.
     * @return the remote endpoint to use
     */
    String endpoint() default "";
}
