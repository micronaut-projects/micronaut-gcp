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
