plugins {
    `java-library`
}

dependencies {
    implementation(fileTree("libs") { include("*.jar") })
    compileOnly(projects.rxhttp)
    compileOnly(libs.okhttp)
    api(libs.simple.xml)
}

val jdkVersion = providers.gradleProperty("jdk_version").get()

java {
    sourceCompatibility = JavaVersion.toVersion(jdkVersion)
    targetCompatibility = JavaVersion.toVersion(jdkVersion)
}
