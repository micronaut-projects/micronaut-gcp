/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.gcp.function.http

import com.google.cloud.functions.HttpResponse
import io.micronaut.http.HttpHeaders

import java.nio.charset.StandardCharsets

class MockGoogleResponse implements HttpResponse {

    int statusCode = 200
    String message
    Map<String, List<String>> headers = [:]
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

    @Override
    void setStatusCode(int code, String message) {
        this.statusCode = code
        this.message = message
    }

    @Override
    void setContentType(String contentType) {
        appendHeader(HttpHeaders.CONTENT_TYPE, contentType)
    }

    @Override
    Optional<String> getContentType() {
        return Optional.ofNullable(
                headers[HttpHeaders.CONTENT_TYPE]?.stream()?.findFirst()?.orElse(null)
        )
    }

    @Override
    void appendHeader(String header, String value) {
        headers.computeIfAbsent(header, { String s -> []})
               .add(value)
    }

    @Override
    Map<String, List<String>> getHeaders() {
        return headers
    }

    @Override
    BufferedWriter getWriter() throws IOException {
        return new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))
    }

    String getText() {
        return new String(outputStream.toByteArray(), StandardCharsets.UTF_8)
    }
}
