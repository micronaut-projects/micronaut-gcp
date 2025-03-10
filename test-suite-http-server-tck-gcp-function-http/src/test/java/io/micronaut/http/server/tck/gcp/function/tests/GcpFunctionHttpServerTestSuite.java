package io.micronaut.http.server.tck.gcp.function.tests;

import org.junit.platform.suite.api.*;

@Suite
@SelectPackages({
    "io.micronaut.http.server.tck.tests",
    "io.micronaut.http.server.tck.gcp.function.tests"
})
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.FilterProxyTest",
    "io.micronaut.http.server.tck.tests.forms.FormsJacksonAnnotationsTest",
    "io.micronaut.http.server.tck.tests.ErrorHandlerFluxTest"
})
@SuiteDisplayName("HTTP Server TCK for for GCP Function HTTP")
class GcpFunctionHttpServerTestSuite {
}
