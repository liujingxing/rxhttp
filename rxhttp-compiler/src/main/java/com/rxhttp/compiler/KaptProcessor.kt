package com.rxhttp.compiler

import com.rxhttp.compiler.kapt.ClassHelper
import com.rxhttp.compiler.kapt.ConverterVisitor
import com.rxhttp.compiler.kapt.DefaultDomainVisitor
import com.rxhttp.compiler.kapt.DomainVisitor
import com.rxhttp.compiler.kapt.OkClientVisitor
import com.rxhttp.compiler.kapt.ParamsVisitor
import com.rxhttp.compiler.kapt.ParserVisitor
import com.rxhttp.compiler.kapt.RxHttpGenerator
import com.rxhttp.compiler.kapt.RxHttpWrapper
import rxhttp.wrapper.annotation.Converter
import rxhttp.wrapper.annotation.DefaultDomain
import rxhttp.wrapper.annotation.Domain
import rxhttp.wrapper.annotation.OkClient
import rxhttp.wrapper.annotation.Param
import rxhttp.wrapper.annotation.Parser
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic


/**
 * User: ljx
 * Date: 2019/3/21
 * Time: 20:36
 */
open class KaptProcessor : AbstractProcessor() {

    private lateinit var types: Types
    private lateinit var logger: Messager
    private lateinit var filer: Filer
    private lateinit var elementUtils: Elements
    private var debug = false
    private var processed = false
    private var incremental = true

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        types = processingEnvironment.typeUtils
        logger = processingEnvironment.messager
        filer = processingEnvironment.filer
        elementUtils = processingEnvironment.elementUtils
        val options = processingEnvironment.options
        rxHttpPackage = options[rxhttp_package] ?: defaultPackageName
        incremental = "false" != options[rxhttp_incremental]
        debug = "true" == options[rxhttp_debug]
        initRxJavaVersion(getRxJavaVersion(options))
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        return LinkedHashSet<String>().apply {
            add(Override::class.java.canonicalName)
            add(Param::class.java.canonicalName)
            add(Parser::class.java.canonicalName)
            add(Converter::class.java.canonicalName)
            add(Domain::class.java.canonicalName)
            add(DefaultDomain::class.java.canonicalName)
            add(OkClient::class.java.canonicalName)
        }
    }

    override fun getSupportedOptions(): MutableSet<String> {
        return mutableSetOf(
            rxhttp_rxjava, rxhttp_package,
            rxhttp_incremental, rxhttp_debug
        ).apply {
            if (incremental)
                add("org.gradle.annotation.processing.aggregating")
        }
    }

    open fun getRxJavaVersion(map: Map<String, String>) = map[rxhttp_rxjava]

    open fun isAndroidPlatform() = true

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (debug) {
            logger.printMessage(
                Diagnostic.Kind.WARNING,
                """
                    process isOver = ${roundEnv.processingOver()}     
                    processed = $processed                
                    rootElements.size = ${roundEnv.rootElements.size}
                    annotations = $annotations                     
                """.trimIndent()
            )
        }
        if (annotations.isEmpty() || processed) return true
        ClassHelper(isAndroidPlatform()).generatorStaticClass(filer)
        try {
            val rxHttpWrapper = RxHttpWrapper(logger)

            val paramsVisitor = ParamsVisitor(logger).apply {
                roundEnv.getElementsAnnotatedWith(Param::class.java).forEach {
                    val typeElement = it as TypeElement
                    add(typeElement, types)
                    rxHttpWrapper.add(typeElement)
                }
            }

            val parserVisitor = ParserVisitor(logger).apply {
                roundEnv.getElementsAnnotatedWith(Parser::class.java).forEach {
                    val typeElement = it as TypeElement
                    add(typeElement, types)
                }
            }

            val converterVisitor = ConverterVisitor(types, logger).apply {
                roundEnv.getElementsAnnotatedWith(Converter::class.java).forEach {
                    val variableElement = it as VariableElement
                    add(variableElement)
                    rxHttpWrapper.addConverter(variableElement)
                }
            }

            val okClientVisitor = OkClientVisitor(types, logger).apply {
                roundEnv.getElementsAnnotatedWith(OkClient::class.java).forEach {
                    val variableElement = it as VariableElement
                    add(variableElement)
                    rxHttpWrapper.addOkClient(variableElement)
                }
            }

            val domainVisitor = DomainVisitor(types, logger).apply {
                roundEnv.getElementsAnnotatedWith(Domain::class.java).forEach {
                    val variableElement = it as VariableElement
                    add(variableElement)
                    rxHttpWrapper.addDomain(variableElement)
                }
            }

            val defaultDomainVisitor = DefaultDomainVisitor(types, logger).apply {
                set(roundEnv.getElementsAnnotatedWith(DefaultDomain::class.java))
            }

            //Generate RxHttp.java
            RxHttpGenerator().apply {
                this.paramsVisitor = paramsVisitor
                this.parserVisitor = parserVisitor
                this.converterVisitor = converterVisitor
                this.domainVisitor = domainVisitor
                this.defaultDomainVisitor = defaultDomainVisitor
                this.okClientVisitor = okClientVisitor
            }.generateCode(filer)

            // 生成 RxHttp 封装类
            rxHttpWrapper.generateRxWrapper(filer)
            processed = true
        } catch (e: Throwable) {
            e.printStackTrace()
            logger.printMessage(Diagnostic.Kind.ERROR, e.message)
        }
        return true
    }
}