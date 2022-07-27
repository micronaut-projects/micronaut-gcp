package io.micronaut.gcp.function.http;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;

import javax.validation.constraints.NotBlank;

@Introspected
public class MyResponse {
    @NonNull
    @NotBlank
    private final String greeting;

    public MyResponse(@NonNull String greeting) {
        this.greeting = greeting;
    }

    @NonNull
    public String getGreeting() {
        return greeting;
    }
}
