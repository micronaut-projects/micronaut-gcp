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
import spock.lang.Specification

import jakarta.inject.Singleton

class PubSubBinderSpec extends Specification{

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
}