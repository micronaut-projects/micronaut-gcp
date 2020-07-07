package io.micronaut.gcp.pubsub.support

import io.micronaut.aop.MethodInvocationContext
import io.micronaut.gcp.pubsub.intercept.PubSubClientIntroductionAdvice
import spock.lang.Specification

class PublisherIntroductionAdviceSpec extends Specification{

    void "client without annotation invoked"() {
        given:
            PublisherFactory factory = Mock()
            PubSubClientIntroductionAdvice advice = new PubSubClientIntroductionAdvice(factory)
            MethodInvocationContext<Object, Object> context = Mock()
        when:
            advice.intercept(context)
        then:
            1 * context.proceed()

    }
}

