/*
 * Copyright 2017-2024 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
