import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.ksp) apply false
}

subprojects {
    pluginManager.withPlugin("java") {
        pluginManager.apply("maven-publish")
        val targetProject = this@subprojects

        extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("release") {
                    from(targetProject.components["java"])
                }
            }
        }
    }
}
