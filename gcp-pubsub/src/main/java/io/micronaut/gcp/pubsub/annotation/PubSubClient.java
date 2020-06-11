package io.micronaut.gcp.pubsub.annotation;

import io.micronaut.aop.Introduction;
import io.micronaut.context.annotation.Type;
import io.micronaut.gcp.pubsub.intercept.PubSubClientIntroductionAdvice;

import javax.inject.Scope;
import javax.inject.Singleton;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Vinicius Carvalho
 * @since 2.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Scope
@Introduction
@Type(PubSubClientIntroductionAdvice.class)
@Singleton
public @interface PubSubClient {
    /**
     * @return Topic name to publish
     */
    String topic();

    /**
     * @return GCP Project Id, if null it will be inferred from the environment via {@link io.micronaut.gcp.GoogleCloudConfiguration}
     */
    String project();
}
