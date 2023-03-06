package io.micronaut.http.server.tck.gcp.function.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for for GCP Function HTTP")
@ExcludeClassNamePatterns(
    {
        "io.micronaut.http.server.tck.tests.CookiesTest",
        "io.micronaut.http.server.tck.tests.ConsumesTest",
        "io.micronaut.http.server.tck.tests.filter.HttpServerFilterTest",
        "io.micronaut.http.server.tck.tests.BodyTest",
        "io.micronaut.http.server.tck.tests.MiscTest",
        "io.micronaut.http.server.tck.tests.OctetTest",
        "io.micronaut.http.server.tck.tests.ErrorHandlerTest"
    }
)
class GcpFunctionHttpServerTestSuite {
}
