package io.micronaut.http.server.tck.gcp.function.tests;

import io.micronaut.core.value.OptionalMultiValues;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.hateoas.GenericResource;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import io.micronaut.http.hateoas.Resource;
import io.micronaut.http.server.tck.tests.hateoas.JsonErrorTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static io.micronaut.http.tck.TestScenario.asserts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is a replacement for {@link JsonErrorTest} in {@code micronaut-http-server-tck-gcp-function-http-test}.
 * The link href is different in the response.
 * */
@SuppressWarnings({
    "java:S5960", // We're allowed assertions, as these are used in tests only
    "checkstyle:MissingJavadocType",
    "checkstyle:DesignForExtension"
})
class JsonErrorTestReplacement {

    private static final String SPEC_NAME = "JsonErrorTest";

    @Test
    void responseCanBeBoundToJsonError() throws IOException {
        asserts(SPEC_NAME,
            HttpRequest.GET("/jsonError"),
            (server, request) -> {
                Executable e = () -> server.exchange(request, JsonError.class);
                HttpClientResponseException ex = Assertions.assertThrows(HttpClientResponseException.class, e);
                Optional<JsonError> jsonErrorOptional = ex.getResponse().getBody(JsonError.class);
                assertTrue(jsonErrorOptional.isPresent());
                JsonError jsonError = jsonErrorOptional.get();
                assertEquals("Not Found", jsonError.getMessage());
                OptionalMultiValues<Link> links = jsonError.getLinks();
                assertFalse(links.isEmpty());
                links.getFirst("self").ifPresent(link -> {
                    // this is the difference... The link href is prefixed by the server URL
                    Assertions.assertEquals("http://localhost:" + server.getPort().get() + "/jsonError", link.getHref());
                    assertFalse(link.isTemplated());
                });
                OptionalMultiValues<Resource> resourceOptionalMultiValues = jsonError.getEmbedded();
                assertFalse(resourceOptionalMultiValues.isEmpty());

                Optional<List<Resource>> errorsOptional = resourceOptionalMultiValues.get("errors");
                assertTrue(errorsOptional.isPresent());
                List<Resource> resources = errorsOptional.get();
                Optional<GenericResource> genericResourceOptional = resources.stream()
                    .filter(resource -> resource instanceof GenericResource)
                    .map(GenericResource.class::cast)
                    .findFirst();
                assertTrue(genericResourceOptional.isPresent());
                assertEquals("Page Not Found", genericResourceOptional.get().getAdditionalProperties().get("message"));
            });
    }
}
