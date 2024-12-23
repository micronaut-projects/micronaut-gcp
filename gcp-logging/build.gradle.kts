plugins {
    id("io.micronaut.build.internal.gcp-module")
}

dependencies {
    compileOnly(projects.micronautGcpTracing)
    api(projects.micronautGcpCommon)
    implementation(libs.logback.json.classic) {
        exclude(group = "ch.qos.logback", module = "logback-classic")
    }
    implementation(mnLogging.logback.classic)
    implementation(mn.micronaut.json.core)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mnTestResources.testcontainers.core)
}
