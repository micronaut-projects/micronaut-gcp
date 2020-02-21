package io.micronaut.gcp.function.http;

import com.google.cloud.functions.HttpResponse;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.function.http.ServerlessHttpResponse;
import io.micronaut.http.*;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.netty.cookies.NettyCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * The response object for Google Cloud Function.
 *
 * @param <B> The body type
 * @author graemerocher
 * @since 1.2.0
 */
@Internal
final class GoogleFunctionHttpResponse<B> implements ServerlessHttpResponse<HttpResponse, B> {

    private final HttpResponse response;
    private final MediaTypeCodecRegistry mediaTypeCodecRegistry;
    private MutableConvertibleValues<Object> attributes;
    private B body;
    private HttpStatus status;

    /**
     * Default constructor.
     *
     * @param response               The Google response object
     * @param mediaTypeCodecRegistry The media type codec registry
     */
    GoogleFunctionHttpResponse(HttpResponse response, MediaTypeCodecRegistry mediaTypeCodecRegistry) {
        this.response = response;
        this.mediaTypeCodecRegistry = mediaTypeCodecRegistry;
    }

    @Override
    public MutableHttpResponse<B> cookie(Cookie cookie) {
        if (cookie instanceof NettyCookie) {
            NettyCookie nettyCookie = (NettyCookie) cookie;
            final String encoded = ServerCookieEncoder.LAX.encode(nettyCookie.getNettyCookie());
            header(HttpHeaders.SET_COOKIE, encoded);
        }
        return this;
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
                final BufferedWriter writer = response.getWriter();
                writer.write(body.toString());
                writer.flush();
            } catch (IOException e) {
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
            }
        } else if (body instanceof byte[]) {
            try {
                final OutputStream outputStream = response.getOutputStream();
                outputStream.write((byte[]) body);
                outputStream.flush();
            } catch (IOException e) {
                throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());

            }
        } else if (body != null) {
            final MediaType ct = getContentType().orElse(null);
            final MediaTypeCodec codec = ct != null ? mediaTypeCodecRegistry.findCodec(ct, body.getClass()).orElse(null) : null;
            if (codec != null) {
                try {
                    final OutputStream outputStream = response.getOutputStream();
                    codec.encode(body, outputStream);
                    outputStream.flush();
                } catch (Throwable e) {
                    throw new CodecException("Failed to encode object [" + body + "] to content type [" + ct + "]: " + e.getMessage(), e);
                }
            } else {
                if (ct == null) {
                    try {
                        final BufferedWriter writer = response.getWriter();
                        writer.write(body.toString());
                        writer.flush();
                    } catch (IOException e) {
                        throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
                    }
                } else {
                    throw new CodecException("No codec present capable of encoding object [" + body + "] to content type [" + ct + "]");
                }
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

    @Override
    public HttpResponse getNativeResponse() {
        return response;
    }

    /**
     * Models the headers.
     */
    private final class GoogleFunctionHeaders extends GoogleMultiValueMap implements MutableHttpHeaders {

        GoogleFunctionHeaders() {
            super(response.getHeaders());
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
