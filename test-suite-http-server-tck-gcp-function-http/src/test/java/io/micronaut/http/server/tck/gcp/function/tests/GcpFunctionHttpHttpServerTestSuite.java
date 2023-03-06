package io.micronaut.http.server.tck.gcp.function.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.http.server.tck.tests")
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.MiscTest", // Fails when there's no Body annotation
    "io.micronaut.http.server.tck.tests.filter.HttpServerFilterTest",
    "io.micronaut.http.server.tck.tests.FiltersTest",
})
@SuiteDisplayName("HTTP Server TCK for for GCP Function HTTP")
class GcpFunctionHttpHttpServerTestSuite {
}
