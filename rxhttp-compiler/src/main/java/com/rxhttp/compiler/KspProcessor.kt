package com.rxhttp.compiler

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.rxhttp.compiler.ksp.ClassHelper
import com.rxhttp.compiler.ksp.ConverterVisitor
import com.rxhttp.compiler.ksp.DefaultDomainVisitor
import com.rxhttp.compiler.ksp.DomainVisitor
import com.rxhttp.compiler.ksp.OkClientVisitor
import com.rxhttp.compiler.ksp.ParamsVisitor
import com.rxhttp.compiler.ksp.ParserVisitor
import com.rxhttp.compiler.ksp.RxHttpGenerator
import com.rxhttp.compiler.ksp.RxHttpWrapper
import com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import rxhttp.wrapper.annotation.Converter
import rxhttp.wrapper.annotation.DefaultDomain
import rxhttp.wrapper.annotation.Domain
import rxhttp.wrapper.annotation.OkClient
import rxhttp.wrapper.annotation.Param
import rxhttp.wrapper.annotation.Parser

/**
 * User: ljx
 * Date: 2021/10/8
 * Time: 16:31
 */
class KspProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {

    private var processed: Boolean = false

    @OptIn(
        KspExperimental::class,
        KotlinPoetJavaPoetPreview::class,
        KotlinPoetKspPreview::class
    )
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val logger = env.logger
        val options = env.options
        val codeGenerator = env.codeGenerator

        val debug = "true" == options[rxhttp_debug]
        if (debug) {
            logger.warn(
                "LJX process getAllFiles.size=${
                    resolver.getAllFiles().toList().size
                } newFiles.size=${resolver.getNewFiles().toList().size}"
            )
        }
        if (processed) return emptyList()

        rxHttpPackage = options[rxhttp_package] ?: defaultPackageName
        initRxJavaVersion(options[rxhttp_rxjava])

        val rxHttpWrapper = RxHttpWrapper(logger)

        val domainVisitor = DomainVisitor(resolver, logger)
        resolver.getSymbolsWithAnnotation(Domain::class.java.name).forEach {
            if (it is KSPropertyDeclaration) {
                it.accept(domainVisitor, Unit)
                rxHttpWrapper.addDomain(it)
            }
        }

        val defaultDomainVisitor = DefaultDomainVisitor(logger)
        resolver.getSymbolsWithAnnotation(DefaultDomain::class.java.name).forEach {
            if (it is KSPropertyDeclaration) {
                it.accept(defaultDomainVisitor, Unit)
            }
        }

        val okClientVisitor = OkClientVisitor(resolver, logger)
        resolver.getSymbolsWithAnnotation(OkClient::class.java.name).forEach {
            if (it is KSPropertyDeclaration) {
                it.accept(okClientVisitor, Unit)
                rxHttpWrapper.addOkClient(it)
            }
        }

        val converterVisitor = ConverterVisitor(resolver, logger)
        resolver.getSymbolsWithAnnotation(Converter::class.java.name).forEach {
            if (it is KSPropertyDeclaration) {
                it.accept(converterVisitor, Unit)
                rxHttpWrapper.addConverter(it)
            }
        }

        val parserVisitor = ParserVisitor(logger)
        resolver.getSymbolsWithAnnotation(Parser::class.java.name).forEach {
            if (it is KSClassDeclaration) {
                it.accept(parserVisitor, Unit)
            }
        }

        val paramsVisitor = ParamsVisitor(logger, resolver)
        resolver.getSymbolsWithAnnotation(Param::class.java.name).forEach {
            if (it is KSClassDeclaration) {
                it.accept(paramsVisitor, Unit)
                rxHttpWrapper.add(it)
            }
        }
        rxHttpWrapper.generateRxWrapper(codeGenerator)
        ClassHelper.generatorStaticClass(codeGenerator, true)
        RxHttpGenerator(logger).apply {
            this.paramsVisitor = paramsVisitor
            this.parserVisitor = parserVisitor
            this.domainVisitor = domainVisitor
            this.okClientVisitor = okClientVisitor
            this.converterVisitor = converterVisitor
            this.defaultDomainVisitor = defaultDomainVisitor
            this.generateCode(codeGenerator)
        }
        processed = true
        return emptyList()
    }

    override fun finish() {}

    override fun onError() {}
}

class KspProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = KspProcessor(environment)
}