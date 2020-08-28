package io.micronaut.gcp.pubsub.support

import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.core.type.Argument
import io.micronaut.gcp.pubsub.AbstractPublisherSpec
import io.micronaut.gcp.pubsub.DataHolder
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.exception.PubSubClientException
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDes
import io.micronaut.http.MediaType
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest
@Property(name = "spec.name", value = "SerDesSpec")
@Property(name = "gcp.projectId", value = "test-project")
class SerDesSpec extends AbstractPublisherSpec {

    @Inject
    SerDesTestClient testClient

    void "method with invalid content type"(){
        when:
            testClient.invalidMimeType("")
        then:
            def e = thrown(PubSubClientException)
            e.message.startsWith("Could not locate a valid SerDes implementation for type")
    }

    void "method with custom content type"(){
        Person person = new Person()
        person.name = "alf"
        byte[] expected = [42]
        when:
            testClient.customType(person)
        then:
            PubsubMessage pubSubMessage = (PubsubMessage) DataHolder.getInstance().getData()
            pubSubMessage.getAttributesMap().get("Content-Type") == "application/x-custom"
            pubSubMessage.getData().toByteArray() == expected
    }

    void "bypass serdes with raw bytes"(){
        byte[] expected = [42]
        when:
            testClient.bypassSerDes(expected)
        then:
            PubsubMessage pubSubMessage = (PubsubMessage) DataHolder.getInstance().getData()
            pubSubMessage.getAttributesMap().get("Content-Type") == MediaType.APPLICATION_JSON
            pubSubMessage.getData().toByteArray() == expected
    }

    void "bypass serdes and set content type"() {
        byte[] expected = [42]
        when:
            testClient.bypassSerDesWithContent(expected)
        then:
            PubsubMessage pubSubMessage = (PubsubMessage) DataHolder.getInstance().getData()
            pubSubMessage.getAttributesMap().get("Content-Type") == MediaType.IMAGE_JPEG
            pubSubMessage.getData().toByteArray() == expected

    }

    void "bypass serdes with pubsub type"() {
        PubsubMessage message = PubsubMessage
                .newBuilder()
                    .putAttributes("Custom-Attr", "foo")
                    .setData(ByteString.copyFrom("foo".getBytes()))
                .build()
        when:
            testClient.bypassSerDes(message)
        then:
            PubsubMessage pubSubMessage = (PubsubMessage) DataHolder.getInstance().getData()
            pubSubMessage.containsAttributes("Content-Type") == false
            pubSubMessage.getAttributesMap().get("Custom-Attr") == "foo"
            pubSubMessage.getData().toByteArray() == "foo".getBytes()
    }

}

@PubSubClient
@Requires(property = "spec.name", value = "SerDesSpec")
interface SerDesTestClient {
    @Topic(value = "testTopic", contentType = MediaType.APPLICATION_ATOM_XML)
    void invalidMimeType(Object data)

    @Topic(value = "test-topic", contentType = "application/x-custom")
    String customType(Object data)

    @Topic(value = "test-topic")
    String bypassSerDes(byte[] data)

    @Topic(value = "test-topic", contentType = MediaType.IMAGE_JPEG)
    String bypassSerDesWithContent(byte[] data)

    @Topic("test-topic")
    String bypassSerDes(PubsubMessage message)
}

@Singleton
@Requires(property = "spec.name", value = "SerDesSpec")
class CustomSerializer implements PubSubMessageSerDes {

    @Override
    Object deserialize(byte[] data, Argument<?> type) {
        return null
    }

    @Override
    byte[] serialize(Object data) {
        return [42]
    }

    @Override
    String supportedType() {
        return "application/x-custom"
    }
}
