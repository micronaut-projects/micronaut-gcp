package io.micronaut.gcp.pubsub.support

import io.micronaut.aop.MethodInvocationContext
import io.micronaut.context.annotation.Replaces
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.intercept.PubSubClientIntroductionAdvice
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class PublisherIntroductionAdviceSpec extends Specification {

    @Inject
    PubSubClientIntroductionAdvice advice

    @Inject
    TestPubSubClient pubSubClient

    void "client without annotation invoked"() {
        given:
            MethodInvocationContext<Object, Object> context = Mock()
        when:
            advice.intercept(context)
        then:
            1 * context.proceed()

    }

    void "null body error test"() {
        given:
            Object data = null;
        when:
            pubSubClient.send(data);
        then:
            println("");
    }


    @MockBean
    @Replaces(PublisherFactory)
    PublisherFactory publisherFactory() {
        return Mock(PublisherFactory)
    }




}

@PubSubClient
interface TestPubSubClient {
    @Topic("testTopic")
    void send(Object data)
}
