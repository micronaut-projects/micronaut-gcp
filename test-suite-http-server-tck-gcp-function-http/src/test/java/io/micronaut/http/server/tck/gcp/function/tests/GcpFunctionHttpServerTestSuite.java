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
    "io.micronaut.http.server.tck.tests.filter.RequestFilterTest", // Unbindable argument [byte[] bytes] to method [void requestFilterBinding(String contentType,byte[] bytes,FilterContinuation<HttpResponse<Object>> continuation)]
    "io.micronaut.http.server.tck.tests.cors.SimpleRequestWithCorsNotEnabledTest", // Multiple routes are selected
})
class GcpFunctionHttpServerTestSuite {
}
