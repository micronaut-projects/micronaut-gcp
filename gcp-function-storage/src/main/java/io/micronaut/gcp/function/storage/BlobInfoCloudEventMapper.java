package io.micronaut.gcp.function.storage;

import io.cloudevents.CloudEvent;
import io.micronaut.cloudevents.CloudEventMapper;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

@Singleton
public class BlobInfoCloudEventMapper implements CloudEventMapper<BlobInfo> {
    private static final Logger LOG = LoggerFactory.getLogger(BlobInfoCloudEventMapper.class);
    private static final String APPLICATION_JSON = "application/json";
    private final ObjectMapper objectMapper;

    public BlobInfoCloudEventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @NonNull
    public Optional<BlobInfo> map(@NonNull CloudEvent event) {
        if (!APPLICATION_JSON.equals(event.getDataContentType())) {
            return Optional.empty();
        }
        if (event.getData() == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(objectMapper.readValue(event.getData().toBytes(), BlobInfo.class));

        } catch (IOException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("IOException reading bytes into BlobInfo", e);
            }
        }
        return Optional.empty();
    }
}
