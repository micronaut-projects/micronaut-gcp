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
