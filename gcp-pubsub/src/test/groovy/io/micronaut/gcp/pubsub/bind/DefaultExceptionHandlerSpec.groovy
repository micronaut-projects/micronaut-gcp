package io.micronaut.gcp.pubsub.bind

import io.micronaut.context.annotation.Property
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
import jakarta.inject.Inject
import org.spockframework.runtime.IStandardStreamsListener
import org.spockframework.runtime.StandardStreamsCapturer
import spock.lang.AutoCleanup
import spock.util.concurrent.PollingConditions

@MicronautTest
@Property(name = "spec.name", value = "DefaultExceptionHandlerSpec")
@Property(name = "gcp.projectId", value = "test-project")
class DefaultExceptionHandlerSpec extends AbstractConsumerSpec {

    @Inject
    SimpleClient simpleClient

    @Inject
    ListenerWithHandler listenerWithHandler

    @Inject
    PubSubMessageReceiverExceptionHandler exceptionHandler

    PollingConditions conditions = new PollingConditions(timeout: 3)

    SimpleStreamsListener captured = new SimpleStreamsListener()

    @AutoCleanup("stop")
    StandardStreamsCapturer capturer = new StandardStreamsCapturer()

    void setup() {
        capturer.addStandardStreamsListener(captured)
        capturer.start()
    }

    void "handle exception via default handler"() {
        given:
        byte[] invalidPayload = "foo".getBytes()

        when:
        simpleClient.publishTopicNoHandler(invalidPayload)

        then:
        exceptionHandler instanceof DefaultPubSubMessageReceiverExceptionHandler
        conditions.eventually {
            captured.messages.any {
                it.contains("Error processing message on bean ${SimpleListener.class.name} listening for subscription")
            }
        }
    }

    void "handle exception via listener"() {
        given:
        byte[] invalidPayload = "foo".getBytes()

        when:
        simpleClient.publishTopicHandler(invalidPayload)

        then:
        exceptionHandler instanceof DefaultPubSubMessageReceiverExceptionHandler
        conditions.eventually {
            listenerWithHandler.ex instanceof PubSubMessageReceiverException
        }
    }



    @PubSubClient
    @Requires(property = "spec.name", value = "DefaultExceptionHandlerSpec")
    static interface SimpleClient {
        @Topic("test-topic-no-handler") void publishTopicNoHandler(byte[] data)
        @Topic("test-topic-handler") void publishTopicHandler(byte[] data)
    }

    @PubSubListener
    @Requires(property = "spec.name", value = "DefaultExceptionHandlerSpec")
    static class SimpleListener {
        @Subscription("test-topic-no-handler") void onMessage(Person person) {}
    }

    @PubSubListener
    @Requires(property = "spec.name", value = "DefaultExceptionHandlerSpec")
    static class ListenerWithHandler implements PubSubMessageReceiverExceptionHandler {
        PubSubMessageReceiverException ex
        @Subscription("test-topic-handler") void onMessage(Person person) {}
        @Override void handle(PubSubMessageReceiverException exception) { ex = exception }
    }

    static class SimpleStreamsListener implements IStandardStreamsListener {
        List<String> messages = []
        @Override void standardOut(String m) { messages << m }
        @Override void standardErr(String m) { messages << m }
    }
}
