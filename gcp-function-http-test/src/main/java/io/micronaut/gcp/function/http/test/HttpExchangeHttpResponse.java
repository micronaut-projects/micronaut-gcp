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

import com.google.cloud.functions.HttpResponse;
import com.sun.net.httpserver.HttpExchange;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpStatus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.function.Consumer;

@Experimental
@Internal
class HttpExchangeHttpResponse implements HttpResponse {
    private final HttpExchange httpExchange;
    private int statusCode = HttpStatus.OK.getCode();
    private String statusMessage;
    private Map<String, List<String>> headers = new HashMap<>();
    private final Consumer<HttpExchangeHttpResponse> beforeWriteConsumer;

    HttpExchangeHttpResponse(HttpExchange httpExchange,
                             Consumer<HttpExchangeHttpResponse> beforeWriteConsumer) {
        this.httpExchange = httpExchange;
        this.beforeWriteConsumer = beforeWriteConsumer;
    }

    @Override
    public void setStatusCode(int i) {
        this.statusCode = i;

    }

    @Override
    public void setStatusCode(int i, String s) {
        setStatusCode(i);
        this.statusMessage = s;
    }

    @Override
    public void setContentType(String s) {
        headers.put(HttpHeaders.CONTENT_TYPE, List.of(s));
    }

    @Override
    public Optional<String> getContentType() {
        return Optional.ofNullable(headers.get(HttpHeaders.CONTENT_TYPE))
            .filter(l -> !l.isEmpty())
            .map(l -> l.get(0));
    }

    @Override
    public void appendHeader(String s, String s1) {
        if (headers.containsKey(s)) {
            headers.get(s).add(s);
        } else {
            List<String> values = new ArrayList<>();
            values.add(s1);
            headers.put(s, values);
        }
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        beforeWriteConsumer.accept(this);
        return httpExchange.getResponseBody();
    }

    @Override
    public BufferedWriter getWriter() throws IOException {
        return new BufferedWriter(new OutputStreamWriter(getOutputStream()));
    }

    /**
     *
     * @return HTTP Status Code
     */
    public int getStatus() {
        return statusCode;
    }
}
