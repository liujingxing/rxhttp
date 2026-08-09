plugins {
    `java-library`
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
}

val jdkVersion = providers.gradleProperty("jdk_version").get()

java {
    sourceCompatibility = JavaVersion.toVersion(jdkVersion)
    targetCompatibility = JavaVersion.toVersion(jdkVersion)
}
