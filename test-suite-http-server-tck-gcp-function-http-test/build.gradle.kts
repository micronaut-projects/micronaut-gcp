plugins {
    id("io.micronaut.build.internal.http-server-tck-module")
}
dependencies {
    testImplementation(projects.micronautGcpFunctionHttpTest)
}
