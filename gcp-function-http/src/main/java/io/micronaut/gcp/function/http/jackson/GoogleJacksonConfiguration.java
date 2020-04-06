package io.micronaut.gcp.function.http.jackson;

import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.jackson.JacksonConfiguration;

import javax.inject.Singleton;

/**
 * Disables module scan for Jackson which is slow in function context.
 *
 * @author graemerocher
 * @since 1.2.0
 */
@Singleton
public class GoogleJacksonConfiguration implements BeanCreatedEventListener<JacksonConfiguration> {
    @Override
    public JacksonConfiguration onCreated(BeanCreatedEvent<JacksonConfiguration> event) {
        JacksonConfiguration jacksonConfiguration = event.getBean();
        jacksonConfiguration.setModuleScan(false);
        jacksonConfiguration.setBeanIntrospectionModule(true);
        return jacksonConfiguration;
    }
}
