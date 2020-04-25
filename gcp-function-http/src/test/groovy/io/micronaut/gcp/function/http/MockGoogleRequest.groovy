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

import com.google.cloud.functions.HttpRequest
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod

import java.nio.charset.StandardCharsets

class MockGoogleRequest implements HttpRequest {

    final String method
    final URI uri
    final Map<String, List<String>> headers = [:]
    final Map<String, List<String>> parameters = [:]
    final Map<String, HttpPart> parts = [:]
    final String body

    MockGoogleRequest(HttpMethod method, String uri) {
        this(method, uri, "")
    }

    MockGoogleRequest(HttpMethod method, String uri, String body) {
        this.method = method.toString()
        this.uri = URI.create(uri)
        this.body = body
    }

    void addHeader(String name, String val) {
        headers.computeIfAbsent(name, { String n -> []})
               .add(val)
    }

    void addParameter(String name, String val) {
        parameters.computeIfAbsent(name, { String n -> []})
                .add(val)
    }

    @Override
    String getMethod() {
        return method.toString()
    }

    @Override
    String getUri() {
        return uri.toString()
    }

    @Override
    String getPath() {
        return uri.path
    }

    @Override
    Optional<String> getQuery() {
        return Optional.ofNullable(uri.query)
    }

    @Override
    Map<String, List<String>> getQueryParameters() {
        return parameters
    }

    @Override
    Map<String, HttpPart> getParts() {
        return parts
    }

    @Override
    Optional<String> getContentType() {
        return resolveHeaderValue(HttpHeaders.CONTENT_TYPE)
    }

    private Optional<String> resolveHeaderValue(String header) {
        def values = headers[header]
        if (values) {
            return Optional.ofNullable(values[0])
        }
        Optional.empty()
    }

    @Override
    long getContentLength() {
        return resolveHeaderValue(HttpHeaders.CONTENT_LENGTH)
                    .map({s -> Long.valueOf(s)})
                    .orElse(0)
    }

    @Override
    Optional<String> getCharacterEncoding() {
        return resolveHeaderValue(HttpHeaders.CONTENT_ENCODING)
    }

    @Override
    InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(body.bytes)
    }

    @Override
    BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding().orElse(StandardCharsets.UTF_8.toString())))
    }
}
