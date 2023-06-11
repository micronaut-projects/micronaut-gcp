package io.micronaut.http.server.tck.gcp.function.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for GCP Function HTTP Test")
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.BodyTest", // Body is returned as '{"scanAvailable":true,"prefetch":-1}'
    "io.micronaut.http.server.tck.tests.FluxTest", // Body is returned as '{"scanAvailable":true,"prefetch":-1}'
    "io.micronaut.http.server.tck.tests.MiscTest", // Fails when there's no Body annotation
    "io.micronaut.http.server.tck.tests.filter.RequestFilterExceptionHandlerTest", // Request is immutable
    "io.micronaut.http.server.tck.tests.filter.RequestFilterTest", // Internal error and Request is immutable
    "io.micronaut.http.server.tck.tests.filter.ResponseFilterTest", // Request is immutable
    "io.micronaut.http.server.tck.tests.endpoints.health.HealthTest", // Caused by: org.eclipse.jetty.http.BadMessageException: 500: No version
    "io.micronaut.http.server.tck.tests.HeadersTest", // Error 400, bad request
    "io.micronaut.http.server.tck.tests.codec.JsonCodecAdditionalTypeTest", // https://github.com/micronaut-projects/micronaut-core/pull/9308
    "io.micronaut.http.server.tck.tests.StreamTest", // Fails in Servlet https://github.com/micronaut-projects/micronaut-servlet/pull/482
    "io.micronaut.http.server.tck.tests.codec.JsonCodeAdditionalTypeTest",  // https://github.com/micronaut-projects/micronaut-core/pull/9419
    "io.micronaut.http.server.tck.tests.constraintshandler.ControllerConstraintHandlerTest" // Broken in servlet

})
class GcpFunctionHttpTestServerTestSuite {
}
