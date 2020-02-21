package io.micronaut.gcp.function.http;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.function.http.ServerlessCookies;
import io.micronaut.function.http.ServerlessExchange;
import io.micronaut.function.http.ServerlessHttpRequest;
import io.micronaut.function.http.ServerlessHttpResponse;
import io.micronaut.http.*;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookies;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

/**
 * Implementation of the {@link ServerlessHttpRequest} interface for Google Cloud Function.
 *
 * @param <B> The body type
 * @author graemerocher
 * @since 1.2.0
 */
@Internal
final class GoogleFunctionHttpRequest<B> implements ServerlessHttpRequest<com.google.cloud.functions.HttpRequest, B>, ServerlessExchange<com.google.cloud.functions.HttpRequest, com.google.cloud.functions.HttpResponse> {
    private final com.google.cloud.functions.HttpRequest googleRequest;
    private final URI uri;
    private final HttpMethod method;
    private final GoogleFunctionHeaders headers;
    private final GoogleFunctionHttpResponse<?> googleResponse;
    private final MediaTypeCodecRegistry codecRegistry;
    private HttpParameters httpParameters;
    private MutableConvertibleValues<Object> attributes;
    private Object body;
    private ServerlessCookies cookies;

    /**
     * Default constructor.
     *
     * @param googleRequest  The native google request
     * @param googleResponse The {@link GoogleFunctionHttpResponse} object
     * @param codecRegistry  The codec registry
     */
    GoogleFunctionHttpRequest(
            com.google.cloud.functions.HttpRequest googleRequest,
            GoogleFunctionHttpResponse<?> googleResponse,
            MediaTypeCodecRegistry codecRegistry) {
        this.googleRequest = googleRequest;
        this.googleResponse = googleResponse;
        this.uri = URI.create(googleRequest.getUri());
        HttpMethod method;
        try {
            method = HttpMethod.valueOf(googleRequest.getMethod());
        } catch (IllegalArgumentException e) {
            method = HttpMethod.CUSTOM;
        }
        this.method = method;
        this.headers = new GoogleFunctionHeaders();
        this.codecRegistry = codecRegistry;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return googleRequest.getInputStream();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return googleRequest.getReader();
    }

    @Override
    public com.google.cloud.functions.HttpRequest getNativeRequest() {
        return googleRequest;
    }

    /**
     * Reference to the response object.
     *
     * @return The response.
     */
    GoogleFunctionHttpResponse<?> getGoogleResponse() {
        return googleResponse;
    }

    @Nonnull
    @Override
    public Cookies getCookies() {
        ServerlessCookies cookies = this.cookies;
        if (cookies == null) {
            synchronized (this) { // double check
                cookies = this.cookies;
                if (cookies == null) {
                    cookies = new ServerlessCookies(getPath(), getHeaders(), ConversionService.SHARED);
                    this.cookies = cookies;
                }
            }
        }
        return cookies;
    }

    @Nonnull
    @Override
    public HttpParameters getParameters() {
        HttpParameters httpParameters = this.httpParameters;
        if (httpParameters == null) {
            synchronized (this) { // double check
                httpParameters = this.httpParameters;
                if (httpParameters == null) {
                    httpParameters = new GoogleFunctionParameters();
                    this.httpParameters = httpParameters;
                }
            }
        }
        return httpParameters;
    }

    @Nonnull
    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @Nonnull
    @Override
    public URI getUri() {
        return this.uri;
    }

    @Nonnull
    @Override
    public HttpHeaders getHeaders() {
        return headers;
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
        return (Optional<B>) getBody(Argument.STRING);
    }

    @Nonnull
    @Override
    public <T> Optional<T> getBody(@Nonnull Argument<T> arg) {
        if (arg != null) {
            final Class<T> type = arg.getType();
            final MediaType contentType = getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
            if (body == null) {

                if (isFormSubmission(contentType)) {
                    body = getParameters();
                    if (ConvertibleValues.class == type) {
                        return (Optional<T>) Optional.of(body);
                    } else {
                        return Optional.empty();
                    }
                } else {

                    final MediaTypeCodec codec = codecRegistry.findCodec(contentType, type).orElse(null);
                    if (codec != null) {
                        try (InputStream inputStream = googleRequest.getInputStream()) {
                            if (ConvertibleValues.class == type) {
                                final Map map = codec.decode(Map.class, inputStream);
                                body = ConvertibleValues.of(map);
                                return (Optional<T>) Optional.of(body);
                            } else {
                                final T value = codec.decode(arg, inputStream);
                                body = value;
                                return Optional.ofNullable(value);
                            }
                        } catch (IOException e) {
                            throw new CodecException("Error decoding request body: " + e.getMessage(), e);
                        }

                    }
                }
            } else {
                if (type.isInstance(body)) {
                    return (Optional<T>) Optional.of(body);
                } else {
                    if (body != httpParameters) {
                        final T result = ConversionService.SHARED.convertRequired(body, arg);
                        return Optional.ofNullable(result);
                    }
                }

            }
        }
        return Optional.empty();
    }

    private boolean isFormSubmission(MediaType contentType) {
        return MediaType.MULTIPART_FORM_DATA_TYPE.equals(contentType) || MediaType.MULTIPART_FORM_DATA_TYPE.equals(contentType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServerlessHttpRequest<com.google.cloud.functions.HttpRequest, ? super Object> getRequest() {
        return (ServerlessHttpRequest) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServerlessHttpResponse<com.google.cloud.functions.HttpResponse, ? super Object> getResponse() {
        return (ServerlessHttpResponse<com.google.cloud.functions.HttpResponse, ? super Object>) googleResponse;
    }

    /*
     * TODO: Copied from Micronaut AWS. Find a way to share this code
     */
    private static String crlf(String s) {
        if (s == null) {
            return null;
        }
        return s.replaceAll("[\r\n]", "");
    }

    /**
     * Models the http parameters.
     */
    private final class GoogleFunctionParameters extends GoogleMultiValueMap implements HttpParameters {
        GoogleFunctionParameters() {
            super(googleRequest.getQueryParameters());
        }

        @Override
        public Optional<String> getFirst(CharSequence name) {
            ArgumentUtils.requireNonNull("name", name);
            return googleRequest.getFirstQueryParameter(name.toString());
        }

        @Nullable
        @Override
        public String get(CharSequence name) {
            return getFirst(name).orElse(null);
        }
    }

    /**
     * Models the headers.
     */
    private final class GoogleFunctionHeaders extends GoogleMultiValueMap implements HttpHeaders {
        GoogleFunctionHeaders() {
            super(googleRequest.getHeaders());
        }
    }
}
