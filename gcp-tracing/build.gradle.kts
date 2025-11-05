plugins {
    id("io.micronaut.build.internal.gcp-module")
}

dependencies {
    api(mnTracing.micronaut.tracing.brave.http)
    api(projects.micronautGcpCommon)
    implementation(libs.brave.opentracing)
    implementation(libs.brave.propagation.stackdriver)
    implementation(libs.google.auth.library.credentials)
    implementation(libs.grpc.auth)
    implementation(libs.grpc.netty.shaded)
    implementation(libs.zipkin.sender.stackdriver)
    implementation(platform(mnGrpc.boms.grpc))
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.inject.java)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(testFixtures(projects.micronautGcpCommon))
}
