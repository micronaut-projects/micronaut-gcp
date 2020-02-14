package io.micronaut.gcp.function.http;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class SimplePojo {
    private String name;

    public SimplePojo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
