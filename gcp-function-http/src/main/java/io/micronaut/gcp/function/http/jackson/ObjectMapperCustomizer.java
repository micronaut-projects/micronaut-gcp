package io.micronaut.gcp.function.http.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;

import javax.inject.Singleton;

/**
 * Customizes the jackson object mapper to ensure essential modules are registered.
 *
 * @author graemerocher
 * @since 1.2.0
 */
@Singleton
public class ObjectMapperCustomizer implements BeanCreatedEventListener<ObjectMapper> {
    @Override
    public ObjectMapper onCreated(BeanCreatedEvent<ObjectMapper> event) {
        ObjectMapper objectMapper = event.getBean();
        // register modules manually
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());
        return objectMapper;
    }
}
