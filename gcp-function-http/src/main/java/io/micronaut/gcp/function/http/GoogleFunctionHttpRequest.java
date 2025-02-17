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
import io.micronaut.core.execution.ExecutionFlow;
import io.micronaut.core.io.buffer.ByteArrayBufferFactory;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.http.CaseInsensitiveMutableHttpHeaders;
import io.micronaut.http.FullHttpRequest;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpParameters;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.ServerHttpRequest;
import io.micronaut.http.body.ByteBody;
import io.micronaut.http.body.CloseableAvailableByteBody;
import io.micronaut.http.body.CloseableByteBody;
import io.micronaut.http.body.stream.InputStreamByteBody;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.simple.SimpleHttpParameters;
import io.micronaut.servlet.http.BodyBuilder;
import io.micronaut.servlet.http.ByteArrayByteBuffer;
import io.micronaut.servlet.http.ParsedBodyHolder;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpRequest;
import io.micronaut.servlet.http.ServletHttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static io.micronaut.servlet.http.BodyBuilder.isFormSubmission;

/**
 * Implementation of the {@link ServletHttpRequest} interface for Google Cloud Function.
 *
 * @param <B> The body type
 * @author graemerocher
 * @since 1.2.0
 */
@Internal
final class GoogleFunctionHttpRequest<B> implements
    ServletHttpRequest<com.google.cloud.functions.HttpRequest, B>,
    ServletExchange<com.google.cloud.functions.HttpRequest, com.google.cloud.functions.HttpResponse>,
    ServerHttpRequest<B>,
    FullHttpRequest<B>,
    ParsedBodyHolder<B> {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleFunctionHttpRequest.class);

    private final com.google.cloud.functions.HttpRequest googleRequest;
    private final URI uri;
    private final HttpMethod method;
    private final GoogleFunctionHeaders headers;
    private final GoogleFunctionHttpResponse<?> googleResponse;
    private final Supplier<ByteBody> byteBody;
    private MutableHttpParameters httpParameters;
    private MutableConvertibleValues<Object> attributes;
    private B parsedBody;
    private Supplier<Optional<B>> body;
    private GoogleCookies cookies;

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
        BodyBuilder bodyBuilder,
        Executor ioExecutor) {
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
            B built = parsedBody != null ? parsedBody :  (B) bodyBuilder.buildBody(this::getInputStream, this);
            return Optional.ofNullable(built);
        });
        this.byteBody = SupplierUtil.memoized(() -> {
            try {
                return InputStreamByteBody.create(
                    googleRequest.getInputStream(),
                    OptionalLong.of(googleRequest.getContentLength()),
                    ioExecutor,
                    ByteArrayBufferFactory.INSTANCE
                );
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public byte[] getBodyBytes() throws IOException {
        try (CloseableByteBody streaming = byteBody().split(ByteBody.SplitBackpressureMode.FASTEST);
             CloseableAvailableByteBody buffered = streaming.buffer().get()) {
            return buffered.toByteArray();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof IOException ioe) {
                throw ioe;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return byteBody().split(ByteBody.SplitBackpressureMode.FASTEST).toInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
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
        GoogleCookies localCookies = this.cookies;
        if (localCookies == null) {
            synchronized (this) { // double check
                localCookies = this.cookies;
                if (localCookies == null) {
                    localCookies = new GoogleCookies(getPath(), getHeaders(), conversionService);
                    this.cookies = localCookies;
                }
            }
        }
        return localCookies;
    }

    @NonNull
    @Override
    public HttpParameters getParameters() {
        MediaType mediaType = getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
        Map<CharSequence, List<String>> values = new HashMap<>(3);
        values.putAll(googleRequest.getQueryParameters());
        if (isFormSubmission(mediaType)) {
            Map<String, List<String>> parameters = null;
            try {
                parameters = new QueryStringDecoder(new String(getInputStream().readAllBytes(), getCharacterEncoding()), false).parameters();
            } catch (IOException ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Error decoding form data: " + ex.getMessage(), ex);
                }
                parameters = new HashMap<>();
            }
            values.putAll(parameters);
        }
        return new SimpleHttpParameters(values, conversionService);
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

    @Override
    public void setParsedBody(B body) {
        this.parsedBody = body;
    }

    @Override
    public @Nullable ByteBuffer<?> contents() {
        try {
            return new ByteArrayByteBuffer<>(getBodyBytes());
        } catch (IOException e) {
            throw new IllegalStateException("Error getting all body contents", e);
        }
    }

    @Override
    public @Nullable ExecutionFlow<ByteBuffer<?>> bufferContents() {
        return ExecutionFlow.just(contents());
    }

    @Override
    public @NonNull ByteBody byteBody() {
        return byteBody.get();
    }

    /**
     * Models the headers.
     */
    private final class GoogleFunctionHeaders implements MutableHttpHeaders {

        private final CaseInsensitiveMutableHttpHeaders headers;

        GoogleFunctionHeaders(ConversionService conversionService) {
            headers = new CaseInsensitiveMutableHttpHeaders(googleRequest.getHeaders(), conversionService);
        }

        @Override
        public MutableHttpHeaders add(CharSequence header, CharSequence value) {
            headers.add(header, value);
            return this;
        }

        @Override
        public MutableHttpHeaders remove(CharSequence header) {
            ArgumentUtils.requireNonNull("header", header);
            headers.remove(header);
            return this;
        }

        @Override
        public void setConversionService(@NonNull ConversionService conversionService) {
            headers.setConversionService(conversionService);
        }

        @Override
        public List<String> getAll(CharSequence name) {
            return headers.getAll(name);
        }

        @Override
        public @Nullable String get(CharSequence name) {
            return headers.get(name);
        }

        @Override
        public Set<String> names() {
            return headers.names();
        }

        @Override
        public Collection<List<String>> values() {
            return headers.values();
        }

        @Override
        public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
            return headers.get(name, conversionContext);
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
