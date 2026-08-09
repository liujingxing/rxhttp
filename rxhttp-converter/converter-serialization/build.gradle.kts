import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    compileOnly(projects.rxhttp)
    compileOnly(libs.okhttp)
    api(libs.kotlinx.serialization.json)
}

val jdkVersion = providers.gradleProperty("jdk_version").get()

java {
    sourceCompatibility = JavaVersion.toVersion(jdkVersion)
    targetCompatibility = JavaVersion.toVersion(jdkVersion)
}

kotlin {
//    jvmToolchain(jdkVersion.toInt())
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
    }
}
