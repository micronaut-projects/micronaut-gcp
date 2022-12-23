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

//TODO Don't pin version once this module uses MN 3.8.0
configurations.all {
    resolutionStrategy {
        force("io.micronaut.serde:micronaut-serde-jackson:1.5.0")
    }
}
