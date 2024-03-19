package io.micronaut.http.server.tck.gcp.function.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for GCP Function HTTP Test")
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.LocalErrorReadingBodyTest",
    "io.micronaut.http.server.tck.tests.FilterProxyTest",
    "io.micronaut.http.server.tck.tests.filter.options.OptionsFilterTest", // https://github.com/micronaut-projects/micronaut-core/pull/10126 removes a header which is not currently supported in gcp-function-http-test
})
class GcpFunctionHttpTestServerTestSuite {
}
