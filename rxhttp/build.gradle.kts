import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    alias(libs.plugins.kotlin.jvm)
}

val copyJavaTemplates = tasks.register<Copy>("copyJavaTemplates") {
    from("src/main/java-templates")
    into(layout.buildDirectory.dir("generated/sources/java-templates/java/main"))
    expand("projectVersion" to libs.versions.rxhttp.get())
    filteringCharset = "UTF-8"
}

sourceSets.named("main") {
    java.srcDir(layout.buildDirectory.dir("generated/sources/java-templates/java/main"))
}

tasks.named("compileKotlin") {
    dependsOn(copyJavaTemplates)
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    compileOnly(libs.android)
    compileOnly(libs.okhttp)
//    api(libs.rxhttp.annotation)
    api(projects.rxhttpAnnotation)
    api(libs.gson)
    api(libs.kotlinx.coroutines.android)
    testImplementation(libs.junit)
}

val jdkVersion = providers.gradleProperty("jdk_version").get()

java {
    sourceCompatibility = JavaVersion.toVersion(jdkVersion)
    targetCompatibility = JavaVersion.toVersion(jdkVersion)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
    }
}
