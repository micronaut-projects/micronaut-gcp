/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.gcp.function.http;

import com.google.cloud.functions.HttpResponse;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpStatus;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Extended interface used for testing.
 *
 * @author graemerocher
 * @since 2.0.0
 */
public interface GoogleHttpResponse extends HttpResponse {
    /**
     * @return The status code
     */
    int getStatusCode();

    /**
     * @return The headers
     */
    HttpHeaders getHttpHeaders();

    /**
     * @return The body as text
     */
    String getBodyAsText();

    /**
     * @return The body as the given type
     * @param type  The type required
     * @param <T> The body type
     */
    <T> Optional<T> getBody(Argument<T> type);

    /**
     * @return The status message
     */
    @Nullable String getMessage();

    /**
     * @return The HTTP status
     */
    default HttpStatus getStatus() {
        return HttpStatus.valueOf(getStatusCode());
    }
}
