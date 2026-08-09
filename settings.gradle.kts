pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "RxHttp-Project"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")
include(":rxhttp")
include(":rxhttp-compiler")
include(":rxhttp-annotation")
include(":rxhttp-converter:converter-fastjson")
include(":rxhttp-converter:converter-jackson")
include(":rxhttp-converter:converter-moshi")
include(":rxhttp-converter:converter-protobuf")
include(":rxhttp-converter:converter-serialization")
include(":rxhttp-converter:converter-simplexml")
