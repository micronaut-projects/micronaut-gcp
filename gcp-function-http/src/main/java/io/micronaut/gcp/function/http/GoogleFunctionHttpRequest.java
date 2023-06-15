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
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpParameters;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.servlet.http.BodyBuilder;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpRequest;
import io.micronaut.servlet.http.ServletHttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private MutableHttpParameters httpParameters;
    private MutableConvertibleValues<Object> attributes;
    private Supplier<Optional<B>> body;
    private volatile GoogleCookies cookies;

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
        if (cookies == null) {
            synchronized (this) { // double check
                if (cookies == null) {
                    cookies = new GoogleCookies(getPath(), getHeaders(), conversionService);
                }
            }
        }
        return cookies;
    }

    @NonNull
    @Override
    public HttpParameters getParameters() {
        MutableHttpParameters httpParameters = this.httpParameters;
        if (httpParameters == null) {
            synchronized (this) { // double check
                httpParameters = this.httpParameters;
                if (httpParameters == null) {
                    httpParameters = new GoogleFunctionParameters();
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

    @Override
    public MutableHttpRequest<B> mutate() {
        return new GoogleFunctionMutableHttpRequest();
    }

    /**
     * Models the http parameters.
     */
    private final class GoogleFunctionParameters implements MutableHttpParameters {
        private final Map<String, List<String>> params = googleRequest.getQueryParameters();

        @Override
        public List<String> getAll(CharSequence name) {
            if (StringUtils.isNotEmpty(name)) {
                final List<String> strings = params.get(name.toString());
                if (CollectionUtils.isNotEmpty(strings)) {
                    return strings;
                }
            }
            return Collections.emptyList();
        }

        @Nullable
        @Override
        public String get(CharSequence name) {
            return getFirst(name).orElse(null);
        }

        @Nullable
        @Override
        public Optional<String> getFirst(CharSequence name) {
            ArgumentUtils.requireNonNull("name", name);
            return googleRequest.getFirstQueryParameter(name.toString());
        }

        @Override
        public Set<String> names() {
            return params.keySet();
        }

        @Override
        public Collection<List<String>> values() {
            return params.values();
        }

        @Override
        public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
            final String v = get(name);
            if (v != null) {
                if (conversionService == null) {
                    return Optional.empty();
                }
                return conversionService.convert(v, conversionContext);
            }
            return Optional.empty();
        }

        @Override
        public MutableHttpParameters add(CharSequence name, List<CharSequence> values) {
            params.put(name.toString(), values.stream().map(CharSequence::toString).toList());
            return this;
        }

        @Override
        public void setConversionService(@NonNull ConversionService conversionService) {
            // Not used
        }
    }

    /**
     * Models the headers.
     */
    private final class GoogleFunctionHeaders extends GoogleMultiValueMap implements MutableHttpHeaders {
        GoogleFunctionHeaders(ConversionService conversionService) {
            super(googleRequest.getHeaders());
            setConversionService(conversionService);
        }

        @Override
        public MutableHttpHeaders add(CharSequence header, CharSequence value) {
            ArgumentUtils.requireNonNull("header", header);
            if (value != null) {
                googleRequest.getHeaders()
                    .computeIfAbsent(header.toString(), s -> new ArrayList<>())
                    .add(value.toString());
            } else {
                googleRequest.getHeaders().remove(header.toString());
            }
            return this;
        }

        @Override
        public MutableHttpHeaders remove(CharSequence header) {
            ArgumentUtils.requireNonNull("header", header);
            googleRequest.getHeaders().remove(header.toString());
            return this;
        }
    }

    private class GoogleFunctionMutableHttpRequest implements MutableHttpRequest<B> {
        private URI uri = GoogleFunctionHttpRequest.this.getUri();

        @Nullable
        private Object body;

        @Override
        public MutableHttpRequest<B> cookie(Cookie cookie) {
            GoogleFunctionHttpRequest.this.cookies.put(cookie.getName(), cookie);
            return this;
        }

        @Override
        public MutableHttpRequest<B> uri(URI uri) {
            this.uri = uri;
            return this;
        }

        @Override
        public <B1> MutableHttpRequest<B1> body(B1 body) {
            this.body = body;
            return (MutableHttpRequest<B1>) this;
        }

        @Override
        public MutableHttpHeaders getHeaders() {
            return GoogleFunctionHttpRequest.this.headers;
        }

        @Override
        public @NonNull MutableConvertibleValues<Object> getAttributes() {
            return GoogleFunctionHttpRequest.this.getAttributes();
        }

        @Override
        public @NonNull Optional<B> getBody() {
            if (body != null) {
                return Optional.of((B) body);
            }
            return GoogleFunctionHttpRequest.this.getBody();
        }

        @Override
        public @NonNull Cookies getCookies() {
            return GoogleFunctionHttpRequest.this.cookies;
        }

        @Override
        public MutableHttpParameters getParameters() {
            return GoogleFunctionHttpRequest.this.httpParameters;
        }

        @Override
        public @NonNull HttpMethod getMethod() {
            return GoogleFunctionHttpRequest.this.getMethod();
        }

        @Override
        public @NonNull URI getUri() {
            return this.uri;
        }

        @Override
        public void setConversionService(@NonNull ConversionService conversionService) {
            // ignored, we use the parent
        }
    }
}
