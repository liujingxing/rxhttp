plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.legacy.kapt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.httpsender"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.rxhttp"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = true
        dataBinding = true
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

kotlin {
    jvmToolchain(17)
}

ksp {
    arg("rxhttp_rxjava", libs.versions.rxjava.get())
//    arg("rxhttp_package", "rxhttp")
}

kapt {
    includeCompileClasspath = false
    arguments {
        arg("rxhttp_rxjava", libs.versions.rxjava.get()) // 可传入 rxjava2、rxjava3 或具体版本号，如 3.1.1
//        arg("rxhttp_package", "rxhttp") // 设置 RxHttp 相关类的包名，多 module 依赖时需要配置不同包名
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.github.magicindicator)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.service)

    implementation(projects.rxhttp)
    ksp(projects.rxhttpCompiler)
//    kapt(projects.rxhttpCompiler)

    implementation(libs.okhttp)
    testImplementation(libs.mockwebserver)

//    implementation(libs.rxhttp)
//    ksp(libs.rxhttp.compiler)
//    kapt(libs.rxhttp.compiler)

    // 管理 RxJava 及生命周期，Activity/Fragment 销毁时自动关闭未完成的请求
    implementation(libs.github.rxlife.rxjava3)
    implementation(libs.rxandroid)
    implementation(libs.rxjava)

//    implementation(libs.rxhttp.converter.serialization)
//    implementation(libs.rxhttp.converter.fastjson)
//    implementation(libs.rxhttp.converter.jackson)
//    implementation(libs.rxhttp.converter.moshi)
//    implementation(libs.rxhttp.converter.protobuf)
//    implementation(libs.rxhttp.converter.simplexml)

    implementation(projects.rxhttpConverter.converterSerialization)
    implementation(projects.rxhttpConverter.converterFastjson)
    implementation(projects.rxhttpConverter.converterSimplexml)
    implementation(projects.rxhttpConverter.converterProtobuf)
    implementation(projects.rxhttpConverter.converterMoshi)
    implementation(projects.rxhttpConverter.converterJackson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.utilcodex)

    testImplementation(libs.kotlin.compile.testing.ksp)
    testImplementation(libs.kotlinpoet)
    testImplementation(libs.kotlinpoet.ksp)
    testImplementation(projects.rxhttpCompiler)
}
