plugins {
    id("io.micronaut.build.internal.gcp-module")
}

dependencies {
    annotationProcessor(mn.micronaut.serde.processor)

    api(mn.micronaut.serde.api)
    api(libs.managed.google.cloudevent.types)

    testImplementation(mn.micronaut.serde.jackson)
}
