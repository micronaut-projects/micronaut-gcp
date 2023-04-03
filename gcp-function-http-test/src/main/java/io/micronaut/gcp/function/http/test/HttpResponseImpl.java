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
package io.micronaut.gcp.function.http.test;

import static java.util.stream.Collectors.toMap;

import com.google.cloud.functions.HttpResponse;
import io.micronaut.core.annotation.Internal;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Forked from the invoker.
 *
 * @link https://github.com/GoogleCloudPlatform/functions-framework-java/blob/master/invoker/core/src/main/java/com/google/cloud/functions/invoker/http/HttpRequestImpl.java
 * @author graemerocher
 * @since 3.0.2
 */
@Internal
final class HttpResponseImpl implements HttpResponse {
    private final HttpServletResponse response;
    private BufferedWriter writer;

    public HttpResponseImpl(HttpServletResponse response) {
        this.response = response;
    }

    @Override
    public void setStatusCode(int code) {
        response.setStatus(code);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setStatusCode(int code, String message) {
        response.setStatus(code, message);
    }

    @Override
    public void setContentType(String contentType) {
        response.setContentType(contentType);
    }

    @Override
    public Optional<String> getContentType() {
        return Optional.ofNullable(response.getContentType());
    }

    @Override
    public void appendHeader(String key, String value) {
        response.addHeader(key, value);
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return response.getHeaderNames().stream()
                .map(header -> new SimpleEntry<>(header, list(response.getHeaders(header))))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static <T> List<T> list(Collection<T> collection) {
        return (collection instanceof List<?>) ? (List<T>) collection : new ArrayList<>(collection);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return response.getOutputStream();
    }

    @Override
    public synchronized BufferedWriter getWriter() throws IOException {
        if (writer == null) {
            // Unfortunately this means that we get two intermediate objects between the object we return
            // and the underlying Writer that response.getWriter() wraps. We could try accessing the
            // PrintWriter.out field via reflection, but that sort of access to non-public fields of
            // platform classes is now frowned on and may draw warnings or even fail in subsequent
            // versions.
            // We could instead wrap the OutputStream, but that would require us to deduce the appropriate
            // Charset, using logic like this:
            // https://github.com/eclipse/jetty.project/blob/923ec38adf/jetty-server/src/main/java/org/eclipse/jetty/server/Response.java#L731
            // We may end up doing that if performance is an issue.
            writer = new BufferedWriter(response.getWriter());
        }
        return writer;
    }
}
