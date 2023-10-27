package io.micronaut.gcp.pubsub.bind

import com.google.cloud.pubsub.v1.AckReplyConsumer
import com.google.protobuf.ByteString
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PubsubMessage
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Executable
import io.micronaut.context.annotation.Requires
import io.micronaut.core.bind.BoundExecutable
import io.micronaut.core.bind.DefaultExecutableBinder
import io.micronaut.gcp.pubsub.annotation.MessageId
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import io.micronaut.messaging.Acknowledgement
import io.micronaut.serde.annotation.Serdeable
import io.reactivex.rxjava3.core.Flowable
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class PubSubBinderSpec extends Specification{

    String animalJson = """
        {
            "name" : "dog"
        }
    """

    void "can bind to supported message body types"(String methodName, Class<?> argType) {
        ApplicationContext applicationContext = ApplicationContext.run(["spec.name" : getClass().simpleName])
        TestBinderBean bean = applicationContext.getBean(TestBinderBean)
        BeanDefinition<TestBinderBean> beanDefinition = applicationContext.getBeanDefinition(TestBinderBean)
        ExecutableMethod<?, ?> method = beanDefinition.findMethod(methodName, argType).get()
        PubSubBinderRegistry binderRegistry = applicationContext.getBean(PubSubBinderRegistry)
        DefaultExecutableBinder<PubSubConsumerState> binder = new DefaultExecutableBinder<>()
        AckReplyConsumer ackReplyConsumer = Mock(AckReplyConsumer)
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of("test-project","test-subscription")
        PubsubMessage message = PubsubMessage.newBuilder().setData(ByteString.copyFrom(animalJson.getBytes())).setMessageId("1234").build()
        PubSubConsumerState consumerState = new PubSubConsumerState(message, ackReplyConsumer, subscriptionName, "application/json")
        BoundExecutable executable = binder.bind(method, binderRegistry, consumerState)

        when:
        executable.invoke(bean)

        then:
        Map<String, Object> result = bean.dataHolder["receive"]
        result["body"] != null && argType.isAssignableFrom(result["body"].getClass())
        verifyAnimalPayload(result["body"])

        where:
        methodName                      | argType
        "bindByteArrayBody"             | byte[]
        "bindMonoByteArrayBody"         | Mono<byte[]>
        "bindFluxByteArrayBody"         | Flux<byte[]>
        "bindFlowableByteArrayBody"     | Flowable<byte[]>
        "bindPubsubMessageBody"         | PubsubMessage
        "bindMonoPubsubMessageBody"     | Mono<PubsubMessage>
        "bindFluxPubsubMessageBody"     | Flux<PubsubMessage>
        "bindFlowablePubsubMessageBody" | Flowable<PubsubMessage>
        "bindPojoBody"                  | Animal
        "bindMonoPojoBody"              | Mono<Animal>
        "bindFluxPojoBody"              | Flux<Animal>
        "bindFlowablePojoBody"          | Flowable<Animal>
    }

    void verifyAnimalPayload(Object result) {
        Object unwrappedResult;
        if (result instanceof Publisher) {
             unwrappedResult = Mono.from(result).block()
        } else {
            unwrappedResult = result
        }
        switch(unwrappedResult) {
            case byte[]:
                byte[] message = (byte[]) unwrappedResult
                assert animalJson == new String(message, StandardCharsets.UTF_8)
                break
            case PubsubMessage:
                PubsubMessage message = (PubsubMessage) unwrappedResult
                assert animalJson == new String(message.getData().toByteArray(), StandardCharsets.UTF_8)
                break
            case Animal:
                Animal message = (Animal) unwrappedResult
                assert message.getName() == "dog"
                break
            default:
                throw new IllegalStateException("Unhandled result type "+unwrappedResult.getClass())
        }
    }

    void "test messageId argument"() {
        ApplicationContext applicationContext = ApplicationContext.run(["spec.name" : getClass().simpleName])
        TestBinderBean bean = applicationContext.getBean(TestBinderBean)
        BeanDefinition<TestBinderBean> beanDefinition = applicationContext.getBeanDefinition(TestBinderBean)
        ExecutableMethod<?, ?> method = beanDefinition.findMethod("receive", byte[], String).get()
        PubSubBinderRegistry binderRegistry = applicationContext.getBean(PubSubBinderRegistry)
        DefaultExecutableBinder<PubSubConsumerState> binder = new DefaultExecutableBinder<>()
        AckReplyConsumer ackReplyConsumer = Mock(AckReplyConsumer)
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of("test-project","test-subscription")
        PubsubMessage message = PubsubMessage.newBuilder().setData(ByteString.copyFrom("foo".getBytes())).setMessageId("1234").build()
        PubSubConsumerState consumerState = new PubSubConsumerState(message, ackReplyConsumer, subscriptionName, "application/json")
        BoundExecutable executable = binder.bind(method, binderRegistry, consumerState)
        when:
            executable.invoke(bean)
        then:
            Map<String, Object> result = bean.dataHolder["receive"]
            result["id"] == "1234"
    }

    void "test with wrong argument"() {
        ApplicationContext applicationContext = ApplicationContext.run(["spec.name" : getClass().simpleName])
        TestBinderBean bean = applicationContext.getBean(TestBinderBean)
        BeanDefinition<TestBinderBean> beanDefinition = applicationContext.getBeanDefinition(TestBinderBean)
        ExecutableMethod<?, ?> method = beanDefinition.findMethod("bindFail", byte[], Integer).get()
        PubSubBinderRegistry binderRegistry = applicationContext.getBean(PubSubBinderRegistry)
        DefaultExecutableBinder<PubSubConsumerState> binder = new DefaultExecutableBinder<>()
        AckReplyConsumer ackReplyConsumer = Mock(AckReplyConsumer)
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of("test-project","test-subscription")
        PubsubMessage message = PubsubMessage.newBuilder().setData(ByteString.copyFrom("foo".getBytes())).setMessageId("1234").build()
        PubSubConsumerState consumerState = new PubSubConsumerState(message, ackReplyConsumer, subscriptionName, "application/json")
        when:
            binder.bind(method, binderRegistry, consumerState)
        then:
            def e = thrown(IllegalArgumentException)
            e.message.startsWith("Can't bind messageId to argument")
    }

    void "should be possible to bind Acknowledgement"() {
        ApplicationContext applicationContext = ApplicationContext.run(["spec.name" : getClass().simpleName])
        TestBinderBean bean = applicationContext.getBean(TestBinderBean)
        BeanDefinition<TestBinderBean> beanDefinition = applicationContext.getBeanDefinition(TestBinderBean)
        ExecutableMethod<?, ?> method = beanDefinition.findMethod("bindWithAck", byte[], Acknowledgement).get()
        PubSubBinderRegistry binderRegistry = applicationContext.getBean(PubSubBinderRegistry)
        DefaultExecutableBinder<PubSubConsumerState> binder = new DefaultExecutableBinder<>()
        AckReplyConsumer ackReplyConsumer = Mock(AckReplyConsumer)
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of("test-project","test-subscription")
        PubsubMessage message = PubsubMessage.newBuilder().setData(ByteString.copyFrom("foo".getBytes())).setMessageId("1234").build()
        PubSubConsumerState consumerState = new PubSubConsumerState(message, ackReplyConsumer, subscriptionName, "application/json")
        BoundExecutable executable = binder.bind(method, binderRegistry, consumerState)
        when:
            executable.invoke(bean)
        then:
            Map<String, Object> result = bean.dataHolder["receive"]
            result["ack"] != null
    }

}

@Singleton
@Requires(property = "spec.name", value = "PubSubBinderSpec")
class TestBinderBean {

    Map<String, Object> dataHolder = new HashMap<>()

    @Executable
    void receive(byte[] body, @MessageId String id) {
        Map<String, Object> data = new HashMap<>()
        data["body"] = body
        data["id"] = id
        dataHolder["receive"] = data
    }

    @Executable
    void bindFail(byte[] body, @MessageId Integer id) {

    }

    @Executable
    void bindWithAck(byte[] body, Acknowledgement ack) {
        Map<String, Object> data = new HashMap<>()
        data["body"] = body
        data["ack"] = ack
        dataHolder["receive"] = data
    }

    @Executable
    void bindByteArrayBody(byte[] body) {
        Map<String, Object> data = new HashMap<>()
        data["body"] = body
        dataHolder["receive"] = data
    }

    @Executable
    void bindMonoByteArrayBody(Mono<byte[]> body) {
        Map<String, Object> data = new HashMap<>()
        data["body"] = body
        dataHolder["receive"] = data
    }

    @Executable
    void bindFluxByteArrayBody(Flux<byte[]> body) {
        Map<String, Object> data = new HashMap<>()
        data["body"] = body
        dataHolder["receive"] = data
    }

    @Executable
    void bindFlowableByteArrayBody(Flowable<byte[]> body) {
        Map<String, Object> data = new HashMap<>()
        data["body"] = body
        dataHolder["receive"] = data
    }

    @Executable
    void bindPubsubMessageBody(PubsubMessage body) {
        Map<String, Object> data = new HashMap<>()
        data["body"] = body
        dataHolder["receive"] = data
    }

    @Executable
    void bindMonoPubsubMessageBody(Mono<PubsubMessage> body) {
        Map<String, Object> data = new HashMap<>()
        data["body"] = body
        dataHolder["receive"] = data
    }

    @Executable
    void bindFluxPubsubMessageBody(Flux<PubsubMessage> body) {
        Map<String, Object> data = new HashMap<>()
        data["body"] = body
        dataHolder["receive"] = data
    }

    @Executable
    void bindFlowablePubsubMessageBody(Flowable<PubsubMessage> body) {
        Map<String, Object> data = new HashMap<>()
        data["body"] = body
        dataHolder["receive"] = data
    }

    @Executable
    void bindPojoBody(Animal body) {
        Map<String, Object> data = new HashMap<>()
        data["body"] = body
        dataHolder["receive"] = data
    }

    @Executable
    void bindMonoPojoBody(Mono<Animal> body) {
        Map<String, Object> data = new HashMap<>()
        data["body"] = body
        dataHolder["receive"] = data
    }

    @Executable
    void bindFluxPojoBody(Flux<Animal> body) {
        Map<String, Object> data = new HashMap<>()
        data["body"] = body
        dataHolder["receive"] = data
    }

    @Executable
    void bindFlowablePojoBody(Flowable<Animal> body) {
        Map<String, Object> data = new HashMap<>()
        data["body"] = body
        dataHolder["receive"] = data
    }
}

@Serdeable
final class Animal {
    private String name;

    Animal(String name) {
        this.name = name;
    }

    Animal() { }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }
}
