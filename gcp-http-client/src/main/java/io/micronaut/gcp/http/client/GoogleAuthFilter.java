/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.exceptions.HttpClientException;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import javax.annotation.PreDestroy;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

/**
 * A filter that allows service to service communication in GCP (https://cloud.google.com/run/docs/authenticating/service-to-service).
 *
 * Requires the user to set the {@code gcp.http.client.auth.patterns} property with the URI patterns
 * to apply the filter to. For example {@code /**} for all requests.
 *
 * @author graemerocher
 * @since 1.0.0
 */
@Requires(env = Environment.GOOGLE_COMPUTE)
@Requires(property = "gcp.http.client.auth.patterns")
@Filter(patterns = "${gcp.http.client.auth.patterns:/**}")
public class GoogleAuthFilter implements HttpClientFilter, AutoCloseable {
    private static final String METADATA_FLAVOR = "Metadata-Flavor";
    private static final String GOOGLE = "Google";
    private static final String AUDIENCE = "/computeMetadata/v1/instance/service-accounts/default/identity?audience=";
    private final RxHttpClient authClient;

    /**
     * Default constructor.
     */
    public GoogleAuthFilter() {
        try {
            this.authClient = RxHttpClient.create(new URL("http://metadata"));
        } catch (MalformedURLException e) {
            throw new HttpClientException("Cannot create Google Auth Client: " + e.getMessage(), e);
        }
    }

    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request, ClientFilterChain chain) {
        Flowable<String> token = Flowable.fromCallable(() -> encodeURI(request))
                .flatMap(authURI -> authClient.retrieve(HttpRequest.GET(authURI).header(
                        METADATA_FLAVOR, GOOGLE
                )));

        return token.flatMap(t -> chain.proceed(request.bearerAuth(t)));
    }

    @Override
    @PreDestroy
    public void close() {
        authClient.close();
    }

    private String encodeURI(MutableHttpRequest<?> request) throws UnsupportedEncodingException {
        URI fullURI = request.getUri();
        String receivingURI = fullURI.getScheme() + "://" + fullURI.getHost();
        return AUDIENCE + URLEncoder.encode(receivingURI, "UTF-8");
    }
}
