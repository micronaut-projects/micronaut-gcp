package io.micronaut.gcp.function.http.test;

import com.google.cloud.functions.HttpRequest;
import com.sun.net.httpserver.HttpExchange;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.MediaType;
import io.micronaut.http.util.HttpHeadersUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        //TODO Use QueryStringDecoder
        return Map.of();
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

    public Charset characterEncoding() {
        return parseCharacterEncoding(
            getHeader(HttpHeaders.CONTENT_TYPE),
            getHeader(HttpHeaders.ACCEPT_CHARSET));
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

    // Delete the following methods once this PR is merged in core: https://github.com/micronaut-projects/micronaut-core/pull/11670
    /**
     * Resolve the {@link Charset} to use for request identified by the Content-Type HTTP Header value and the Accept-Charset HTTP Header value.
     *
     * @param contentTypeHeaderValue Content-Type HTTP Header Value
     * @param acceptCharsetHeaderValue Accept-Charset HTTP Header Value
     * @return A {@link Charset}
     * @since 4.8.8
     */
    @NonNull
    public static Charset parseCharacterEncoding(@Nullable String contentTypeHeaderValue, @Nullable String acceptCharsetHeaderValue) {
        MediaType contentType = contentTypeHeaderValue == null ? null : MediaType.of(contentTypeHeaderValue);
        Charset charset = acceptCharsetHeaderValue != null ? parseAcceptCharset(acceptCharsetHeaderValue) : StandardCharsets.UTF_8;
        return parseCharacterEncoding(contentType, charset);
    }

    /**
     * Resolve the {@link Charset} to use for the request.
     *
     * @param contentType ContenType
     * @return An {@link Optional} of {@link Charset}
     * @since 4.8.8
     */
    @NonNull
    public static Charset parseCharacterEncoding(@Nullable MediaType contentType,
                                                 @NonNull Charset acceptCharset) {
        try {

            if (contentType != null) {
                String charset = contentType.getParametersMap().get(MediaType.CHARSET_PARAMETER);
                if (charset != null) {
                    try {
                        return Charset.forName(charset);
                    } catch (Exception e) {
                        // unsupported charset, default to UTF-8
                        return Charset.defaultCharset();
                    }
                }
            }
        } catch (UnsupportedCharsetException e) {
            return StandardCharsets.UTF_8;
        }
        return acceptCharset;
    }

    /**
     *
     * @param acceptCharsetHeaderValue Accept-Charset HeaderValue
     * @return Accept Charset
     * @since 4.8.8
     */
    @NonNull
    public static Charset parseAcceptCharset(@NonNull String acceptCharsetHeaderValue) {
        String text = HttpHeadersUtil.splitAcceptHeader(acceptCharsetHeaderValue);
        if (text != null) {
            try {
                return Charset.forName(text);
            } catch (Exception ignored) {
            }
        }
        return StandardCharsets.UTF_8;
    }
}
