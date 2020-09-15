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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

/**
 * Logback extension that sets a property called google_cloud_logging to allow users to switch between appender configurations.
 *
 * @author Vinicius Carvalho
 * @since 3.1.1
 */
public class GoogleCloudPropertyDefiner extends PropertyDefinerBase {

    private static final int DEFAULT_READ_TIMEOUT = 500;
    private static final int DEFAULT_CONNECT_TIMEOUT = 500;
    private static final String GOOGLE_COMPUTE_METADATA = "http://metadata.google.internal";

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

    private Boolean isGoogleCompute() {
        try {
            final HttpURLConnection con = createConnection(GOOGLE_COMPUTE_METADATA);
            con.setRequestMethod("GET");
            con.setDoOutput(true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            if (con.getHeaderField("Metadata-Flavor") != null &&
                    con.getHeaderField("Metadata-Flavor").equalsIgnoreCase("Google")) {
                return true;
            }

        } catch (IOException e) {
            // well not google then
        }
        return false;
    }

    private  HttpURLConnection createConnection(String spec) throws IOException {
        final URL url = new URL(spec);
        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setReadTimeout(DEFAULT_READ_TIMEOUT);
        con.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        return con;
    }

}
