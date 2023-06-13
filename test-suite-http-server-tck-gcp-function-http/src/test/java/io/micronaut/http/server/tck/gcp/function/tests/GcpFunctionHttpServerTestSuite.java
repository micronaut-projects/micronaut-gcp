package io.micronaut.http.server.tck.gcp.function.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages({
    "io.micronaut.http.server.tck.tests",
    "io.micronaut.http.server.tck.gcp.function.tests"
})
@SuiteDisplayName("HTTP Server TCK for for GCP Function HTTP")
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.BodyTest", // Bad Request 400 errors
    "io.micronaut.http.server.tck.tests.ConsumesTest", // Bad Request 400 errors
    "io.micronaut.http.server.tck.tests.CookiesTest", // Cookies are not handled, I suspect as it's not implemented in the NettyClientHttpRequest
    "io.micronaut.http.server.tck.tests.ErrorHandlerTest", // testCustomGlobalExceptionHandlersForPOSTWithBody fails; cannot get ErrorHandlerTest$RequestObject no String-argument constructor/factory method to deserialize from String value ('{\"numberField\":101}')
    "io.micronaut.http.server.tck.tests.MiscTest", // Bad Request 400 errors
    "io.micronaut.http.server.tck.tests.filter.RequestFilterExceptionHandlerTest", // Request is immutable
    "io.micronaut.http.server.tck.tests.filter.RequestFilterTest", // Request is immutable
    "io.micronaut.http.server.tck.tests.filter.ResponseFilterTest", // Request is immutable
    "io.micronaut.http.server.tck.tests.endpoints.health.HealthTest", // error Service Unavailable (503)
    "io.micronaut.http.server.tck.tests.HeadersTest", // Error 400, bad request
    "io.micronaut.http.server.tck.tests.cors.SimpleRequestWithCorsNotEnabledTest", // Multiple routes are selected
    "io.micronaut.http.server.tck.tests.codec.JsonCodecAdditionalTypeTest", // https://github.com/micronaut-projects/micronaut-core/pull/9308
    "io.micronaut.http.server.tck.tests.StreamTest", // Fails in Servlet https://github.com/micronaut-projects/micronaut-servlet/pull/482
    "io.micronaut.http.server.tck.tests.constraintshandler.ControllerConstraintHandlerTest" // Broken in servlet
})
class GcpFunctionHttpServerTestSuite {
}
