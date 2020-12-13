package io.micronaut.gcp.pubsub.integration

import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.TopicName
import io.micronaut.context.ApplicationContext
import io.micronaut.gcp.pubsub.annotation.OrderingKey
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.Subscription
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.support.Person
import spock.util.concurrent.PollingConditions

class MessageOrderingTests extends IntegrationTestSpec {

    void "publish message to specific location"() {
        TopicName topicName = TopicName.of("test-project", "test-topic-east")
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of("test-project", "test-subscription-east")
        IntegrationTestSpec.pubSubResourceAdmin.createTopic(topicName)
        IntegrationTestSpec.pubSubResourceAdmin.createSubscription(topicName, subscriptionName)
        PollingConditions conditions = new PollingConditions(timeout: 3)
        ApplicationContext ctx = ApplicationContext.run(
                ["gcp.projectId": "test-project",
                 "spec.name": "MessageOrderingTests"], "integration")
        PubSubLocationClient client = ctx.getBean(PubSubLocationClient)
        PubSubLocationListener listener = ctx.getBean(PubSubLocationListener)

        def person = new Person()
        person.name = "alf"

        when:
            client.send(person)
        then:
            conditions.eventually {
                listener.data.name == person.name
            }
        cleanup:
            ctx.close()
    }

    void "publish message to with ordering key"() {
        TopicName topicName = TopicName.of("test-project", "test-topic-ordering")
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of("test-project", "test-subscription-ordering")
        IntegrationTestSpec.pubSubResourceAdmin.createTopic(topicName)
        IntegrationTestSpec.pubSubResourceAdmin.createSubscription(topicName, subscriptionName)
        PollingConditions conditions = new PollingConditions(timeout: 3)
        ApplicationContext ctx = ApplicationContext.run(
                ["gcp.projectId": "test-project",
                 "spec.name": "MessageOrderingTests"], "integration")
        PubSubOrderingClient client = ctx.getBean(PubSubOrderingClient)
        PubSubOrderingListener listener = ctx.getBean(PubSubOrderingListener)

        def person = new Person()
        person.name = "alf"

        when:
            client.send(person, 42)
        then:
            conditions.eventually {
                listener.data.name == person.name
            }
        cleanup:
            ctx.close()
    }

}

@PubSubClient
@io.micronaut.context.annotation.Requires(property = "spec.name", value = "MessageOrderingTests")
interface PubSubLocationClient {
    @Topic(value ="test-topic-east", endpoint = "us-east1-pubsub.googleapis.com:443")
    void send(Person person)
}

@PubSubClient
@io.micronaut.context.annotation.Requires(property = "spec.name", value = "MessageOrderingTests")
interface PubSubOrderingClient {
    @Topic(value = "test-topic-ordering", endpoint = "us-east1-pubsub.googleapis.com:443")
    void send(Person person, @OrderingKey Integer key)
}

@PubSubListener
@io.micronaut.context.annotation.Requires(property = "spec.name", value = "MessageOrderingTests")
class PubSubLocationListener {

    Person data

    @Subscription("test-subscription-east")
    void onMessage(Person person) {
        this.data = person
    }
}

@PubSubListener
@io.micronaut.context.annotation.Requires(property = "spec.name", value = "MessageOrderingTests")
class PubSubOrderingListener {

    Person data

    @Subscription("test-subscription-ordering")
    void onMessage(Person person) {
        this.data = person
    }
}