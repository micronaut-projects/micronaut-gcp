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
package io.micronaut.discovery.cloud.gcp;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.discovery.cloud.AbstractComputeInstanceMetadata;
import io.micronaut.http.HttpMethod;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is a variant/subset of ComputeInstanceMetadataResolverUtils from micronaut-discovery-core
 * that uses micronaut-serialization instead of jackson-databind.
 *
 * @author Dean Wette
 * @since 5.0.0
 */
@Internal
final class GoogleComputeInstanceMetadataResolverUtils {

    private GoogleComputeInstanceMetadataResolverUtils() {
    }

    /**
     * Resolve a value as a string from the metadata json.
     *
     * @param json The json
     * @param key  The key
     * @return An optional value
     */
    static Optional<String> stringValue(JsonNode json, String key) {
        return Optional.ofNullable(json.get(key)).map(JsonNode::coerceStringValue);
    }

    /**
     * Reads the result of a URL and parses it using the given {@link JsonMapper}.
     *
     * @param url                 the URL to read
     * @param connectionTimeoutMs connection timeout, in milliseconds
     * @param readTimeoutMs       read timeout, in milliseconds
     * @param jsonMapper        JSON mapper to use for parsing
     * @param requestProperties   any request properties to pass
     * @return a {@link JsonNode} instance
     * @throws IOException if any I/O error occurs
     * @since 3.3.0
     */
    static JsonNode readMetadataUrl(URL url, int connectionTimeoutMs, int readTimeoutMs, JsonMapper jsonMapper, Map<String, String> requestProperties) throws IOException {
        try (InputStream in = openMetadataUrl(url, connectionTimeoutMs, readTimeoutMs, requestProperties)) {
            return jsonMapper.readValue(in, Argument.of(JsonNode.class));
        }
    }

    private static InputStream openMetadataUrl(URL url, int connectionTimeoutMs, int readTimeoutMs, Map<String, String> requestProperties) throws IOException {
        URLConnection urlConnection = url.openConnection();

        if (url.getProtocol().equalsIgnoreCase("file")) {
            urlConnection.connect();
            return urlConnection.getInputStream();
        } else {
            HttpURLConnection uc = (HttpURLConnection) urlConnection;
            uc.setConnectTimeout(connectionTimeoutMs);
            requestProperties.forEach(uc::setRequestProperty);
            uc.setReadTimeout(readTimeoutMs);
            uc.setRequestMethod(HttpMethod.GET.name());
            uc.setDoOutput(true);
            return uc.getInputStream();
        }
    }

    /**
     * Populates the instance {@link AbstractComputeInstanceMetadata#setMetadata(Map)} property.
     *
     * @param instanceMetadata The instance metadata
     * @param metadata         A map of metadata
     */
    static void populateMetadata(@NonNull AbstractComputeInstanceMetadata instanceMetadata, @NonNull Map<?, ?> metadata) {
        Map<String, String> stringMetadata = metadata.entrySet().stream()
            .filter(e -> e.getValue() instanceof String)
            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey().toString(), e.getValue().toString()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        instanceMetadata.setMetadata(stringMetadata);
    }
}
