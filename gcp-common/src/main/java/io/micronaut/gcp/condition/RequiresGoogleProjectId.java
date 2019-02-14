package io.micronaut.gcp.condition;

import io.micronaut.context.annotation.Requires;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
@Requires(condition = RequiresProjectIdCondition.class)
public @interface RequiresGoogleProjectId {
}
