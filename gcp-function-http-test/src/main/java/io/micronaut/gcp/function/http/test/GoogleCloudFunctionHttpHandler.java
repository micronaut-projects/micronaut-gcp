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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.gcp.function.http.HttpFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Experimental
@Internal
class GoogleCloudFunctionHttpHandler implements HttpHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GoogleCloudFunctionHttpHandler.class);
    private final HttpFunction httpFunction;
    private boolean headersSent = false;

    GoogleCloudFunctionHttpHandler(HttpFunction httpFunction) {
        this.httpFunction = httpFunction;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpExchangeHttpRequest request = new HttpExchangeHttpRequest(exchange);
        HttpExchangeHttpResponse response = new HttpExchangeHttpResponse(exchange,
            rsp -> {
            sendHeaders(exchange, rsp);
                headersSent = true;
            });
        try {
            httpFunction.service(request, response);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            exchange.sendResponseHeaders(500, 0);
        }
        if (!headersSent) {
            sendHeaders(exchange, response);
        }
        exchange.close();
    }

    void sendHeaders(HttpExchange exchange, HttpExchangeHttpResponse response) {
        response.getHeaders().forEach((name, values) -> {
            exchange.getResponseHeaders().put(name, values);
        });
        try {
            exchange.sendResponseHeaders(response.getStatus(), 0);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
