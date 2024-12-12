/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.gcp.credentials;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.util.StreamingContent;
import com.google.auth.http.HttpTransportFactory;
import com.google.auth.oauth2.GoogleCredentials;
import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.type.MutableHeaders;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.http.*;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.HttpClientConfiguration;
import io.micronaut.http.client.LoadBalancer;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * An implementation of {@link HttpTransportFactory} based upon {@link HttpClient} that can be supplied
 * when creating {@link GoogleCredentials}, allowing full control over configuration of the client.
 *
 * This transport will log unsuccessful HTTP requests at the {@code WARN} level using Micronaut framework's
 * SLF4J logging infrastructure, giving more visibility into potential errors without needing to bridge the GCP
 * SDK's java.util.logging statements.
 *
 * @author Jeremy Grelle
 * @since 5.4.0
 */
@Singleton
@Requires(classes = HttpClient.class)
@Requires(property = GoogleCredentialsConfiguration.PREFIX + ".use-http-client", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
public class DefaultOAuth2HttpTransportFactory implements HttpTransportFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultOAuth2HttpTransportFactory.class);

    private final Supplier<HttpClientHttpTransport> transport;

    /**
     * Constructor for {@code DefaultOAuth2HttpTransportFactory}.
     *
     * @param beanContext the current bean context
     * @param defaultClientConfiguration the default HTTP client configuration
     */
    public DefaultOAuth2HttpTransportFactory(BeanContext beanContext, HttpClientConfiguration defaultClientConfiguration) {
        this.transport = SupplierUtil.memoized(() -> new HttpClientHttpTransport(beanContext.createBean(HttpClient.class,
            LoadBalancer.empty(),
            defaultClientConfiguration)));
    }

    @Override
    public HttpTransport create() {
        return this.transport.get();
    }

    private static final class HttpClientHttpTransport extends HttpTransport {

        private final HttpClient httpClient;

        public HttpClientHttpTransport(HttpClient httpClient) {
            this.httpClient = httpClient;
        }

        @Override
        protected LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
            return new MutableBlockingLowLevelHttpRequest(httpClient, method, url);
        }
    }

    /**
     * Implementation of {@link LowLevelHttpRequest} that uses the blocking API of {@link HttpClient}.
     *
     * The GCP SDK manages threads for these requests, so they will not block the main Micronaut event loop.
     */
    public static final class MutableBlockingLowLevelHttpRequest extends LowLevelHttpRequest {

        private final BlockingHttpClient httpClient;

        private final MutableHttpRequest<StreamingContent> request;

        private MutableBlockingLowLevelHttpRequest(HttpClient httpClient, String method, String url) {
            this.httpClient = httpClient.toBlocking();
            this.request = HttpRequest.create(HttpMethod.valueOf(method), url);
        }

        @Override
        public void addHeader(String name, String value) throws IOException {
            this.request.header(name, value);
        }

        @Override
        public LowLevelHttpResponse execute() throws IOException {
            //If there is StreamingContent, the setting of these headers must be delayed until the MessageBodyWriter
            if (getStreamingContent() == null) {
                writeContentHeaders(request.getHeaders());
            }
            request.body(this);
            try {
                return new BlockingLowLevelHttpResponse(httpClient.exchange(this.request));
            } catch (HttpClientResponseException ce) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("A {} {} response was received from {} while attempting to retrieve an access token for a GCP API request. " +
                        "The GCP libraries treat this as a retryable error, but misconfigured credentials can keep it from ever " +
                        "succeeding.", ce.getStatus().getCode(), ce.getStatus().getReason(), request.getUri());
                }
                return new BlockingLowLevelHttpResponse(ce.getResponse());
            } catch (Throwable ex) {
                throw new IOException(ex);
            }
        }

        /**
         * Writes the content headers to the underlying {@link MutableHttpRequest}.
         *
         * @param headers The headers of the wrapped {@link MutableHttpRequest}.
         */
        void writeContentHeaders(MutableHeaders headers) {
            if (getContentEncoding() != null) {
                headers.add(HttpHeaders.CONTENT_ENCODING, getContentEncoding());
            }
            if (getContentType() != null) {
                headers.add(HttpHeaders.CONTENT_TYPE, getContentType());
            }
            if (getContentLength() > -1) {
                headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(getContentLength()));
            }
        }
    }

    private static final class BlockingLowLevelHttpResponse extends LowLevelHttpResponse {

        private final HttpResponse<?> bufferedResponse;

        private final List<String> indexedHeaderNames = new ArrayList<>();

        private final List<String> indexedHeaderValues = new ArrayList<>();

        private BlockingLowLevelHttpResponse(HttpResponse<?> response) {
            bufferedResponse = response;
            if (bufferedResponse != null) {
                bufferedResponse.getHeaders().forEach((name, values) -> {
                    indexedHeaderNames.add(name);
                    indexedHeaderValues.add(String.join(",", values));
                });
            }
        }

        @Override
        public InputStream getContent() {
            ByteBuffer<?> body = bufferedResponse.getBody(ByteBuffer.class).orElse(null);
            return body != null ? body.toInputStream() : null;
        }

        @Override
        public String getContentEncoding() {
            return bufferedResponse.getHeaders().get(HttpHeaders.CONTENT_ENCODING);
        }

        @Override
        public long getContentLength() {
            return bufferedResponse.getContentLength();
        }

        @Override
        public String getContentType() {
            return bufferedResponse.getContentType().map(MediaType::toString).orElse(null);
        }

        @Override
        public String getStatusLine() {
            return bufferedResponse.getStatus().getReason();
        }

        @Override
        public int getStatusCode() {
            return bufferedResponse.code();
        }

        @Override
        public String getReasonPhrase() {
            return bufferedResponse.reason();
        }

        @Override
        public int getHeaderCount() {
            return bufferedResponse.getHeaders().asMap().size();
        }

        @Override
        public String getHeaderName(int index) {
            return indexedHeaderNames.get(index);
        }

        @Override
        public String getHeaderValue(int index) {
            return indexedHeaderValues.get(index);
        }
    }
}
