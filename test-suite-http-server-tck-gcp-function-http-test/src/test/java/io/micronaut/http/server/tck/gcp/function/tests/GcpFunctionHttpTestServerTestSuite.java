package io.micronaut.http.server.tck.gcp.function.tests;

import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@ExcludeTags("max-request-size")
@SelectPackages("io.micronaut.http.server.tck.tests")
@SuiteDisplayName("HTTP Server TCK for GCP Function HTTP Test")
@ExcludeClassNamePatterns({
    "io.micronaut.http.server.tck.tests.BodyTest",
    "io.micronaut.http.server.tck.tests.FilterProxyTest",
    // The GCP Function HTTP adapter does not support the mutable request body/form semantics
    // exercised by this TCK. Exclude the untagged class by its fully qualified name.
    "io.micronaut.http.server.tck.tests.filter.FilterMutatedRequestTest",
    "io.micronaut.http.server.tck.tests.forms.FormBindingDeadlockTest",
    "io.micronaut.http.server.tck.tests.forms.UploadTest"
})
class GcpFunctionHttpTestServerTestSuite {
}
