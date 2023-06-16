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
    "io.micronaut.http.server.tck.tests.ErrorHandlerTest", // testCustomGlobalExceptionHandlersForPOSTWithBody fails; cannot get ErrorHandlerTest$RequestObject no String-argument constructor/factory method to deserialize from String value ('{\"numberField\":101}')
    "io.micronaut.http.server.tck.tests.MiscTest", // Bad Request 400 errors
    "io.micronaut.http.server.tck.tests.filter.RequestFilterTest", // Unbindable argument [byte[] bytes] to method [void requestFilterBinding(String contentType,byte[] bytes,FilterContinuation<HttpResponse<Object>> continuation)]
    "io.micronaut.http.server.tck.tests.cors.SimpleRequestWithCorsNotEnabledTest", // Multiple routes are selected
    "io.micronaut.http.server.tck.tests.constraintshandler.ControllerConstraintHandlerTest" // Broken in servlet
})
class GcpFunctionHttpServerTestSuite {
}
