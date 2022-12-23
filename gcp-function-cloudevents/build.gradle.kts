plugins {
    id("io.micronaut.build.internal.gcp-module")
}
dependencies {
    api(libs.cloudevents.api)
    api(libs.managed.functions.framework.api)
    api(projects.gcpFunction)
    api(projects.gcpSerdeCloudevents)
    implementation(mnSerde.micronaut.serde.jackson)
    testImplementation(libs.cloudevents.core)
}
