from typing import Annotated

from jakarta.inject import Inject
from java.util import Base64, HashMap
from micronaut.context.annotation import Property
from micronaut.gcp.pubsub.push import PushRequest
from micronaut.gcp.pubsub.support.Animal import Animal
from micronaut.http import HttpRequest, HttpStatus
from micronaut.http.client import HttpClient
from micronaut.http.client.annotation import Client
from micronaut.http.client.exceptions import HttpClientResponseException
from micronaut.json import JsonMapper
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import BeforeEach, Test

from micronaut.gcp.pubsub.subscriber.MessageProcessor import MessageProcessor


@MicronautTest
@Property(name="spec.name", value="AcknowledgementPushSubscriberTest")
@Property(name="gcp.projectId", value="test-project")
class AcknowledgementPushSubscriberTest:
    pushClient: Annotated[HttpClient, Inject, Client("/")]
    messageProcessor: Annotated[MessageProcessor, Inject]
    jsonMapper: Annotated[JsonMapper, Inject]

    @BeforeEach
    def setup(self):
        self.messageProcessor.result = True
        self.messageProcessor.received = []

    def push(self, subscription: str, animal: Animal):
        encoded_data = Base64.getEncoder().encodeToString(self.jsonMapper.writeValueAsBytes(animal))
        request = PushRequest("projects/test-project/subscriptions/" + subscription,
                              PushRequest.PushMessage(HashMap(), encoded_data, "1", "2021-02-26T19:13:55.749Z"))
        return self.pushClient.toBlocking().exchange(HttpRequest.POST("/push", request))

    @Test
    def test_blocking_ack(self):
        self.messageProcessor.result = True

        response = self.push("animals-push", Animal("dog"))

        assert response.getStatus() == HttpStatus.OK

    @Test
    def test_blocking_nack(self):
        self.messageProcessor.result = False

        try:
            self.push("animals-push", Animal("cat"))
        except HttpClientResponseException as ex:
            assert ex.getResponse().getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        else:
            assert False, "expected a 422 response"

    @Test
    def test_async_ack(self):
        self.messageProcessor.result = True

        response = self.push("animals-async-push", Animal("dog"))

        assert response.getStatus() == HttpStatus.OK

    @Test
    def test_async_nack(self):
        self.messageProcessor.result = False

        try:
            self.push("animals-async-push", Animal("cat"))
        except HttpClientResponseException as ex:
            assert ex.getResponse().getStatus() == HttpStatus.UNPROCESSABLE_ENTITY
        else:
            assert False, "expected a 422 response"
