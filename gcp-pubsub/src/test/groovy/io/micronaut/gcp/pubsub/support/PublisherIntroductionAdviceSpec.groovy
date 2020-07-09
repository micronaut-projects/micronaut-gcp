package io.micronaut.gcp.pubsub.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.core.SettableApiFuture
import com.google.cloud.pubsub.v1.Publisher
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.context.annotation.Replaces
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.exception.PubSubClientException
import io.micronaut.gcp.pubsub.intercept.PubSubClientIntroductionAdvice
import io.micronaut.http.MediaType
import io.micronaut.messaging.annotation.Header
import io.micronaut.messaging.annotation.Headers
import io.micronaut.test.annotation.MicronautTest
import io.micronaut.test.annotation.MockBean
import io.reactivex.Single
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class PublisherIntroductionAdviceSpec extends Specification {

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

    void "method with invalid content type"(){
        when:
            pubSubClient.invalidMimeType("")
        then:
            def e = thrown(PubSubClientException)
            e.message.startsWith("Could not locate a valid SerDes implementation for type")
    }

    void "skip SerDes"() {

    }

    void "publish without return"() {
        Person person = new Person()
        person.name = "alf"
        byte[] serialized = objectMapper.writeValueAsBytes(person)
        when:
            pubSubClient.send(person)
        then:
            def pubSubMessage = (PubsubMessage)DataHolder.getInstance().getData()
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

    void "with default headers"() {
        Person person = new Person()
        person.name = "alf"
        when:
            pubSubClient.sendAndWait(person)
        then:
            def pubSubMessage = (PubsubMessage)DataHolder.getInstance().getData()
            pubSubMessage.getAttributesMap().size() == 2
    }

    void "extra headers on method"() {
        Person person = new Person()
        person.name = "alf"
        when:
            pubSubClient.withExtraHeaders(person)
        then:
            def pubSubMessage = (PubsubMessage)DataHolder.getInstance().getData()
            pubSubMessage.getAttributesMap().size() == 3
    }



    @MockBean
    @Replaces(PublisherFactory)
    PublisherFactory publisherFactory() {
        def factory = Mock(PublisherFactory)
        def publisher = Mock(Publisher)
        def future = new SettableApiFuture<String>()
        future.set("1234")
        publisher.publish(_) >> { PubsubMessage message -> DataHolder.getInstance().setData(message); return future; }
        factory.createPublisher(_) >> publisher
        return factory
    }

}

@PubSubClient
@Headers(  @Header(name = "x-client-type", value = "test")  )
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

    @Topic(value = "testTopic", contentType = MediaType.APPLICATION_ATOM_XML)
    void invalidMimeType(Object data)

    @Topic(value="testTopic")
    @Headers(  @Header(name = "x-header-added", value = "foo")  )
    String withExtraHeaders(Object data)

}

class Person {
    String name;
}