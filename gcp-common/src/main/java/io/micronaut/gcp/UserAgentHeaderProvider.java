/*
 * Copyright 2017-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.gcp;

import com.google.api.gax.rpc.HeaderProvider;

import java.util.Collections;
import java.util.Map;

/**
 * Provides the User-Agent header to signal to the Google Cloud Client Libraries that requests originate from a
 * Micronaut Integration.
 *
 * @author João André Martins
 * @author Chengyuan Zhao
 * @author Mike Eltsufin
 * @author Ray Tsang
 */
public class UserAgentHeaderProvider implements HeaderProvider {

    private String userAgent;

    private final Map<String, String> headers;

    public UserAgentHeaderProvider(String component) {
        this.userAgent = computeUserAgent(component);
        this.headers = Collections.singletonMap("User-Agent", this.userAgent);
    }

    /**
     * Returns the "User-Agent" header whose value should be added to the google-cloud-java REST API calls.
     * e.g., {@code User-Agent: Micronaut/1.0.0.RELEASE micronaut-gcp-trace/1.0.0.RELEASE}.
     */
    @Override
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    /**
     * Returns the "User-Agent" header value which should be added to the google-cloud-java REST API calls.
     * e.g., {@code Micronaut/1.0.0.RELEASE micronaut-gcp-trace/1.0.0.RELEASE}.
     *
     * @return the user agent string.
     */
    public String getUserAgent() {
        return this.userAgent;
    }

    private String computeUserAgent(String component) {
        String library = "micronaut-gcp-" + component;
        String version = this.getClass().getPackage().getImplementationVersion();

        return "Micronaut/" + version + " " + library + "/" + version;

    }
}
