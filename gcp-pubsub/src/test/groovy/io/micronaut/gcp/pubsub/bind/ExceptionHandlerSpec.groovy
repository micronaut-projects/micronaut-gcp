package io.micronaut.gcp.pubsub.bind

import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.AbstractConsumerSpec
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.Subscription
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.exception.DefaultPubSubMessageReceiverExceptionHandler
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverException
import io.micronaut.gcp.pubsub.exception.PubSubMessageReceiverExceptionHandler
import io.micronaut.gcp.pubsub.support.Person
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.util.concurrent.PollingConditions

import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest
@Property(name = "spec.name", value = "ExceptionHandlerSpec")
@Property(name = "gcp.projectId", value = "test-project")
class ExceptionHandlerSpec extends AbstractConsumerSpec{

    @Inject
    SimpleClient simpleClient

    @Inject
    SimpleListener simpleListener

    @Inject
    ListenerWithHandler listenerWithHandler

    @Inject
    MockExceptionHandler exceptionHandler

    void "handle exception via default handler"() {
        def invalidPayload = "foo".getBytes()
        PollingConditions conditions = new PollingConditions(timeout: 3)

        when:
            simpleClient.publishTopicNoHandler(invalidPayload)

        then:
            conditions.eventually {
                exceptionHandler.ex != null
            }
    }

    void "handle exception via listener"() {
        def invalidPayload = "foo".getBytes()
        PollingConditions conditions = new PollingConditions(timeout: 3)

        when:
            simpleClient.publishTopicHandler(invalidPayload)
        then:
            conditions.eventually {
                listenerWithHandler.ex != null
            }
    }
}

@PubSubClient
@Requires(property = "spec.name", value = "ExceptionHandlerSpec")
interface SimpleClient {
    @Topic("test-topic-no-handler")
    void publishTopicNoHandler(byte[] data)

    @Topic("test-topic-handler")
    void publishTopicHandler(byte[] data)
}

@PubSubListener
@Requires(property = "spec.name", value = "ExceptionHandlerSpec")
class SimpleListener {

    @Subscription("test-topic-no-handler")
    void onMessage(Person person){

    }

}

@PubSubListener
@Requires(property = "spec.name", value = "ExceptionHandlerSpec")
class ListenerWithHandler implements PubSubMessageReceiverExceptionHandler {

    public PubSubMessageReceiverException ex

    @Subscription("test-topic-handler")
    void onMessage(Person person) {

    }

    @Override
    void handle(PubSubMessageReceiverException exception) {
        this.ex = exception
    }
}

@Singleton
@Primary
@Replaces(DefaultPubSubMessageReceiverExceptionHandler)
@Requires(property = "spec.name", value = "ExceptionHandlerSpec")
class MockExceptionHandler implements PubSubMessageReceiverExceptionHandler {
    public PubSubMessageReceiverException ex

    @Override
    void handle(PubSubMessageReceiverException exception) {
        this.ex = exception
    }
}