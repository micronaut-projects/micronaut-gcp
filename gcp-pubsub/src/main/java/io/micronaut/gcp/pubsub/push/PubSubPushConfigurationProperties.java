package io.micronaut.gcp.pubsub.push;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.gcp.pubsub.configuration.PubSubConfigurationProperties;

@Requires(property = PubSubPushConfigurationProperties.PREFIX + ".enabled", notEquals = StringUtils.FALSE, defaultValue = StringUtils.TRUE)
@ConfigurationProperties(PubSubPushConfigurationProperties.PREFIX)
public class PubSubPushConfigurationProperties implements PushControllerConfiguration {

    public static final String PREFIX = PubSubConfigurationProperties.PREFIX + ".push";

    /**
     * The default enable value.
     */
    public static final boolean DEFAULT_ENABLED = true;

    /**
     * The default path.
     */
    public static final String DEFAULT_PATH = "/push";

    private boolean enabled = DEFAULT_ENABLED;

    private String path = DEFAULT_PATH;

    @Override
    @NonNull
    public String getPath() {
        return this.path;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Enables {@link PushController}. Default value {@value #DEFAULT_ENABLED}
     * @param enabled True if it is enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Path to the {@link PushController}. Default value {@value #DEFAULT_PATH}
     * @param path The path
     */
    public void setPath(String path) {
        if (StringUtils.isNotEmpty(path)) {
            this.path = path;
        }
    }
}
