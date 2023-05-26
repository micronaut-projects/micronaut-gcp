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
package io.micronaut.gcp.function.http;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.servlet.http.BodyBuilder;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpRequest;
import io.micronaut.servlet.http.ServletHttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Implementation of the {@link ServletHttpRequest} interface for Google Cloud Function.
 *
 * @param <B> The body type
 * @author graemerocher
 * @since 1.2.0
 */
@Internal
final class GoogleFunctionHttpRequest<B> implements ServletHttpRequest<com.google.cloud.functions.HttpRequest, B>, ServletExchange<com.google.cloud.functions.HttpRequest, com.google.cloud.functions.HttpResponse> {
    private final com.google.cloud.functions.HttpRequest googleRequest;
    private final URI uri;
    private final HttpMethod method;
    private final GoogleFunctionHeaders headers;
    private final GoogleFunctionHttpResponse<?> googleResponse;
    private HttpParameters httpParameters;
    private MutableConvertibleValues<Object> attributes;
    private Supplier<Optional<B>> body;
    private Cookies cookies;

    private ConversionService conversionService;

    /**
     * Default constructor.
     *
     * @param googleRequest  The native google request
     * @param googleResponse The {@link GoogleFunctionHttpResponse} object
     * @param conversionService Conversion Service
     * @param bodyBuilder Body Builder
     */
    GoogleFunctionHttpRequest(
        com.google.cloud.functions.HttpRequest googleRequest,
        GoogleFunctionHttpResponse<?> googleResponse,
        ConversionService conversionService,
        BodyBuilder bodyBuilder) {
        this.googleRequest = googleRequest;
        this.googleResponse = googleResponse;
        this.uri = URI.create(googleRequest.getUri());
        HttpMethod method;
        try {
            method = HttpMethod.valueOf(googleRequest.getMethod());
        } catch (IllegalArgumentException e) {
            method = HttpMethod.CUSTOM;
        }
        this.method = method;
        this.headers = new GoogleFunctionHeaders(conversionService);
        this.conversionService = conversionService;

        this.body = SupplierUtil.memoizedNonEmpty(() -> {
            B built = (B) bodyBuilder.buildBody(this::getInputStream, this);
            return Optional.ofNullable(built);
        });
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return googleRequest.getInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return googleRequest.getReader();
    }

    @Override
    public com.google.cloud.functions.HttpRequest getNativeRequest() {
        return googleRequest;
    }

    /**
     * Reference to the response object.
     *
     * @return The response.
     */
    GoogleFunctionHttpResponse<?> getGoogleResponse() {
        return googleResponse;
    }

    @NonNull
    @Override
    public Cookies getCookies() {
        Cookies cookies = this.cookies;
        if (cookies == null) {
            synchronized (this) { // double check
                cookies = this.cookies;
                if (cookies == null) {
                    cookies = new GoogleCookies(getPath(), getHeaders(), conversionService);
                    this.cookies = cookies;
                }
            }
        }
        return cookies;
    }

    @NonNull
    @Override
    public HttpParameters getParameters() {
        HttpParameters httpParameters = this.httpParameters;
        if (httpParameters == null) {
            synchronized (this) { // double check
                httpParameters = this.httpParameters;
                if (httpParameters == null) {
                    httpParameters = new GoogleFunctionParameters(conversionService);
                    this.httpParameters = httpParameters;
                }
            }
        }
        return httpParameters;
    }

    @NonNull
    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @NonNull
    @Override
    public URI getUri() {
        return this.uri;
    }

    @NonNull
    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @NonNull
    @Override
    public MutableConvertibleValues<Object> getAttributes() {
        MutableConvertibleValues<Object> attributes = this.attributes;
        if (attributes == null) {
            synchronized (this) { // double check
                attributes = this.attributes;
                if (attributes == null) {
                    attributes = new MutableConvertibleValuesMap<>();
                    this.attributes = attributes;
                }
            }
        }
        return attributes;
    }

    @NonNull
    @Override
    public Optional<B> getBody() {
        return this.body.get();
    }

    @NonNull
    @Override
    public <T> Optional<T> getBody(@NonNull Argument<T> arg) {
        return getBody().map(t -> conversionService.convertRequired(t, arg));
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServletHttpRequest<com.google.cloud.functions.HttpRequest, ? super Object> getRequest() {
        return (ServletHttpRequest) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServletHttpResponse<com.google.cloud.functions.HttpResponse, ? super Object> getResponse() {
        return (ServletHttpResponse<com.google.cloud.functions.HttpResponse, ? super Object>) googleResponse;
    }

    /**
     * Models the http parameters.
     */
    private final class GoogleFunctionParameters extends GoogleMultiValueMap implements HttpParameters {
        GoogleFunctionParameters(ConversionService conversionService) {
            super(googleRequest.getQueryParameters());
            setConversionService(conversionService);
        }

        @Override
        public Optional<String> getFirst(CharSequence name) {
            ArgumentUtils.requireNonNull("name", name);
            return googleRequest.getFirstQueryParameter(name.toString());
        }

        @Nullable
        @Override
        public String get(CharSequence name) {
            return getFirst(name).orElse(null);
        }
    }

    /**
     * Models the headers.
     */
    private final class GoogleFunctionHeaders extends GoogleMultiValueMap implements HttpHeaders {
        GoogleFunctionHeaders(ConversionService conversionService) {
            super(googleRequest.getHeaders());
            setConversionService(conversionService);
        }
    }
}
