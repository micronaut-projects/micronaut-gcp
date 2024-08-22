package io.micronaut.http.server.tck.gcp.function.tests;

import org.junit.platform.suite.api.*;

@Suite
@SelectPackages({
    "io.micronaut.http.server.tck.tests",
    "io.micronaut.http.server.tck.gcp.function.tests"
})
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.FilterProxyTest",
})
@SuiteDisplayName("HTTP Server TCK for for GCP Function HTTP")
class GcpFunctionHttpServerTestSuite {
}
