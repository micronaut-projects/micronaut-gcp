plugins {
    id("io.micronaut.build.internal.http-server-tck-module")
}

dependencies {
    testImplementation(libs.managed.functions.framework.api)
    testImplementation(projects.micronautGcpFunctionHttp)
}
