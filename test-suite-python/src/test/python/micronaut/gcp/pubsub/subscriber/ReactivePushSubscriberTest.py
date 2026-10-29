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


@MicronautTest
@Property(name="spec.name", value="ReactivePushSubscriberTest")
@Property(name="gcp.projectId", value="test-project")
class ReactivePushSubscriberTest:
    pushClient: Annotated[HttpClient, Inject, Client("/")]
    xmlMapper: Annotated[XmlMapper, Inject, Named("xml")]
    jsonMapper: Annotated[JsonMapper, Inject]
    messageProcessor: Annotated[MessageProcessor, Inject]

    @BeforeEach
    def setup(self):
        self.messageProcessor.unwrapped_result = None

    def push(self, subscription: str, encoded_data: str):
        request = PushRequest("projects/test-project/subscriptions/" + subscription,
                              PushRequest.PushMessage(HashMap(), encoded_data, "1", "2021-02-26T19:13:55.749Z"))
        return self.pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

    @Test
    def test_raw_bytes(self):
        response = self.push("raw-push-subscription", Base64.getEncoder().encodeToString("foo".encode()))

        assert response.getStatus() == HttpStatus.OK
        result = self.messageProcessor.unwrapped_result
        assert result is not None
        assert bytes(result).decode() == "foo"

    @Test
    def test_native_message(self):
        response = self.push("native-push-subscription", Base64.getEncoder().encodeToString("foo".encode()))

        assert response.getStatus() == HttpStatus.OK
        result = self.messageProcessor.unwrapped_result
        assert java.instanceof(result, PubsubMessage)
        assert result.getData().toStringUtf8() == "foo"

    @Test
    def test_json_pojo(self):
        response = self.push("animals-push", Base64.getEncoder().encodeToString(self.jsonMapper.writeValueAsBytes(Animal("dog"))))

        assert response.getStatus() == HttpStatus.OK
        result = self.messageProcessor.unwrapped_result
        assert result is not None
        assert result.name == "dog"

    @Test
    def test_xml_pojo(self):
        response = self.push("animals-legacy-push", Base64.getEncoder().encodeToString(self.xmlMapper.writeValueAsBytes(Animal("cat"))))

        assert response.getStatus() == HttpStatus.OK
        result = self.messageProcessor.unwrapped_result
        assert result is not None
        assert result.name == "cat"
