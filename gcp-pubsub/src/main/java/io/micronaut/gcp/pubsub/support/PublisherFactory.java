package io.micronaut.gcp.pubsub.support;

import com.google.cloud.pubsub.v1.Publisher;

/**
 * The publisher factory interface that can create publishers.
 *
 * @author João André Martins
 * @author Chengyuan Zhao
 */
public interface PublisherFactory {

    Publisher createPublisher(String topic);
}
