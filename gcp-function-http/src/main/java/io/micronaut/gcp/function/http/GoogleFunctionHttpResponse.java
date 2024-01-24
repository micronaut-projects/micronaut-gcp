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
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.ServerCookieEncoder;
import io.micronaut.servlet.http.ServletHttpResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;

/**
 * The response object for Google Cloud Function.
 *
 * @param <B> The body type
 * @author graemerocher
 * @since 1.2.0
 */
@Internal
final class GoogleFunctionHttpResponse<B> implements ServletHttpResponse<HttpResponse, B> {

    private final HttpResponseWrapper response;
    private final MediaTypeCodecRegistry mediaTypeCodecRegistry;

    private final ConversionService conversionService;
    private MutableConvertibleValues<Object> attributes;
    private B body;
    private int status = HttpStatus.OK.getCode();
    private String reason = HttpStatus.OK.getReason();

    /**
     * Default constructor.
     *
     * @param response               The Google response object
     * @param mediaTypeCodecRegistry The media type codec registry
     */
    GoogleFunctionHttpResponse(HttpResponse response, MediaTypeCodecRegistry mediaTypeCodecRegistry, ConversionService conversionService) {
        this.response = new HttpResponseWrapper(response);
        this.mediaTypeCodecRegistry = mediaTypeCodecRegistry;
        this.conversionService = conversionService;
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
        ServerCookieEncoder.INSTANCE.encode(cookie).forEach(c -> header(HttpHeaders.SET_COOKIE, c));
        return this;
    }

    @Override
    public MutableHttpHeaders getHeaders() {
        return new GoogleFunctionHeaders(conversionService);
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
        return Optional.ofNullable(this.body);
    }

    @Override
    public <T> MutableHttpResponse<T> body(@Nullable T body) {
        this.body = (B) body;
        return (MutableHttpResponse<T>) this;
    }

    @Override
    public MutableHttpResponse<B> status(int status, CharSequence message) {
        this.status = status;
        if (message == null) {
            this.reason = HttpStatus.getDefaultReason(status);
        } else {
            this.reason = message.toString();
        }
        response.setStatusCode(status, reason);
        return this;
    }

    @Override
    public int code() {
        return status;
    }

    @Override
    public String reason() {
        return reason;
    }

    @Override
    public HttpResponse getNativeResponse() {
        return response;
    }

    /**
     * Models the headers.
     */
    private final class GoogleFunctionHeaders extends GoogleMultiValueMap implements MutableHttpHeaders {

        GoogleFunctionHeaders(ConversionService conversionService) {
            super(response.getHeaders());
            setConversionService(conversionService);
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

        @Override
        public void setConversionService(ConversionService conversionService) {
            super.setConversionService(conversionService);
        }
    }
}
