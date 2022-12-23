plugins {
    id("io.micronaut.build.internal.gcp-module")
}

dependencies {
    annotationProcessor(mnSerde.micronaut.serde.processor)
    api(mnSerde.micronaut.serde.api)
    api(libs.managed.google.cloudevent.types)
    implementation(mnSerde.micronaut.serde.jackson)
}
