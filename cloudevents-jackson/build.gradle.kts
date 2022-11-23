plugins {
    id("io.micronaut.build.internal.module")
}
dependencies {
    api(project(":cloudevents"))
    implementation(mn.micronaut.jackson.databind)
    testImplementation(libs.cloudevents.core)
    implementation(mn.micronaut.jackson.databind)
    testImplementation(libs.managed.google.cloudevent.types)
}
micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
