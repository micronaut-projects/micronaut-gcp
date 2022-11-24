plugins {
    id("io.micronaut.build.internal.module")
}
dependencies {
    api(libs.managed.google.cloudevent.types)
    api(libs.managed.functions.framework.api)
    api(project(":gcp-function"))
    annotationProcessor(mn.micronaut.serde.processor)
    implementation(mn.micronaut.serde.jackson)
    testImplementation(libs.cloudevents.core)
}

micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
