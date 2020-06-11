package io.micronaut.gcp.pubsub.intercept;

import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;

import javax.inject.Singleton;

@Singleton
/**
 *
 * @author Vinicius Carvalho
 * @since 2.0
 */
public class PubSubClientIntroductionAdvice implements MethodInterceptor<Object, Object> {
    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        return null;
    }
}
