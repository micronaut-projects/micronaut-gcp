package io.micronaut.gcp.pubsub.serdes;

import io.micronaut.http.MediaType;

import java.util.Optional;

/**
 * A registry of {@link PubSubMessageSerDes} instances. Returns the
 * SerDes for the given {@link io.micronaut.http.MediaType}
 */
public interface PubSubMessageSerDesRegistry {
    default Optional<PubSubMessageSerDes> find(MediaType type) {
        return find(type.getType());
    }

    Optional<PubSubMessageSerDes> find(String type);
}
