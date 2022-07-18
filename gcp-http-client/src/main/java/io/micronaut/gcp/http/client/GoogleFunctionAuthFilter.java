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
package io.micronaut.gcp.http.client;

import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Filter;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

/**
 * A filter that allows function to function communication in GCP
 * (https://cloud.google.com/functions/docs/securing/authenticating?authuser=1#function-to-function).
 *
 * Requires the user to set the {@code gcp.http.client.auth.function.patterns} property with the URI patterns
 * to apply the filter to. For example {@code /**} for all requests.
 *
 * @author perrym
 * @since 4.3.0
 */
@Requires(property = "gcp.http.client.auth.function.patterns")
@Filter(patterns = "${gcp.http.client.auth.function.patterns:/**}")
public class GoogleFunctionAuthFilter extends GoogleAuthFilter {

    /**
     * Default constructor.
     */
    public GoogleFunctionAuthFilter() {
        super();
    }

    @Override
    protected String encodeURI(MutableHttpRequest<?> request) throws UnsupportedEncodingException {
        URI fullURI = request.getUri();
        String receivingURI = fullURI.getScheme() + "://" + fullURI.getHost() + fullURI.getPath();
        return AUDIENCE + URLEncoder.encode(receivingURI, "UTF-8");
    }
}
