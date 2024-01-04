package io.micronaut.gcp.pubsub.push;

import jakarta.validation.Constraint;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
@Constraint(validatedBy = { PushMessageValidator.class })
public @interface ValidPushMessage {

    String message() default "invalid pubsub push request message ({validatedValue}) - message must contain either a non-empty data field or at least one attribute";
}
