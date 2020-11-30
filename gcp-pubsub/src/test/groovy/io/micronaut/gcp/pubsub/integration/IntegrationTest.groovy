package io.micronaut.gcp.pubsub.integration


import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.TopicName
import io.micronaut.context.ApplicationContext
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.Subscription
import io.micronaut.gcp.pubsub.annotation.Topic
import io.micronaut.gcp.pubsub.support.Person
import spock.util.concurrent.PollingConditions

class IntegrationTest extends IntegrationTestSpec{

    void "simple publishing integration"(){
        TopicName topicName = TopicName.of("test-project", "test-topic")
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of("test-project", "test-subscription")
        IntegrationTestSpec.pubSubResourceAdmin.createTopic(topicName)
        IntegrationTestSpec.pubSubResourceAdmin.createSubscription(topicName, subscriptionName)

        ApplicationContext ctx = ApplicationContext.run(
                ["gcp.projectId": "test-project",
                 "spec.name": "IntegrationTest"], "integration")

        PubSubIntegrationTestClient client = ctx.getBean(PubSubIntegrationTestClient)
        PubSubIntegrationTestListener listener = ctx.getBean(PubSubIntegrationTestListener)

        PollingConditions conditions = new PollingConditions(timeout: 3)

        def person = new Person()
            person.name = "alf"

        when:
            client.send(person)
        then:
            conditions.eventually {
                listener.data.name == person.name
            }

    }

}

@PubSubClient
@io.micronaut.context.annotation.Requires(property = "spec.name", value = "IntegrationTest")
interface PubSubIntegrationTestClient {
    @Topic("test-topic")
    String send(Person person)
}

@PubSubListener
@io.micronaut.context.annotation.Requires(property = "spec.name", value = "IntegrationTest")
class PubSubIntegrationTestListener {

    Person data

    @Subscription("test-subscription")
    void onMessage(Person person) {
        this.data = person
    }
}