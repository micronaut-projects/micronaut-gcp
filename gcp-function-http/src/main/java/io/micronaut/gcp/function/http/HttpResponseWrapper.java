/*
 * Copyright 2017-2021 original authors
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
import io.micronaut.http.HttpStatus;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class HttpResponseWrapper implements HttpResponse {

    private final HttpResponse response;
    private HttpStatus httpStatus = HttpStatus.OK;

    HttpResponseWrapper(HttpResponse response) {
        this.response = response;
    }

    @Override
    public void setStatusCode(int code) {
        this.httpStatus = HttpStatus.valueOf(code);
        response.setStatusCode(code);
    }

    @Override
    public void setStatusCode(int code, String message) {
        this.httpStatus = HttpStatus.valueOf(code);
        response.setStatusCode(code, message);
    }

    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public void setContentType(String contentType) {
        response.setContentType(contentType);
    }

    @Override
    public Optional<String> getContentType() {
        return response.getContentType();
    }

    @Override
    public void appendHeader(String header, String value) {
        response.appendHeader(header, value);
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return response.getHeaders();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }

    @Override
    public BufferedWriter getWriter() throws IOException {
        return response.getWriter();
    }
}
