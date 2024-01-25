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

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.MutableHeaders;
import io.micronaut.http.MediaType;
import io.micronaut.http.body.MessageBodyWriter;
import io.micronaut.http.codec.CodecException;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A {@link MessageBodyWriter} implementation for writing the {@link com.google.api.client.http.LowLevelHttpRequest} that
 * is used for OAuth2 token refresh.
 *
 * @author Jeremy Grelle
 * @since 5.4.0
 */
@Internal
@Experimental
@Requires(beans = DefaultOAuth2HttpTransportFactory.class)
@Singleton
public final class OAuth2MessageBodyWriter implements MessageBodyWriter<DefaultOAuth2HttpTransportFactory.MutableBlockingLowLevelHttpRequest> {

    @Override
    public void writeTo(@NonNull Argument<DefaultOAuth2HttpTransportFactory.MutableBlockingLowLevelHttpRequest> type,
                        @NonNull MediaType mediaType,
                        DefaultOAuth2HttpTransportFactory.MutableBlockingLowLevelHttpRequest httpTransportRequest,
                        @NonNull MutableHeaders outgoingHeaders,
                        @NonNull OutputStream outputStream) throws CodecException {
        httpTransportRequest.writeContentHeaders(outgoingHeaders);
        if (httpTransportRequest.getStreamingContent() != null) {
            try {
                httpTransportRequest.getStreamingContent().writeTo(outputStream);
            } catch (IOException ex) {
                throw new CodecException("StreamingContent failed to write to OutputStream", ex);
            }
        }
    }
}
