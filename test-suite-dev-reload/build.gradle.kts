plugins {
    id("io.micronaut.build.internal.java-base")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    testImplementation(platform(mn.micronaut.core.bom))
    testImplementation(projects.micronautGcpPubsub)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(mn.micronaut.dev.tck)
    // the reload harness compiles the application under test with the processors on the test classpath
    testImplementation(mn.micronaut.inject.java)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers)
    testImplementation(mnTest.junit.jupiter.api)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testRuntimeOnly(mnTest.junit.platform.launcher)
    testRuntimeOnly(mnLogging.logback.classic)
}
