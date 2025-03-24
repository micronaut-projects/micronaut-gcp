/*
 * Copyright 2017-2025 original authors
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
package io.micronaut.gcp.function.http.test;

import com.google.cloud.functions.HttpRequest;
import com.sun.net.httpserver.HttpExchange;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.uri.QueryStringDecoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.micronaut.http.util.HttpHeadersUtil.parseCharacterEncoding;

@Experimental
@Internal
class HttpExchangeHttpRequest implements HttpRequest {
    private final HttpExchange httpExchange;

    HttpExchangeHttpRequest(HttpExchange httpExchange) {
        this.httpExchange = httpExchange;
    }

    @Override
    public String getMethod() {
        return httpExchange.getRequestMethod();
    }

    @Override
    public String getUri() {
        return httpExchange.getRequestURI().toString();
    }

    @Override
    public String getPath() {
        return httpExchange.getRequestURI().getPath();
    }

    @Override
    public Optional<String> getQuery() {
        return Optional.ofNullable(httpExchange.getRequestURI().getQuery());
    }

    @Override
    public Map<String, List<String>> getQueryParameters() {
        return new QueryStringDecoder(httpExchange.getRequestURI()).parameters();
    }

    @Override
    public Map<String, HttpPart> getParts() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Optional<String> getContentType() {
        return Optional.ofNullable(getHeader(HttpHeaders.CONTENT_TYPE));
    }

    @Override
    public long getContentLength() {
        String value = getHeader(HttpHeaders.CONTENT_LENGTH);
        if (StringUtils.isEmpty(value)) {
            return 0;
        }
        return Long.parseLong(value);
    }

    @Override
    public Optional<String> getCharacterEncoding() {
        return Optional.of(characterEncoding().toString());
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return httpExchange.getRequestBody();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), characterEncoding()));
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return httpExchange.getRequestHeaders();
    }

    @Nullable
    private String getHeader(@NonNull String headerName) {
        return getHeaders().get(headerName).stream().findFirst().orElse(null);
    }

    @NonNull
    private Charset characterEncoding() {
        return parseCharacterEncoding(
            getHeader(HttpHeaders.CONTENT_TYPE),
            getHeader(HttpHeaders.ACCEPT_CHARSET));
    }
}
