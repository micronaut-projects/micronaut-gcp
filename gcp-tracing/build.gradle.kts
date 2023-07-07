plugins {
    id("io.micronaut.build.internal.gcp-module")
}

dependencies {
    api(projects.micronautGcpCommon)
    api(mnTracing.micronaut.tracing.brave.http)
    implementation(libs.google.auth.library.credentials)
    implementation(libs.zipkin.sender.stackdriver)
    implementation(libs.brave.propagation.stackdriver)
    implementation(libs.grpc.auth)
    implementation(libs.grpc.netty.shaded)
    implementation(libs.brave.opentracing)
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.inject.java)
}
