package io.micronaut.http.server.tck.gcp.function;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.tck.ServerUnderTest;
import io.micronaut.http.tck.ServerUnderTestProvider;

import java.util.Map;

public class GcpFunctionHttpServerUnderTestProvider implements ServerUnderTestProvider {

    @NonNull
    @Override
    public ServerUnderTest getServer(Map<String, Object> properties) {
        return new GcpFunctionHttpServerUnderTest(properties);
    }
}
