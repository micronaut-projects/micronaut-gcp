package io.micronaut.gcp.function.http;

import com.google.cloud.functions.HttpResponse;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.exceptions.HttpStatusException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

/**
 * The response object for Google Cloud Function.
 *
 * @param <B> The body type
 * @since 1.2.0
 * @author graemerocher
 */
@Internal
final class GoogleFunctionHttpResponse<B> implements MutableHttpResponse<B> {

    private final HttpResponse response;
    private MutableConvertibleValues<Object> attributes;
    private B body;
    private HttpStatus status;
    private Map<String, Cookie> cookieMap = null;

    GoogleFunctionHttpResponse(HttpResponse response) {
        this.response = response;
    }

    @Override
    public MutableHttpResponse<B> cookie(Cookie cookie) {
        if (cookie != null) {

            if (cookieMap == null) {
                cookieMap = new LinkedHashMap<>(5);
            }
            cookieMap.put(cookie.getName(), cookie);
        }
        return this;
    }

    public Map<String, Cookie> getCookies() {
        if (cookieMap != null) {
            return cookieMap;
        } else {
            //noinspection unchecked
            return Collections.EMPTY_MAP;
        }
    }

    @Override
    public MutableHttpHeaders getHeaders() {
        return new GoogleFunctionHeaders();
    }

    @Nonnull
    @Override
    public MutableConvertibleValues<Object> getAttributes() {
        MutableConvertibleValues<Object> attributes = this.attributes;
        if (attributes == null) {
            synchronized (this) { // double check
                attributes = this.attributes;
                if (attributes == null) {
                    attributes = new MutableConvertibleValuesMap<>();
                    this.attributes = attributes;
                }
            }
        }
        return attributes;
    }

    @Nonnull
    @Override
    public Optional<B> getBody() {
        return Optional.ofNullable(this.body);
    }

    @Override
    public MutableHttpResponse<B> body(@Nullable B body) {
        if (body instanceof CharSequence) {
            if (!getContentType().isPresent()) {
                contentType(MediaType.TEXT_PLAIN_TYPE);
            }
            try {
                response.getWriter().write(body.toString());
            } catch (IOException e) {
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } else if (body instanceof byte[]) {
            try {
                response.getOutputStream().write((byte[]) body);
            } catch (IOException e) {
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());

            }
        }
        this.body = body;
        return this;
    }

    @Override
    public MutableHttpResponse<B> status(HttpStatus status, CharSequence message) {
        ArgumentUtils.requireNonNull("status", status);
        this.status = status;
        response.setStatusCode(status.getCode(), message != null ? message.toString() : status.getReason());
        return this;
    }

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }

    private final class GoogleFunctionHeaders extends GoogleMultiValueMap implements MutableHttpHeaders {

        private final Map<String, List<String>> headers;

        GoogleFunctionHeaders() {
            super(response.getHeaders());
            this.headers = response.getHeaders();
        }

        @Override
        public MutableHttpHeaders add(CharSequence header, CharSequence value) {
            ArgumentUtils.requireNonNull("header", header);
            if (value != null) {
                response.appendHeader(header.toString(), value.toString());
            } else {
                response.getHeaders().remove(header.toString());
            }
            return this;
        }

        @Override
        public MutableHttpHeaders remove(CharSequence header) {
            ArgumentUtils.requireNonNull("header", header);
            response.getHeaders().remove(header.toString());
            return this;
        }
    }
}
