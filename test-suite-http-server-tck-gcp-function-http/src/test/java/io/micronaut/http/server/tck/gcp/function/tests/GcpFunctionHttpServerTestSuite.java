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
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.FilterProxyTest",
        "io.micronaut.http.server.tck.tests.NoBodyResponseTest"
})
@SuiteDisplayName("HTTP Server TCK for for GCP Function HTTP")
class GcpFunctionHttpServerTestSuite {
}
