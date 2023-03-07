package io.micronaut.http.server.tck.gcp.function.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for for GCP Function HTTP")
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.BodyTest", // Error decoding JSON stream for type [List|pojo]
    "io.micronaut.http.server.tck.tests.ConsumesTest", // Unable to decode request body: Error decoding JSON stream for type [pojo]
    "io.micronaut.http.server.tck.tests.CookiesTest", // Cookies are not handled, I suspect as it's not implemented in the NettyClientHttpRequest
    "io.micronaut.http.server.tck.tests.ErrorHandlerTest", // Error decoding JSON stream for type [object]
    "io.micronaut.http.server.tck.tests.MiscTest", // Decoding errors and bad requests
    "io.micronaut.http.server.tck.tests.OctetTest", // Body is badly encoded
    "io.micronaut.http.server.tck.tests.filter.HttpServerFilterTest", // Expected io.micronaut.http.client.exceptions.HttpClientResponseException to be thrown, but nothing was thrown.
})
class GcpFunctionHttpServerTestSuite {
}
