package com.rxhttp.compiler.kapt.maven

import com.rxhttp.compiler.KaptProcessor

/**
 * User: ljx
 * Date: 2020/8/8
 * Time: 10:30
 */
class AnnotationRxJava3Processor : KaptProcessor() {

    override fun getRxJavaVersion(map: Map<String, String>) = "3.1.1"

    override fun isAndroidPlatform() = false
}