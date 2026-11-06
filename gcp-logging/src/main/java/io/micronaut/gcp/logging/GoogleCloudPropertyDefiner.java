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
package io.micronaut.gcp.logging;

import ch.qos.logback.core.PropertyDefinerBase;
import io.micronaut.context.env.Environment;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Logback extension that sets a property called google_cloud_logging to allow users to switch between appender configurations.
 *
 * @author Vinicius Carvalho
 * @since 3.2.0
 */
public class GoogleCloudPropertyDefiner extends PropertyDefinerBase {

    private static final int DEFAULT_READ_TIMEOUT = 500;
    private static final int DEFAULT_CONNECT_TIMEOUT = 500;
    private static final String GOOGLE_COMPUTE_METADATA = "http://metadata.google.internal";
    private static final int DEFAULT_MAX_ATTEMPTS = 3;
    private static final long DEFAULT_RETRY_DELAY = 500;

    private int maxAttempts = DEFAULT_MAX_ATTEMPTS;
    private long retryDelay = DEFAULT_RETRY_DELAY;
    private String metadataUrl = GOOGLE_COMPUTE_METADATA;

    /**
     * @return true if application is running on GCP via metadata server detection or if user provided MICRONAUT_ENVIRONMENTS value.
     */
    @Override
    public String getPropertyValue() {
        String[] fromEnvironment = StringUtils.tokenizeToStringArray(Optional.ofNullable(System.getenv(Environment.ENVIRONMENTS_ENV)).orElse(""), ",");
        String[] fromProperties = StringUtils.tokenizeToStringArray(Optional.ofNullable(System.getProperty(Environment.ENVIRONMENTS_PROPERTY)).orElse(""), ",");
        String[] combinedEnvironments = ArrayUtils.concat(fromEnvironment, fromProperties);
        boolean isGcp = false;
        if (combinedEnvironments.length > 0) {
            isGcp = Arrays.stream(combinedEnvironments).anyMatch(s -> s.equals(Environment.GOOGLE_COMPUTE));
        } else {
            isGcp = isGoogleCompute();
        }
        return isGcp ? "CONSOLE_JSON" : "STDOUT";
    }

    /**
     * Sets how many times the metadata server is queried before deciding the application is not
     * running on Google Cloud. Set from logback.xml with {@code <maxAttempts>} inside {@code <define>}.
     * A host name that does not resolve is never retried.
     *
     * @param maxAttempts the number of attempts, at least 1
     * @since 6.2.1
     */
    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = Math.max(1, maxAttempts);
    }

    /**
     * Sets the delay between metadata server attempts in milliseconds. Set from logback.xml with
     * {@code <retryDelay>} inside {@code <define>}.
     *
     * @param retryDelay the delay in milliseconds
     * @since 6.2.1
     */
    public void setRetryDelay(long retryDelay) {
        this.retryDelay = Math.max(0, retryDelay);
    }

    /**
     * Overrides the metadata server URL. Defaults to {@code http://metadata.google.internal}.
     *
     * @param metadataUrl the metadata server URL
     * @since 6.2.1
     */
    public void setMetadataUrl(String metadataUrl) {
        this.metadataUrl = metadataUrl;
    }

    private boolean isGoogleCompute() {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                if (queryMetadataServer()) {
                    return true;
                }
            } catch (UnknownHostException _) {
                // the metadata host only resolves on Google Cloud: not Google, no point retrying
                return false;
            } catch (IOException _) {
                // the server may not be reachable yet, for example while a service mesh sidecar starts
            }
            if (attempt < maxAttempts && !sleep()) {
                return false;
            }
        }
        return false;
    }

    private boolean queryMetadataServer() throws IOException {
        HttpURLConnection con = createConnection(metadataUrl);
        try {
            con.setRequestMethod("GET");
            con.setRequestProperty("Metadata-Flavor", "Google");
            try (InputStream in = con.getInputStream()) {
                in.transferTo(OutputStream.nullOutputStream());
            }
            return "Google".equalsIgnoreCase(con.getHeaderField("Metadata-Flavor"));
        } finally {
            con.disconnect();
        }
    }

    private boolean sleep() {
        try {
            Thread.sleep(retryDelay);
            return true;
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private HttpURLConnection createConnection(String spec) throws IOException {
        final URL url = URI.create(spec).toURL();
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setReadTimeout(DEFAULT_READ_TIMEOUT);
        con.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        return con;
    }

}
