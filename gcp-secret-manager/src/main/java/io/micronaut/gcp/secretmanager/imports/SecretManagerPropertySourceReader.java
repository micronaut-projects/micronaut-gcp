/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.gcp.secretmanager.imports;

import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.context.env.PropertySourceLoader;
import io.micronaut.context.env.PropertySourceReader;
import io.micronaut.core.annotation.Internal;
import io.micronaut.gcp.secretmanager.client.VersionedSecret;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared secret payload reader for Google Secret Manager config imports.
 *
 * @since 5.0
 */
@Internal
public final class SecretManagerPropertySourceReader {

    private static final Logger LOG = LoggerFactory.getLogger(SecretManagerPropertySourceReader.class);

    private SecretManagerPropertySourceReader() {
    }

    /**
     * Reads a decoded secret payload with the available property source readers and returns it as a property source.
     *
     * @param environment The active environment providing property source readers
     * @param secret The fetched secret payload
     * @param sourceName The property source name to publish
     * @param priority The property source priority
     * @return A property source for the decoded secret payload
     */
    public static PropertySource read(Environment environment,
                                      VersionedSecret secret,
                                      String sourceName,
                                      int priority) {
        List<PropertySourceLoader> readers = environment.getPropertySourceLoaders().stream().toList();
        Map<String, Object> data = new HashMap<>();
        for (PropertySourceReader reader : readers) {
            try {
                data.putAll(reader.read(secret.getName(), secret.getContents()));
                if (!data.isEmpty()) {
                    break;
                }
            } catch (Exception e) {
                LOG.debug("Property source reader {} could not decode Google Secret Manager secret {}", reader.getClass().getName(), secret.getName(), e);
            }
        }
        return PropertySource.of(sourceName, data, priority);
    }
}
