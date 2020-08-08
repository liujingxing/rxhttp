package com.rxhttp.compiler.maven

import com.rxhttp.compiler.AnnotationProcessor

/**
 * User: ljx
 * Date: 2020/8/8
 * Time: 10:30
 */
class AnnotationRxJava3Processor : AnnotationProcessor() {

    override fun getRxJavaVersion(map: Map<String, String>): String? {
        return "rxjava3"
    }

    override fun isAndroidPlatform() = false
}