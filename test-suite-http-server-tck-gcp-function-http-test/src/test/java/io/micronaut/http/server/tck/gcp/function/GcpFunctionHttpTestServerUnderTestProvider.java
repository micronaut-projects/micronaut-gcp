package io.micronaut.http.server.tck.gcp.function;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.server.tck.ServerUnderTest;
import io.micronaut.http.server.tck.ServerUnderTestProvider;

import java.util.Map;

public class GcpFunctionHttpTestServerUnderTestProvider implements ServerUnderTestProvider {

    @NonNull
    @Override
    public ServerUnderTest getServer(Map<String, Object> properties) {
        return new GcpFunctionHttpTestServerUnderTest(properties);
    }
}
