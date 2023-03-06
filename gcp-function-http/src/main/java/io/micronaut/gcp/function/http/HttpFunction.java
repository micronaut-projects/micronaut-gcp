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
package io.micronaut.gcp.function.http;

import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.env.Environment;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.function.executor.FunctionInitializer;
import io.micronaut.http.*;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.netty.cookies.NettyCookie;
import io.micronaut.servlet.http.DefaultServletExchange;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpHandler;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.util.internal.MacAddressUtil;
import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micronaut.core.annotation.NonNull;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Entry point into the Micronaut + GCP integration.
 *
 * @author graemerocher
 * @since 1.2.0
 */
public class HttpFunction extends FunctionInitializer implements com.google.cloud.functions.HttpFunction {

    static {
        byte[] bestMacAddr = new byte[8];
        PlatformDependent.threadLocalRandom().nextBytes(bestMacAddr);
        System.setProperty("io.netty.machineId", MacAddressUtil.formatAddress(bestMacAddr));
    }

    protected static final Logger LOG = LoggerFactory.getLogger(HttpFunction.class);

    private final ServletHttpHandler<HttpRequest, HttpResponse> httpHandler;

    private final ConversionService conversionService;

    /**
     * Default constructor.
     */
    public HttpFunction() {
        httpHandler = initializeHandler();
        this.conversionService = applicationContext.getBean(ConversionService.class);
    }

    public HttpFunction(ApplicationContext context) {
        super(context);
        httpHandler = initializeHandler();
        this.conversionService = applicationContext.getBean(ConversionService.class);
    }

    private ServletHttpHandler<HttpRequest, HttpResponse> initializeHandler() {
        final ServletHttpHandler<HttpRequest, HttpResponse> httpHandler = new ServletHttpHandler<HttpRequest, HttpResponse>(applicationContext) {
            @Override
            protected ServletExchange<HttpRequest, HttpResponse> createExchange(HttpRequest request, HttpResponse response) {
                final GoogleFunctionHttpResponse<Object> res =
                        new GoogleFunctionHttpResponse<>(response, getMediaTypeCodecRegistry(), conversionService);
                final GoogleFunctionHttpRequest<Object> req =
                        new GoogleFunctionHttpRequest<>(request, res, getMediaTypeCodecRegistry(), conversionService);

                return new DefaultServletExchange<>(req, res);
            }

            @Override
            public void service(HttpRequest request, HttpResponse response) {
                ServletExchange<HttpRequest, HttpResponse> exchange = createExchange(request, response);
                service(exchange);
            }
        };

        Runtime.getRuntime().addShutdownHook(
                new Thread(httpHandler::close)
        );
        return httpHandler;
    }

    @Override
    protected void startThis(ApplicationContext applicationContext) {
        final long time = System.currentTimeMillis();
        try {
            super.startThis(applicationContext);
        } finally {
            if (LOG.isInfoEnabled()) {
                LOG.info("Initialized function in: " + (System.currentTimeMillis() - time) + "ms");
            }
        }
    }

    @NonNull
    @Override
    protected ApplicationContextBuilder newApplicationContextBuilder() {
        final ApplicationContextBuilder builder = super.newApplicationContextBuilder();
        builder.deduceEnvironment(false);
        builder.environments(Environment.FUNCTION, Environment.GOOGLE_COMPUTE);
        return builder;
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        httpHandler.service(request, response);
    }

    /**
     * Invoke the function directly with the given request object.
     * @param request The request object
     * @return The response object
     */
    public GoogleHttpResponse invoke(HttpRequest request) {
        HttpResponseImpl response = new HttpResponseImpl(conversionService);
        httpHandler.service(Objects.requireNonNull(request), response);
        return response;
    }

    /**
     * Invoke the function directly with the given request object.
     * @param method The method
     * @param uri The URI
     * @return The response object
     */
    public GoogleHttpResponse invoke(HttpMethod method, String uri) {
        MutableHttpRequest<Object> request = HttpRequestFactory.INSTANCE.create(method, uri);
        HttpResponseImpl response = new HttpResponseImpl(conversionService);
        httpHandler.service(toGoogleRequest(request), response);
        return response;
    }

    /**
     * Invoke the function directly with the given request object.
     * @param method The method
     * @param uri The URI
     * @param body The body
     * @return The response object
     */
    public GoogleHttpResponse invoke(HttpMethod method, String uri, Object body) {
        MutableHttpRequest<Object> request = HttpRequestFactory.INSTANCE.create(method, uri);
        request.body(body);
        HttpResponseImpl response = new HttpResponseImpl(conversionService);
        httpHandler.service(toGoogleRequest(request), response);
        return response;
    }

    /**
     * Invoke the function directly with the given request object.
     * @param request The request object
     * @return The response object
     */
    public GoogleHttpResponse invoke(io.micronaut.http.HttpRequest<?> request) {
        HttpResponseImpl response = new HttpResponseImpl(conversionService);
        httpHandler.service(toGoogleRequest(Objects.requireNonNull(request)), response);
        return response;
    }

    private HttpRequest toGoogleRequest(io.micronaut.http.HttpRequest<?> request) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        Map<String, List<String>> parameters = new LinkedHashMap<>();
        request.getHeaders().forEach(headers::put);
        request.getParameters().forEach(parameters::put);
        Object body = request.getBody().orElse(null);
        try {
            Cookies cookies = request.getCookies();
            cookies.forEach((s, cookie) -> {
                if (cookie instanceof NettyCookie) {
                    headers.computeIfAbsent(HttpHeaders.COOKIE, s1 -> new ArrayList<>())
                            .add(ClientCookieEncoder.STRICT.encode(((NettyCookie) cookie).getNettyCookie()));
                }
            });
        } catch (UnsupportedOperationException e) {
            //not all request types support retrieving cookies
        }
        return new HttpRequest() {
            @Override
            public String getMethod() {
                return request.getMethodName();
            }

            @Override
            public String getUri() {
                return request.getUri().toString();
            }

            @Override
            public String getPath() {
                return request.getPath();
            }

            @Override
            public Optional<String> getQuery() {
                return Optional.of(request.getUri().getQuery());
            }

            @Override
            public Map<String, List<String>> getQueryParameters() {
                return parameters;
            }

            @Override
            public Map<String, HttpPart> getParts() {
                return Collections.emptyMap();
            }

            @Override
            public Optional<String> getContentType() {
                List<String> values = headers.get(HttpHeaders.CONTENT_TYPE);
                if (values != null) {
                    Iterator<String> i = values.iterator();
                    if (i.hasNext()) {
                        return Optional.ofNullable(i.next());
                    }
                }
                return Optional.empty();
            }

            @Override
            public long getContentLength() {
                List<String> values = headers.get(HttpHeaders.CONTENT_LENGTH);
                if (values != null) {
                    Iterator<String> i = values.iterator();
                    if (i.hasNext()) {
                        return Long.parseLong(i.next());
                    }
                }
                return 0;
            }

            @Override
            public Optional<String> getCharacterEncoding() {
                List<String> values = headers.get(HttpHeaders.CONTENT_ENCODING);
                if (values != null) {
                    Iterator<String> i = values.iterator();
                    if (i.hasNext()) {
                        return Optional.ofNullable(i.next());
                    }
                }
                return Optional.empty();
            }

            @Override
            public InputStream getInputStream() {
                if (body != null) {
                    if (body instanceof byte[]) {
                        return new ByteArrayInputStream((byte[]) body);
                    } else {
                        MediaType mediaType = getContentType().map(MediaType::new).orElse(null);
                        if (mediaType != null) {

                            MediaTypeCodec codec = httpHandler.getMediaTypeCodecRegistry().findCodec(
                                    mediaType
                            ).orElse(null);
                            if (codec != null) {
                                byte[] bytes = codec.encode(body);
                                return new ByteArrayInputStream(bytes);
                            }
                        } else {
                            return new ByteArrayInputStream(body.toString().getBytes(StandardCharsets.UTF_8));
                        }
                    }
                }
                return new ByteArrayInputStream(new byte[0]);
            }

            @Override
            public BufferedReader getReader() {
                return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return headers;
            }
        };
    }

    /**
     * Implementation of {@link GoogleHttpResponse}.
     */
    private static class HttpResponseImpl implements GoogleHttpResponse {

        private int statusCode = HttpStatus.OK.getCode();
        private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        private Map<String, List<String>> headers = new LinkedHashMap<>();
        private String message;

        private final ConversionService conversionService;

        private HttpResponseImpl(ConversionService conversionService) {
            this.conversionService = conversionService;
        }

        @Override
        public void setStatusCode(int code) {
            this.statusCode = code;
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public HttpHeaders getHttpHeaders() {
            return new GoogleFunctionHeaders(conversionService);
        }

        @Override
        public String getBodyAsText() {
            return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        }

        @Override
        public <T> Optional<T> getBody(Argument<T> type) {
            return conversionService.convert(
                    outputStream.toByteArray(),
                    Objects.requireNonNull(type, "Type cannot be null")
            );
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public void setStatusCode(int code, String message) {
            this.statusCode = code;
            this.message = message;
        }

        @Override
        public void setContentType(String contentType) {
            ArrayList<String> value = new ArrayList<>();
            value.add(contentType);
            headers.put(HttpHeaders.CONTENT_TYPE, value);
        }

        @Override
        public Optional<String> getContentType() {
            List<String> values = headers.get(HttpHeaders.CONTENT_TYPE);
            if (values != null) {
                Iterator<String> i = values.iterator();
                if (i.hasNext()) {
                    return Optional.ofNullable(i.next());
                }
            }
            return Optional.empty();
        }

        @Override
        public void appendHeader(String header, String value) {
            headers.computeIfAbsent(header, s -> new ArrayList<>()).add(value);
        }

        @Override
        public Map<String, List<String>> getHeaders() {
            return headers;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return outputStream;
        }

        @Override
        public BufferedWriter getWriter() throws IOException {
            return new BufferedWriter(
                    new OutputStreamWriter(getOutputStream(), StandardCharsets.UTF_8)
            );
        }

        /**
         * Function headers impl.
         */
        private final class GoogleFunctionHeaders extends GoogleMultiValueMap implements HttpHeaders {
            GoogleFunctionHeaders(ConversionService conversionService) {
                super(getHeaders());
                setConversionService(conversionService);
            }
        }
    }
}
