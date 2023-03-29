package io.micronaut.http.server.tck.gcp.function.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for for GCP Function HTTP")
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.BodyTest", // Bad Request 400 errors
    "io.micronaut.http.server.tck.tests.ConsumesTest", // Bad Request 400 errors
    "io.micronaut.http.server.tck.tests.CookiesTest", // Cookies are not handled, I suspect as it's not implemented in the NettyClientHttpRequest
    "io.micronaut.http.server.tck.tests.ErrorHandlerTest", // testCustomGlobalExceptionHandlersForPOSTWithBody fails; cannot get ErrorHandlerTest$RequestObject no String-argument constructor/factory method to deserialize from String value ('{\"numberField\":101}')
    "io.micronaut.http.server.tck.tests.FluxTest", // Body is returned as '{"scanAvailable":true,"prefetch":-1}'
    "io.micronaut.http.server.tck.tests.MiscTest", // Bad Request 400 errors
    "io.micronaut.http.server.tck.tests.OctetTest", // Received body contains 512 bytes, expected 256 (encoding type?)
    "io.micronaut.http.server.tck.tests.filter.ClientRequestFilterTest", // Multiple errors, mostly 404s
    "io.micronaut.http.server.tck.tests.filter.ClientResponseFilterTest", // Multiple errors
    "io.micronaut.http.server.tck.tests.filter.RequestFilterExceptionHandlerTest", // Request is immutable
    "io.micronaut.http.server.tck.tests.filter.RequestFilterTest", // Request is immutable
    "io.micronaut.http.server.tck.tests.filter.ResponseFilterTest", // Request is immutable
    "io.micronaut.http.server.tck.tests.endpoints.health.HealthTest", // error Service Unavailable (503)
    "io.micronaut.http.server.tck.tests.HeadersTest", // Error 400, bad request
    "io.micronaut.http.server.tck.tests.cors.SimpleRequestWithCorsNotEnabledTest", // Multiple routes are selected
})
class GcpFunctionHttpServerTestSuite {
}
