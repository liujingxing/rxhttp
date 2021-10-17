package com.rxhttp.compiler

import com.rxhttp.compiler.ClassHelper.generatorStaticClass
import com.rxhttp.compiler.exception.ProcessingException
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import rxhttp.wrapper.annotation.*
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic


lateinit var rxHttpPackage: String  //RxHttp相关类的包名

/**
 * User: ljx
 * Date: 2019/3/21
 * Time: 20:36
 */
//@SupportedOptions(value = ["rxhttp_rxjava", "rxhttp_package", "rxhttp_incremental", "rxhttp_debug"])
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.DYNAMIC)
open class AnnotationProcessor : AbstractProcessor() {
    companion object {
        const val rxhttp_rxjava = "rxhttp_rxjava"
        const val rxhttp_package = "rxhttp_package"
        const val rxhttp_incremental = "rxhttp_incremental"
        const val rxhttp_debug = "rxhttp_debug"
    }

    private lateinit var types: Types
    private lateinit var messager: Messager
    private lateinit var filer: Filer
    private lateinit var elementUtils: Elements
    private var debug = false
    private var processed = false
    private var incremental = true

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        types = processingEnvironment.typeUtils
        messager = processingEnvironment.messager
        filer = processingEnvironment.filer
        elementUtils = processingEnvironment.elementUtils
        val map = processingEnvironment.options
        rxHttpPackage = map[rxhttp_package] ?: "rxhttp.wrapper.param"
        incremental = "false" != map[rxhttp_incremental]
        debug = "true" == map[rxhttp_debug]
        initRxJavaVersion(getRxJavaVersion(map))
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
            rxhttp_rxjava,
            rxhttp_package,
            rxhttp_incremental,
            rxhttp_debug
        ).apply {
            if (incremental) {
                add("org.gradle.annotation.processing.aggregating")
            }
        }
    }

    open fun getRxJavaVersion(map: Map<String, String>) = map[rxhttp_rxjava]

    open fun isAndroidPlatform() = true

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (debug) {
            messager.printMessage(
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
        generatorStaticClass(filer, isAndroidPlatform())
        try {
            val rxHttpWrapper = RxHttpWrapper()

            val paramsVisitor = ParamsVisitor().apply {
                roundEnv.getElementsAnnotatedWith(Param::class.java).forEach {
                    val typeElement = it as TypeElement
                    add(typeElement, types)
                    rxHttpWrapper.add(typeElement)
                }
            }

            val parserVisitor = ParserVisitor().apply {
                roundEnv.getElementsAnnotatedWith(Parser::class.java).forEach {
                    val typeElement = it as TypeElement
                    add(typeElement, types)
                }
            }

            val converterVisitor = ConverterVisitor().apply {
                roundEnv.getElementsAnnotatedWith(Converter::class.java).forEach {
                    val variableElement = it as VariableElement
                    add(variableElement, types)
                    rxHttpWrapper.addConverter(variableElement)
                }
            }

            val okClientVisitor = OkClientVisitor().apply {
                roundEnv.getElementsAnnotatedWith(OkClient::class.java).forEach {
                    val variableElement = it as VariableElement
                    add(variableElement)
                    rxHttpWrapper.addOkClient(variableElement)
                }
            }

            val domainVisitor = DomainVisitor().apply {
                roundEnv.getElementsAnnotatedWith(Domain::class.java).forEach {
                    val variableElement = it as VariableElement
                    add(variableElement)
                    rxHttpWrapper.addDomain(variableElement)
                }
            }

            val defaultDomainVisitor = DefaultDomainVisitor().apply {
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
        } catch (e: ProcessingException) {
            error(e.element, e.message)
        } catch (e: Throwable) {
            e.printStackTrace()
            messager.printMessage(Diagnostic.Kind.ERROR, e.message)
        }
        return true
    }

    private fun error(e: Element, msg: String?, vararg args: Any) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg ?: "", *args), e)
    }
}