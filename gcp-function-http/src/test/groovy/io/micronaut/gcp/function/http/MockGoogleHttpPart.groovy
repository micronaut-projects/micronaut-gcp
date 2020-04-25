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

import java.nio.charset.StandardCharsets

class MockGoogleHttpPart implements HttpRequest.HttpPart {
    final Map<String, List<String>> headers = [:]
    final String fileName
    final String body
    final String contentType

    MockGoogleHttpPart(String fileName, String body, String contentType) {
        this.fileName = fileName
        this.body = body
        this.contentType = contentType
    }

    @Override
    Optional<String> getFileName() {
        return Optional.ofNullable(fileName)
    }

    @Override
    Optional<String> getContentType() {
        return Optional.ofNullable(contentType)
    }

    @Override
    long getContentLength() {
        return body.length()
    }

    @Override
    Optional<String> getCharacterEncoding() {
        return Optional.of(StandardCharsets.UTF_8.name())
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
