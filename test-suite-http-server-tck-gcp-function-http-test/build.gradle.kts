plugins {
    id("io.micronaut.build.internal.http-server-tck-module")
}
repositories {
    mavenLocal {
        mavenContent {
            snapshotsOnly()
        }
    }
}
dependencies {
    testImplementation(projects.micronautGcpFunctionHttpTest)
}
