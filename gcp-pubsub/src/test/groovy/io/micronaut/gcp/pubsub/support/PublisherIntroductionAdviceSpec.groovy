package io.micronaut.gcp.pubsub.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.AbstractPublisherSpec
import io.micronaut.gcp.pubsub.DataHolder
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.exception.PubSubClientException
import io.micronaut.gcp.pubsub.intercept.PubSubClientIntroductionAdvice
import io.micronaut.messaging.annotation.Header
import io.micronaut.messaging.annotation.Headers
import io.micronaut.test.annotation.MicronautTest
import io.reactivex.Single

import javax.inject.Inject

@MicronautTest
@Property(name = "spec.name", value = "PublisherIntroductionAdviceSpec")
class PublisherIntroductionAdviceSpec extends AbstractPublisherSpec {

    @Inject
    PubSubClientIntroductionAdvice advice

    @Inject
    TestPubSubClient pubSubClient

    @Inject
    ObjectMapper objectMapper;

    void "client without annotation invoked"() {
        given:
            MethodInvocationContext<Object, Object> context = Mock()
        when:
            advice.intercept(context)
        then:
            1 * context.proceed()

    }

    void "method without arguments"() {
        when:
            pubSubClient.invalidMethod();
        then:
            def e = thrown(PubSubClientException)
            e.message.startsWith("No valid message body argument found for method")
    }

    void "publish without return"() {
        Person person = new Person()
        person.name = "alf"
        byte[] serialized = objectMapper.writeValueAsBytes(person)
        when:
            pubSubClient.send(person)
        then:
            def pubSubMessage = (PubsubMessage) DataHolder.getInstance().getData()
            pubSubMessage.getData().toByteArray() == serialized
    }

    void "publish with valid return"() {
        Person person = new Person()
        person.name = "alf"
        expect:
            pubSubClient.sendAndWait(person) == "1234"
    }

    void "reactive publish with valid return"() {
        Person person = new Person()
        person.name = "alf"
        expect:
            pubSubClient.reactiveSend(person).blockingGet() == "1234"
    }

}

@PubSubClient
@Headers(  @Header(name = "x-client-type", value = "test")  )
@Requires(property = "spec.name", value = "PublisherIntroductionAdviceSpec")
interface TestPubSubClient {
    @Topic(value = "testTopic", contentType = "application/json")
    void send(Object data)

    @Topic(value = "testTopic", contentType = "application/json")
    String sendAndWait(Object data)

    @Topic(value = "testTopic", contentType = "application/json")
    Single<String> reactiveSend(Object data)

    @Topic("testTopic")
    void sendRaw(byte[] data)

    @Topic(value="testTopic")
    void invalidMethod()

    @Topic(value="testTopic")
    @Headers(  @Header(name = "x-header-added", value = "foo")  )
    String withExtraHeaders(Object data)

}

class Person {
    String name;
}