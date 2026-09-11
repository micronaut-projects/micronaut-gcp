/*
 * Copyright 2017-2026 original authors
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
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.multipart.MultipartBody;
import io.micronaut.http.client.multipart.MultipartDataFactory;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpPostMultipartRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Builds the {@link HttpRequest.HttpPart parts} of a {@code multipart/form-data} request for the request
 * implementations that do not come from the Functions Framework invoker, which parses them itself.
 *
 * @author Denis Stepanov
 * @since 6.2.0
 */
@Internal
public final class GoogleMultipartParts {

    private static final boolean MULTIPART_BODY_PRESENT = ClassUtils.isPresent(
        "io.micronaut.http.client.multipart.MultipartBody",
        GoogleMultipartParts.class.getClassLoader()
    );

    private GoogleMultipartParts() {
    }

    /**
     * Parses a {@code multipart/form-data} body. Like the invoker, a request of any other content type has no parts.
     *
     * @param contentType The request content type, including the boundary
     * @param body        The complete request body
     * @return The parts by name
     * @throws IllegalStateException If the content type is not {@code multipart/form-data}
     */
    public static Map<String, HttpRequest.HttpPart> parse(@Nullable String contentType, byte[] body) {
        if (!isMultipartFormData(contentType)) {
            throw new IllegalStateException("Content-Type must be multipart/form-data: " + contentType);
        }
        FullHttpRequest nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/", Unpooled.wrappedBuffer(body));
        nettyRequest.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        HttpPostMultipartRequestDecoder decoder = new HttpPostMultipartRequestDecoder(new DefaultHttpDataFactory(false), nettyRequest, StandardCharsets.UTF_8);
        try {
            Map<String, HttpRequest.HttpPart> parts = new LinkedHashMap<>();
            for (InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                if (data instanceof FileUpload upload) {
                    parts.put(upload.getName(), new InMemoryPart(upload.getFilename(), upload.getContentType(), upload.getCharset(), upload.get()));
                } else if (data instanceof Attribute attribute) {
                    parts.put(attribute.getName(), new InMemoryPart(null, null, attribute.getCharset(), attribute.get()));
                }
            }
            return Collections.unmodifiableMap(parts);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            decoder.destroy();
            nettyRequest.release();
        }
    }

    /**
     * Resolves the parts of a request body that has not been encoded, such as the {@code MultipartBody} of a request
     * passed to {@link HttpFunction#invoke(io.micronaut.http.HttpRequest)}.
     *
     * @param body The request body
     * @return The parts by name, or empty if the body is not a multipart body
     */
    public static Optional<Map<String, HttpRequest.HttpPart>> fromBody(@Nullable Object body) {
        if (MULTIPART_BODY_PRESENT && body != null) {
            return MultipartBodyParts.fromBody(body);
        }
        return Optional.empty();
    }

    private static boolean isMultipartFormData(@Nullable String contentType) {
        return contentType != null && MediaType.MULTIPART_FORM_DATA_TYPE.getName().equalsIgnoreCase(MediaType.of(contentType).getName());
    }

    /**
     * Isolates the references to the optional HTTP client multipart types.
     */
    private static final class MultipartBodyParts implements MultipartDataFactory<MultipartBodyParts.PendingPart> {

        private static final MultipartBodyParts INSTANCE = new MultipartBodyParts();

        static Optional<Map<String, HttpRequest.HttpPart>> fromBody(Object body) {
            if (!(body instanceof MultipartBody multipartBody)) {
                return Optional.empty();
            }
            List<PendingPart> data = multipartBody.getData(INSTANCE);
            Map<String, HttpRequest.HttpPart> parts = new LinkedHashMap<>(data.size());
            for (PendingPart part : data) {
                parts.put(part.name, new InMemoryPart(part.fileName, part.contentType, part.charset, part.content));
            }
            return Optional.of(Collections.unmodifiableMap(parts));
        }

        @Override
        public PendingPart createFileUpload(String name, String filename, MediaType contentType, @Nullable String encoding, @Nullable Charset charset, long length) {
            return new PendingPart(name, filename, contentType.toString(), charset);
        }

        @Override
        public PendingPart createAttribute(String name, String value) {
            PendingPart part = new PendingPart(name, null, null, StandardCharsets.UTF_8);
            part.content = value.getBytes(StandardCharsets.UTF_8);
            return part;
        }

        @Override
        public void setContent(PendingPart fileUploadObject, Object content) throws IOException {
            if (content instanceof byte[] bytes) {
                fileUploadObject.content = bytes;
            } else if (content instanceof File file) {
                fileUploadObject.content = Files.readAllBytes(file.toPath());
            } else if (content instanceof InputStream inputStream) {
                fileUploadObject.content = inputStream.readAllBytes();
            } else {
                throw new IllegalArgumentException("Unsupported multipart content: " + content);
            }
        }

        /**
         * A part whose content is set after it is created.
         */
        private static final class PendingPart {
            private final String name;
            private final @Nullable String fileName;
            private final @Nullable String contentType;
            private final @Nullable Charset charset;
            private byte[] content = new byte[0];

            private PendingPart(String name, @Nullable String fileName, @Nullable String contentType, @Nullable Charset charset) {
                this.name = name;
                this.fileName = fileName;
                this.contentType = contentType;
                this.charset = charset;
            }
        }
    }

    /**
     * A part held in memory.
     *
     * @param fileName    The file name, absent for a form field
     * @param contentType The content type
     * @param charset     The charset
     * @param content     The content
     */
    private record InMemoryPart(@Nullable String fileName,
                                @Nullable String contentType,
                                @Nullable Charset charset,
                                byte[] content) implements HttpRequest.HttpPart {

        @Override
        public Optional<String> getFileName() {
            return Optional.ofNullable(fileName);
        }

        @Override
        public Optional<String> getContentType() {
            return Optional.ofNullable(contentType);
        }

        @Override
        public long getContentLength() {
            return content.length;
        }

        @Override
        public Optional<String> getCharacterEncoding() {
            return Optional.ofNullable(charset).map(Charset::name);
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), charset != null ? charset : StandardCharsets.UTF_8));
        }

        @Override
        public Map<String, List<String>> getHeaders() {
            return contentType == null ? Map.of() : Map.of(HttpHeaders.CONTENT_TYPE, List.of(contentType));
        }
    }
}
