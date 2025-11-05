plugins {
    id("io.micronaut.build.internal.gcp-module")
}

dependencies {
    annotationProcessor(mnSerde.micronaut.serde.processor)
    api(libs.managed.google.cloudevent.types)
    api(mnSerde.micronaut.serde.api)
    implementation(mnSerde.micronaut.serde.jackson)
}
