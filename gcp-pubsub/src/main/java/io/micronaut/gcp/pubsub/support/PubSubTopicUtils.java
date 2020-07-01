package io.micronaut.gcp.pubsub.support;

import com.google.pubsub.v1.ProjectTopicName;

import javax.annotation.Nonnull;

/**
 * Various utility methods for dealing with Pub/Sub topics.
 *
 * @author Mike Eltsufin
 * @since 1.2
 */
public final class PubSubTopicUtils {

    private PubSubTopicUtils() {
    }

    /**
     * Create a {@link ProjectTopicName} based on a topic name within a project or the
     * fully-qualified topic name. If the specified topic is in the
     * {@code projects/<project_name>/topics/<topic_name>} format, then the {@code projectId} is
     * ignored}
     * @param topic the topic name in the project or the fully-qualified project name
     * @param projectId the project ID to use if the topic is not a fully-qualified name
     * @return the Pub/Sub object representing the topic name
     */
    public static ProjectTopicName toProjectTopicName(@Nonnull String topic, @Nonnull String projectId) {

        ProjectTopicName projectTopicName = null;

        if (ProjectTopicName.isParsableFrom(topic)) {
            // Fully-qualified topic name in the "projects/<project_name>/topics/<topic_name>" format
            projectTopicName = ProjectTopicName.parse(topic);
        } else {
            projectTopicName = ProjectTopicName.of(projectId, topic);
        }

        return projectTopicName;
    }
}
