package com.rxhttp.compiler.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFile
import com.rxhttp.compiler.common.getObservableClass
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage


/**
 * User: ljx
 * Date: 2020/3/31
 * Time: 23:36
 */
class ClassHelper {

    fun generatorStaticClass(codeGenerator: CodeGenerator) {
        if (isDependenceRxJava()) {
            getObservableClass().forEach { (t, u) ->
                generatorClass(codeGenerator, t, u)
            }
        }
    }

    private fun generatorClass(codeGenerator: CodeGenerator, className: String, content: String) {
        codeGenerator.createNewFile(
            Dependencies(false, *emptyList<KSFile>().toTypedArray()),
            rxHttpPackage,
            className,
            "java"
        ).use { 
            it.write(content.toByteArray())
        }
    }
}