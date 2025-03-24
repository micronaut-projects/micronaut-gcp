package io.micronaut.http.server.tck.gcp.function.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for GCP Function HTTP Test")
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.FilterProxyTest"
})
class GcpFunctionHttpTestServerTestSuite {
}
