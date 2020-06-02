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

import com.google.cloud.functions.HttpResponse;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.servlet.http.ServletHttpResponse;
import io.micronaut.http.*;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.netty.cookies.NettyCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * The response object for Google Cloud Function.
 *
 * @param <B> The body type
 * @author graemerocher
 * @since 1.2.0
 */
@Internal
final class GoogleFunctionHttpResponse<B> implements ServletHttpResponse<HttpResponse, B> {

    private final HttpResponse response;
    private final MediaTypeCodecRegistry mediaTypeCodecRegistry;
    private MutableConvertibleValues<Object> attributes;
    private B body;
    private HttpStatus status = HttpStatus.OK;

    /**
     * Default constructor.
     *
     * @param response               The Google response object
     * @param mediaTypeCodecRegistry The media type codec registry
     */
    GoogleFunctionHttpResponse(HttpResponse response, MediaTypeCodecRegistry mediaTypeCodecRegistry) {
        this.response = response;
        this.mediaTypeCodecRegistry = mediaTypeCodecRegistry;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }

    @Override
    public BufferedWriter getWriter() throws IOException {
        return response.getWriter();
    }

    @Override
    public MutableHttpResponse<B> cookie(Cookie cookie) {
        if (cookie instanceof NettyCookie) {
            NettyCookie nettyCookie = (NettyCookie) cookie;
            final String encoded = ServerCookieEncoder.STRICT.encode(nettyCookie.getNettyCookie());
            header(HttpHeaders.SET_COOKIE, encoded);
        }
        return this;
    }

    @Override
    public MutableHttpHeaders getHeaders() {
        return new GoogleFunctionHeaders();
    }

    @Nonnull
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

    @Nonnull
    @Override
    public Optional<B> getBody() {
        return Optional.ofNullable(this.body);
    }

    @Override
    public MutableHttpResponse<B> body(@Nullable B body) {
        if (body instanceof CharSequence) {
            if (!getContentType().isPresent()) {
                contentType(MediaType.TEXT_PLAIN_TYPE);
            }
        }
        this.body = body;
        return this;
    }

    @Override
    public MutableHttpResponse<B> status(HttpStatus status, CharSequence message) {
        ArgumentUtils.requireNonNull("status", status);
        this.status = status;
        response.setStatusCode(status.getCode(), message != null ? message.toString() : status.getReason());
        return this;
    }

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }

    @Override
    public HttpResponse getNativeResponse() {
        return response;
    }

    /**
     * Models the headers.
     */
    private final class GoogleFunctionHeaders extends GoogleMultiValueMap implements MutableHttpHeaders {

        GoogleFunctionHeaders() {
            super(response.getHeaders());
        }

        @Override
        public MutableHttpHeaders add(CharSequence header, CharSequence value) {
            ArgumentUtils.requireNonNull("header", header);
            if (value != null) {
                response.appendHeader(header.toString(), value.toString());
            } else {
                response.getHeaders().remove(header.toString());
            }
            return this;
        }

        @Override
        public MutableHttpHeaders remove(CharSequence header) {
            ArgumentUtils.requireNonNull("header", header);
            response.getHeaders().remove(header.toString());
            return this;
        }
    }
}
