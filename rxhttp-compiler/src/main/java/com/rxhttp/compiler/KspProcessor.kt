package com.rxhttp.compiler

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.rxhttp.compiler.ksp.*
import rxhttp.wrapper.annotation.*

/**
 * User: ljx
 * Date: 2021/10/8
 * Time: 16:31
 */
class KspProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {

    private var processed: Boolean = false

    @KspExperimental
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val logger = env.logger
        val options = env.options
        val codeGenerator = env.codeGenerator

        val debug = "true" == options[rxhttp_debug]
        if (debug) {
            logger.warn(
                "LJX process getAllFiles.size=${resolver.getAllFiles().toList().size} " +
                        "newFiles.size=${resolver.getNewFiles().toList().size}"
            )
        }
        if (processed || resolver.getAllFiles().toList().isEmpty()) return emptyList()

        rxHttpPackage = options[rxhttp_package] ?: defaultPackageName
        initRxJavaVersion(options[rxhttp_rxjava])

        val ksFileSet = HashSet<KSFile>()
        val rxHttpWrapper = RxHttpWrapper(logger)

        val domainVisitor = DomainVisitor(resolver, logger)
        resolver.getSymbolsWithAnnotation(Domain::class.java.name).forEach {
            if (it is KSPropertyDeclaration) {
                ksFileSet.add(it.containingFile!!)
                it.accept(domainVisitor, Unit)
                rxHttpWrapper.addDomain(it)
            }
        }

        val defaultDomainVisitor = DefaultDomainVisitor(resolver, logger)
        resolver.getSymbolsWithAnnotation(DefaultDomain::class.java.name).forEach {
            if (it is KSPropertyDeclaration) {
                ksFileSet.add(it.containingFile!!)
                it.accept(defaultDomainVisitor, Unit)
            }
        }

        val okClientVisitor = OkClientVisitor(resolver, logger)
        resolver.getSymbolsWithAnnotation(OkClient::class.java.name).forEach {
            if (it is KSPropertyDeclaration) {
                ksFileSet.add(it.containingFile!!)
                it.accept(okClientVisitor, Unit)
                rxHttpWrapper.addOkClient(it)
            }
        }

        val converterVisitor = ConverterVisitor(resolver, logger)
        resolver.getSymbolsWithAnnotation(Converter::class.java.name).forEach {
            if (it is KSPropertyDeclaration) {
                ksFileSet.add(it.containingFile!!)
                it.accept(converterVisitor, Unit)
                rxHttpWrapper.addConverter(it)
            }
        }

        val parserVisitor = ParserVisitor(logger)
        resolver.getSymbolsWithAnnotation(Parser::class.java.name).forEach {
            if (it is KSClassDeclaration) {
                ksFileSet.add(it.containingFile!!)
                it.accept(parserVisitor, Unit)
            }
        }

        val paramsVisitor = ParamsVisitor(logger, resolver)
        resolver.getSymbolsWithAnnotation(Param::class.java.name).forEach {
            if (it is KSClassDeclaration) {
                ksFileSet.add(it.containingFile!!)
                it.accept(paramsVisitor, Unit)
                rxHttpWrapper.add(it)
            }
        }
        rxHttpWrapper.generateRxWrapper(codeGenerator)
        ClassHelper(true, ksFileSet).generatorStaticClass(codeGenerator)
        KClassHelper(true, ksFileSet).generatorStaticClass(codeGenerator)
        RxHttpGenerator(logger, ksFileSet).apply {
            this.paramsVisitor = paramsVisitor
            this.parserVisitor = parserVisitor
            this.domainVisitor = domainVisitor
            this.okClientVisitor = okClientVisitor
            this.converterVisitor = converterVisitor
            this.defaultDomainVisitor = defaultDomainVisitor
        }.generateCode(codeGenerator)
        processed = true
        return emptyList()
    }

    override fun finish() {}

    override fun onError() {}
}

class KspProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = KspProcessor(environment)
}