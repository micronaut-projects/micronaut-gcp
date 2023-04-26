plugins {
    id("io.micronaut.build.internal.http-server-tck-module")
}

dependencies {
    testImplementation(projects.micronautGcpFunctionHttp)
    testImplementation(libs.managed.functions.framework.api)
}
