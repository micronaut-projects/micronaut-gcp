package io.micronaut.pubsub.testcontainers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.SECONDS;

public class PubSubEmulator {
    private static final Logger LOG = LoggerFactory.getLogger(PubSubEmulator.class);
    private static final String IMAGE_NAME = "thekevjames/gcloud-pubsub-emulator:446.0.0";
    private static final int PUBSUB_PORT = 8681;
    private static final int SUBSCRIPTION_PORT = 8682;
    private static GenericContainer<?> container;

    public static Map<String, String> getProperties() {
        if (container == null) {
            container = new GenericContainer<>(DockerImageName.parse(IMAGE_NAME));
            container.setExposedPorts(List.of(PUBSUB_PORT, SUBSCRIPTION_PORT));
            container.withStartupTimeout(Duration.of(600, SECONDS)); // 10 minutes as this image is massive
            container.withEnv("PUBSUB_PROJECT1", "gcp-test-suite,animals:animals,animals-async:animals-async,raw-subscription:raw-subscription,native-subscription:native-subscription,animals-legacy:animals-legacy");
            LOG.trace("start container with image {}", IMAGE_NAME);
            container.start();
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while (!container.isRunning());
        }
        return getProperties(container);
    }

    public static void close() {
        if (container != null) {
            container.close();
            container = null;
        }
    }

    private static Map<String, String> getProperties(GenericContainer<?> container) {
        return Map.of(
                "pubsub.emulator.host", container.getHost() + ":" + container.getMappedPort(PUBSUB_PORT),
                "pubsub.host", container.getHost(),
                "pubsub.port", String.valueOf(container.getMappedPort(PUBSUB_PORT)),
                "pubsub.subscription.port", String.valueOf(container.getMappedPort(SUBSCRIPTION_PORT))
        );
    }
}
