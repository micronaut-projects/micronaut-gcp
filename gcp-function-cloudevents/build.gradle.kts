plugins {
    id("io.micronaut.build.internal.module")
}
dependencies {
    api(libs.cloudevents.api)
    api(libs.managed.functions.framework.api)
    api(project(":gcp-function"))
    api(project(":gcp-serde-cloudevents"))
    implementation(mn.micronaut.serde.jackson)
    testImplementation(libs.cloudevents.core)
}

micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
