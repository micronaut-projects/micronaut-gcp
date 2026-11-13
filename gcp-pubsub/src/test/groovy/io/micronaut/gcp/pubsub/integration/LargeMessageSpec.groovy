package io.micronaut.gcp.pubsub.integration

import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.protobuf.ByteString
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.gcp.pubsub.annotation.PubSubClient
import io.micronaut.gcp.pubsub.annotation.PubSubListener
import io.micronaut.gcp.pubsub.annotation.Subscription
import io.micronaut.gcp.pubsub.annotation.Topic
import org.testcontainers.DockerClientFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import spock.lang.AutoCleanup
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.util.concurrent.atomic.AtomicReference

/**
 * Pub/Sub accepts messages of up to 10 MB, while the gRPC default inbound limit is 4 MB.
 * Uses the emulator channel created by {@code PubSubConfigurationFactory} so the configured
 * limit is exercised. Runs its own emulator: the image shared by the other integration tests
 * is from 2020 and rejects requests over 4 MB itself.
 */
@IgnoreIf({ !DockerClientFactory.instance().isDockerAvailable() })
class LargeMessageSpec extends Specification {

    static final String EMULATOR_IMAGE = "gcr.io/google.com/cloudsdktool/google-cloud-cli:592.0.0-emulators"

    @Shared
    @AutoCleanup("stop")
    GenericContainer emulator = new GenericContainer(EMULATOR_IMAGE)
            .withCommand("gcloud", "beta", "emulators", "pubsub", "start", "--project=test-project", "--host-port=0.0.0.0:8085")
            .withExposedPorts(8085)
            .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*Server started, listening on.*"))

    void "a message larger than the gRPC default of 4 MB is delivered to the subscriber"() {
        given:
        emulator.start()
        String emulatorHost = emulator.host + ":" + emulator.getMappedPort(8085)
        ManagedChannel adminChannel = ManagedChannelBuilder.forTarget(emulatorHost).usePlaintext().build()
        def admin = new PubSubResourceAdmin(
                FixedTransportChannelProvider.create(GrpcTransportChannel.create(adminChannel)),
                NoCredentialsProvider.create())
        TopicName topicName = TopicName.of("test-project", "large-topic")
        admin.createTopic(topicName)
        admin.createSubscription(topicName, ProjectSubscriptionName.of("test-project", "large-topic-sub"))
        ApplicationContext ctx = ApplicationContext.run([
                "spec.name"           : "LargeMessageSpec",
                "gcp.projectId"       : "test-project",
                "pubsub.emulator.host": emulatorHost
        ])
        byte[] payload = new byte[6 * 1024 * 1024]
        new Random(42).nextBytes(payload)

        when:
        ctx.getBean(LargeMessageClient).send(payload)

        then:
        new PollingConditions(timeout: 30).eventually {
            ctx.getBean(LargeMessageListener).received.get() != null
        }
        ctx.getBean(LargeMessageListener).received.get().data == ByteString.copyFrom(payload)

        cleanup:
        ctx?.close()
        adminChannel?.shutdownNow()
    }
}

@PubSubClient
@Requires(property = "spec.name", value = "LargeMessageSpec")
interface LargeMessageClient {
    @Topic("large-topic")
    void send(byte[] data)
}

@PubSubListener
@Requires(property = "spec.name", value = "LargeMessageSpec")
class LargeMessageListener {

    final AtomicReference<PubsubMessage> received = new AtomicReference<>()

    @Subscription("large-topic-sub")
    void onMessage(PubsubMessage message) {
        received.set(message)
    }
}
