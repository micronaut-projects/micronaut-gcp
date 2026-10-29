from typing import Annotated

import java
from com.google.pubsub.v1 import PubsubMessage
from jakarta.inject import Inject, Named
from java.util import Base64, HashMap
from micronaut.context.annotation import Property
from micronaut.gcp.pubsub.push import PushRequest
from micronaut.gcp.pubsub.support.Animal import Animal
from micronaut.http import HttpRequest, HttpStatus
from micronaut.http.client import HttpClient
from micronaut.http.client.annotation import Client
from micronaut.json import JsonMapper
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import BeforeEach, Test
from tools.jackson.dataformat.xml import XmlMapper

from micronaut.gcp.pubsub.subscriber.MessageProcessor import MessageProcessor


# tag::clazzBegin[]
@MicronautTest
@Property(name="spec.name", value="ContentTypePushSubscriberTest")
@Property(name="gcp.projectId", value="test-project")
class ContentTypePushSubscriberSpec:
    jsonMapper: Annotated[JsonMapper, Inject]
# end::clazzBegin[]

# tag::injectClient[]
    pushClient: Annotated[HttpClient, Inject, Client("/")]
# end::injectClient[]

    messageProcessor: Annotated[MessageProcessor, Inject]

    xmlMapper: Annotated[XmlMapper, Inject, Named("xml")]

    @BeforeEach
    def setup(self):
        self.messageProcessor.received = None

    @Test
    def test_raw_bytes(self):
        encoded_data = Base64.getEncoder().encodeToString("foo".encode())
        request = PushRequest("projects/test-project/subscriptions/raw-push-subscription",
                              PushRequest.PushMessage(HashMap(), encoded_data, "1", "2021-02-26T19:13:55.749Z"))
        response = self.pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        assert response.getStatus() == HttpStatus.OK
        assert bytes(self.messageProcessor.received).decode() == "foo"

    @Test
    def test_native_message(self):
        encoded_data = Base64.getEncoder().encodeToString("foo".encode())
        request = PushRequest("projects/test-project/subscriptions/native-push-subscription",
                              PushRequest.PushMessage(HashMap(), encoded_data, "1", "2021-02-26T19:13:55.749Z"))
        response = self.pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        assert response.getStatus() == HttpStatus.OK
        assert java.instanceof(self.messageProcessor.received, PubsubMessage)
        assert self.messageProcessor.received.getData().toStringUtf8() == "foo"

# tag::testMethodBegin[]
    @Test
    def test_json_pojo(self):
        dog = Animal("dog")

        encoded_data = Base64.getEncoder().encodeToString(self.jsonMapper.writeValueAsBytes(dog))  # <1>

        request = PushRequest("projects/test-project/subscriptions/animals-push",  # <2>
                              PushRequest.PushMessage(HashMap(), encoded_data, "1", "2021-02-26T19:13:55.749Z"))

        response = self.pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))  # <3>

        assert response.getStatus() == HttpStatus.OK
# end::testMethodBegin[]
        assert self.messageProcessor.received is not None
        assert self.messageProcessor.received.name == "dog"
# tag::testMethodEnd[]
# end::testMethodEnd[]

    @Test
    def test_xml_pojo(self):
        cat = Animal("cat")
        encoded_data = Base64.getEncoder().encodeToString(self.xmlMapper.writeValueAsBytes(cat))
        request = PushRequest("projects/test-project/subscriptions/animals-legacy-push",
                              PushRequest.PushMessage(HashMap(), encoded_data, "1", "2021-02-26T19:13:55.749Z"))
        response = self.pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

        assert response.getStatus() == HttpStatus.OK
        assert self.messageProcessor.received is not None
        assert self.messageProcessor.received.name == "cat"
# tag::clazzEnd[]
# end::clazzEnd[]
