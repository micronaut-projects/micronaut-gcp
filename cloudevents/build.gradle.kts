plugins {
    id("io.micronaut.build.internal.module")
}
dependencies {
    api(libs.cloudevents.api)
}
micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
