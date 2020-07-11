package io.micronaut.gcp.pubsub.support

import com.google.pubsub.v1.PubsubMessage
import io.micronaut.core.type.Argument
import io.micronaut.gcp.pubsub.AbstractPublisherSpec
import io.micronaut.gcp.pubsub.DataHolder
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.Subscription
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.exception.PubSubClientException
import io.micronaut.gcp.pubsub.serdes.PubSubMessageSerDes
import io.micronaut.http.MediaType
import io.micronaut.test.annotation.MicronautTest

import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest
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

}

@PubSubClient
interface SerDesTestClient {
    @Topic(value = "testTopic", contentType = MediaType.APPLICATION_ATOM_XML)
    void invalidMimeType(Object data)

    @Topic(value = "test-topic", contentType = "application/x-custom")
    String customType(Object data)
}

@Singleton
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
