plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    kotlin("plugin.serialization") version "1.9.22"


}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}
dependencies {


    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    testImplementation("junit:junit:4.13.2")
}
