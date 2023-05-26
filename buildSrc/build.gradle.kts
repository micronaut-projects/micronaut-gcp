plugins {
    id("groovy-gradle-plugin")
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven {
    }
}

dependencies {
    implementation(libs.gradle.micronaut)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.gradle.allopen)
}
