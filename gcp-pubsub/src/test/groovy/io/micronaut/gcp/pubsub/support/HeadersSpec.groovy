package io.micronaut.gcp.pubsub.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.AbstractPublisherSpec
import io.micronaut.gcp.pubsub.DataHolder
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.messaging.annotation.MessageHeader
import io.micronaut.test.extensions.spock.annotation.MicronautTest

import javax.inject.Inject

@MicronautTest
@Property(name = "spec.name", value = "HeadersSpec")
@Property(name = "gcp.projectId", value = "test-project")
class HeadersSpec extends AbstractPublisherSpec {

    @Inject
    ClientWithoutHeaders clientWithoutHeaders

    @Inject
    ObjectMapper objectMapper

    void "client with no extra headers"() {
        Person person = new Person()
        person.name = "alf"
        when:
            clientWithoutHeaders.send(person)
        then:
            def pubsubMessage = (PubsubMessage)DataHolder.getInstance().getData()
            pubsubMessage.getAttributesMap().size() == 1

    }

    void "method with extra headers"(){
        Person person = new Person()
        person.name = "alf"
        when:
            clientWithoutHeaders.sendWithExtraHeaders(person)
        then:
            def pubsubMessage = (PubsubMessage)DataHolder.getInstance().getData()
            pubsubMessage.getAttributesMap().size() == 2
    }

    void "method with dynamic header value"(){
        Person person = new Person()
        person.name = "alf"
        def serialized = objectMapper.writeValueAsBytes(person)
        when:
            clientWithoutHeaders.sendWithDynamicHeaderValue(person, "foo")
        then:
            def pubsubMessage = (PubsubMessage)DataHolder.getInstance().getData()
            pubsubMessage.getAttributesMap().size() == 2
            pubsubMessage.getAttributesMap().containsKey("dynamic")
            pubsubMessage.getAttributesMap().get("dynamic") == "foo"
            pubsubMessage.getData().toByteArray() == serialized
    }

    void "method with headers as first argument"(){
        Person person = new Person()
        person.name = "alf"
        when:
            clientWithoutHeaders.sendWithHeadersAsFirstArg("foo", person)
        then:
            def pubsubMessage = (PubsubMessage)DataHolder.getInstance().getData()
            pubsubMessage.getAttributesMap().size() == 2
            pubsubMessage.getAttributesMap().containsKey("dynamic")
            pubsubMessage.getAttributesMap().get("dynamic") == "foo"
    }


}

@PubSubClient
@Requires(property = "spec.name", value = "HeadersSpec")
interface ClientWithoutHeaders {

    @Topic("test-topic")
    String send(Object data)

    @Topic("test-topic")
    @MessageHeader(name = "extra", value = "header")
    String sendWithExtraHeaders(Object data)

    @Topic("test-topic")
    String sendWithDynamicHeaderValue(Object data, @MessageHeader("dynamic") String value)

    @Topic("test-topic")
    String sendWithHeadersAsFirstArg(@MessageHeader("dynamic") String value, Object data)

}