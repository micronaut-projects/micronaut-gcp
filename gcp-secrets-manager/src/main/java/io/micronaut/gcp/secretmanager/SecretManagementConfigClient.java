package io.micronaut.gcp.secretmanager;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.util.StringUtils;
import io.micronaut.discovery.config.ConfigurationClient;
import org.reactivestreams.Publisher;

import javax.inject.Singleton;

@Singleton
@BootstrapContextCompatible
@Requires(property = ConfigurationClient.ENABLED, value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
public class SecretManagementConfigClient implements ConfigurationClient {

    @Override
    public Publisher<PropertySource> getPropertySources(Environment environment) {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }
}
