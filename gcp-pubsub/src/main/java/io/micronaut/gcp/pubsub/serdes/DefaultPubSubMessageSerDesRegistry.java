package io.micronaut.gcp.pubsub.serdes;


import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Default implementation of a {@link PubSubMessageSerDesRegistry}.
 *
 * @author Vinicius Carvalho
 * @since 2.0.0
 */
@Singleton
public class DefaultPubSubMessageSerDesRegistry implements PubSubMessageSerDesRegistry {

    private final Map<String, PubSubMessageSerDes> serDesRegistry;

    /**
     * @param serDes list of {@link PubSubMessageSerDes} to be injected
     */
    public DefaultPubSubMessageSerDesRegistry(PubSubMessageSerDes[] serDes) {
        this.serDesRegistry = Arrays.stream(serDes).collect(Collectors.toMap(PubSubMessageSerDes::supportedType, s -> s));
    }

    @Override
    public Optional<PubSubMessageSerDes> find(String type) {
        return Optional.ofNullable(serDesRegistry.get(type));
    }
}
