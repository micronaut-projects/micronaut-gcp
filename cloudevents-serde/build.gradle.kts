plugins {
    id("io.micronaut.build.internal.module")
}
dependencies {
    api(project(":cloudevents"))
    annotationProcessor(mn.micronaut.serde.processor)
    implementation(mn.micronaut.serde.jackson)
    testImplementation(libs.cloudevents.core)
    implementation(mn.micronaut.jackson.databind)
    testImplementation(libs.managed.google.cloudevent.types)
}
micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
