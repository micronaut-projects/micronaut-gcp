plugins {
    id("io.micronaut.build.internal.gcp-module")
}

dependencies {
    api(projects.micronautGcpCommon)
    compileOnly(projects.micronautGcpTracing)
    implementation(libs.logback.json.classic) {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
    implementation(mn.micronaut.json.core)
    implementation(mnLogging.logback.classic)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mnTestResources.testcontainers.core)
}
