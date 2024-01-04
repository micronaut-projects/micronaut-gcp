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
