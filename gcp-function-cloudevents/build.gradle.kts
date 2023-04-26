plugins {
    id("io.micronaut.build.internal.gcp-module")
}
dependencies {
    api(libs.cloudevents.api)
    api(libs.managed.functions.framework.api)
    api(projects.micronautGcpFunction)
    api(projects.micronautGcpSerdeCloudevents)
    implementation(mnSerde.micronaut.serde.jackson)
    testImplementation(libs.cloudevents.core)
}
