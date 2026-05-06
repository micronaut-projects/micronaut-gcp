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
    "io.micronaut.http.server.tck.tests.BodyTest",
    "io.micronaut.http.server.tck.tests.cors.CorsStaticResourceTest",
    "io.micronaut.http.server.tck.tests.FilterProxyTest",
    "io.micronaut.http.server.tck.tests.forms.FormBindingDeadlockTest",
    "io.micronaut.http.server.tck.tests.forms.FormsJacksonAnnotationsTest",
    "io.micronaut.http.server.tck.tests.ErrorHandlerFluxTest",
    "io.micronaut.http.server.tck.tests.filter.CacheControlTest",
    "io.micronaut.http.server.tck.tests.forms.UploadTest"
})
@SuiteDisplayName("HTTP Server TCK for for GCP Function HTTP")
class GcpFunctionHttpServerTestSuite {
}
