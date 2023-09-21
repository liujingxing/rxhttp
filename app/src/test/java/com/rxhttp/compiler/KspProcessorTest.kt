package com.rxhttp.compiler

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.rxhttp.compiler.ksp.getParserTypeParam
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import rxhttp.wrapper.annotation.Parser
import java.io.File

/**
 * User: ljx
 * Date: 2023/8/27
 * Time: 17:04
 */
@RunWith(JUnit4::class)
class KspProcessorTest {
    @Test
    fun testKspProvider() {
        val compilation = KotlinCompilation().apply {
            sources = sourceFiles()
            symbolProcessorProviders = listOf(KspProvider())
        }
        val result = compilation.compile()
        Assert.assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
    }


    @Test
    fun testKspProvider1() {
        val compilation = KotlinCompilation().apply {
            sources = sourceFiles()
            symbolProcessorProviders = listOf(processorProviderOf {
                TestSymbolProcessor(it.codeGenerator)
            })
        }
        val result = compilation.compile()
        Assert.assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
    }

    class TestSymbolProcessor(codeGenerator: CodeGenerator) :
        AbstractTestSymbolProcessor(codeGenerator) {
        override fun process(resolver: Resolver): List<KSAnnotated> {
            resolver.getSymbolsWithAnnotation(Parser::class.java.name).forEach {
                if (it is KSClassDeclaration) {
                    val parserTypeParam = it.getParserTypeParam()
                    println("parserTypeParam=$parserTypeParam")
                }
            }
            return emptyList()
        }
    }

    private fun sourceFiles(): List<SourceFile> {
        val kotlinPath = "src/main/java/com/example/httpsender/parser"
        val entityPath = "src/main/java/com/example/httpsender/entity"
        val annotationPrefix = "../rxhttp-annotation/src/main/java/rxhttp/wrapper/annotation"
        val parserPrefix = "../rxhttp/src/main/java/rxhttp/wrapper/parse"
        val testPrefix = "src/test/java/com/rxhttp/compiler"
        return mutableListOf(
//            "$kotlinPath/ResponseParser.kt",
            "$entityPath/Response.java",
            "$parserPrefix/TypeParser.java",
            "$parserPrefix/Parser.java",
            "$parserPrefix/TypeParser.java",
            "$annotationPrefix/Converter.java",
            "$annotationPrefix/DefaultDomain.java",
            "$annotationPrefix/Domain.java",
            "$annotationPrefix/OkClient.java",
            "$annotationPrefix/Param.java",
            "$annotationPrefix/Parser.java",
            "$testPrefix/TestParser1.kt",
            "$testPrefix/TestParser2.kt",
            "src/main/java/com/example/httpsender/entity/User.java",
            "../rxhttp/src/main/java/rxhttp/wrapper/callback/Consumer.java"
        ).map { SourceFile.fromPath(File(it)) }
    }

}