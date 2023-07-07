plugins {
    id("io.micronaut.build.internal.gcp-module")
}

dependencies {
    compileOnly(projects.micronautGcpTracing)
    api(projects.micronautGcpCommon)
    implementation(libs.logback.json.classic)
    implementation(mn.micronaut.json.core)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(libs.testcontainers.spock)
}
