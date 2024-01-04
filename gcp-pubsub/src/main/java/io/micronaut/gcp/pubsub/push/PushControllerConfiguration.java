package io.micronaut.gcp.pubsub.push;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.Toggleable;

@FunctionalInterface
public interface PushControllerConfiguration extends Toggleable {

    /**
     * @return the path where push messages will be received
     */
    @NonNull
    String getPath();
}
