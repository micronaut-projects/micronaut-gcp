plugins {
    id("io.micronaut.build.internal.module")
}
dependencies {
    api(libs.managed.google.cloudevent.types)
    api(libs.managed.functions.framework.api)
    api(project(":gcp-function"))

    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut.serde:micronaut-serde-support")

    testImplementation(libs.cloudevents.core)

    //TODO this will be in another repo
    api(project(":cloudevents-serde"))
}

micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
