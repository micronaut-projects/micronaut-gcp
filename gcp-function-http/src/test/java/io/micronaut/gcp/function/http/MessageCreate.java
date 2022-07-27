package io.micronaut.gcp.function.http;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;

import javax.validation.constraints.NotBlank;

@Introspected
public class MessageCreate {

    @NonNull
    @NotBlank
    private final String message;

    public MessageCreate(@NonNull String message) {
        this.message = message;
    }

    @NonNull
    public String getMessage() {
        return message;
    }
}
