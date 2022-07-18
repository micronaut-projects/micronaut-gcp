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
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientException;
import io.micronaut.http.filter.ClientFilterChain;
import io.micronaut.http.filter.HttpClientFilter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PreDestroy;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A filter that allows service to service communication in GCP (https://cloud.google.com/run/docs/authenticating/service-to-service).
 *
 * @author graemerocher
 * @since 1.0.0
 */
@Requires(env = Environment.GOOGLE_COMPUTE)
public abstract class GoogleAuthFilter implements HttpClientFilter, AutoCloseable {
    private static final String METADATA_FLAVOR = "Metadata-Flavor";
    private static final String GOOGLE = "Google";
    protected static final String AUDIENCE = "/computeMetadata/v1/instance/service-accounts/default/identity?audience=";
    private final HttpClient authClient;

    /**
     * Default constructor.
     */
    public GoogleAuthFilter() {
        try {
            this.authClient = HttpClient.create(new URL("http://metadata"));
        } catch (MalformedURLException e) {
            throw new HttpClientException("Cannot create Google Auth Client: " + e.getMessage(), e);
        }
    }

    @Override
    public Publisher<? extends HttpResponse<?>> doFilter(MutableHttpRequest<?> request, ClientFilterChain chain) {
        Flux<String> token = Mono.fromCallable(() -> encodeURI(request))
                .flatMapMany(authURI -> authClient.retrieve(HttpRequest.GET(authURI).header(
                        METADATA_FLAVOR, GOOGLE
                )));

        return token.flatMap(t -> chain.proceed(request.bearerAuth(t)));
    }

    @Override
    @PreDestroy
    public void close() {
        authClient.close();
    }

    protected abstract String encodeURI(MutableHttpRequest<?> request) throws UnsupportedEncodingException;
}
