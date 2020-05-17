package com.rxhttp.compiler

import com.rxhttp.compiler.ClassHelper.generatorBaseRxHttp
import com.rxhttp.compiler.ClassHelper.generatorObservableDownload
import com.rxhttp.compiler.ClassHelper.generatorObservableErrorHandler
import com.rxhttp.compiler.ClassHelper.generatorObservableHttp
import com.rxhttp.compiler.ClassHelper.generatorObservableUpload
import com.rxhttp.compiler.exception.ProcessingException
import rxhttp.wrapper.annotation.*
import java.io.IOException
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

/**
 * User: ljx
 * Date: 2019/3/21
 * Time: 20:36
 */
class AnnotationProcessor : AbstractProcessor() {
    private lateinit var typeUtils: Types
    private lateinit var messager: Messager
    private lateinit var filer: Filer
    private lateinit var elementUtils: Elements
    private var processed = false
    private lateinit var okHttpVersion: String

    @Synchronized
    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        typeUtils = processingEnvironment.typeUtils
        messager = processingEnvironment.messager
        filer = processingEnvironment.filer
        elementUtils = processingEnvironment.elementUtils
        val map = processingEnvironment.options
        okHttpVersion = map["rxhttp_okhttp"] ?: "4.6.0"
        initRxJavaVersion(map["rxhttp_rxjava"])
    }

    override fun getSupportedAnnotationTypes(): Set<String> {
        val annotations: MutableSet<String> = LinkedHashSet()
        annotations.add(Param::class.java.canonicalName)
        annotations.add(Parser::class.java.canonicalName)
        annotations.add(Converter::class.java.canonicalName)
        annotations.add(Domain::class.java.canonicalName)
        annotations.add(DefaultDomain::class.java.canonicalName)
        annotations.add(Override::class.java.canonicalName)
        return annotations
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        messager.printMessage(Diagnostic.Kind.WARNING, "process start annotations$annotations this=$this")
        if (annotations.isEmpty() || processed) return true
        generatorBaseRxHttp(filer)
        if (isDependenceRxJava()) {  //是否依赖了RxJava
            generatorObservableErrorHandler(filer)
            generatorObservableHttp(filer)
            generatorObservableUpload(filer)
            generatorObservableDownload(filer)
        }
        try {
            val rxHttpGenerator = RxHttpGenerator()
            val paramsAnnotatedClass = ParamsAnnotatedClass()
            roundEnv.getElementsAnnotatedWith(Param::class.java).forEach {
                val typeElement = it as TypeElement
                checkParamsValidClass(typeElement)
                paramsAnnotatedClass.add(typeElement)
            }

            val parserAnnotatedClass = ParserAnnotatedClass()
            roundEnv.getElementsAnnotatedWith(Parser::class.java).forEach {
                val typeElement = it as TypeElement
                checkParserValidClass(typeElement)
                parserAnnotatedClass.add(typeElement)
            }
            val converterAnnotatedClass = ConverterAnnotatedClass()
            roundEnv.getElementsAnnotatedWith(Converter::class.java).forEach {
                val variableElement = it as VariableElement
                checkConverterValidClass(variableElement)
                converterAnnotatedClass.add(variableElement)
            }
            val domainAnnotatedClass = DomainAnnotatedClass()
            roundEnv.getElementsAnnotatedWith(Domain::class.java).forEach {
                val variableElement = it as VariableElement
                checkVariableValidClass(variableElement)
                domainAnnotatedClass.add(variableElement)
            }
            val elementSet = roundEnv.getElementsAnnotatedWith(DefaultDomain::class.java)
            if (elementSet.size > 1)
                throw ProcessingException(elementSet.iterator().next(), "@DefaultDomain annotations can only be used once")
            else if (elementSet.iterator().hasNext()) {
                val variableElement = elementSet.iterator().next() as VariableElement
                checkVariableValidClass(variableElement)
                rxHttpGenerator.setAnnotatedClass(variableElement)
            }
            rxHttpGenerator.setAnnotatedClass(paramsAnnotatedClass)
            rxHttpGenerator.setAnnotatedClass(parserAnnotatedClass)
            rxHttpGenerator.setAnnotatedClass(converterAnnotatedClass)
            rxHttpGenerator.setAnnotatedClass(domainAnnotatedClass)

            // Generate code
            rxHttpGenerator.generateCode(elementUtils, filer, okHttpVersion)
            processed = true
        } catch (e: ProcessingException) {
            error(e.element, e.message)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    @Throws(ProcessingException::class)
    private fun checkParamsValidClass(element: TypeElement) {
        if (!element.modifiers.contains(Modifier.PUBLIC)) {
            throw ProcessingException(element,
                "The class %s is not public",
                Param::class.java.simpleName)
        }
        if (element.modifiers.contains(Modifier.ABSTRACT)) {
            throw ProcessingException(element,
                "The class %s is abstract. You can't annotate abstract classes with @%",
                element.simpleName.toString(), Param::class.java.simpleName)
        }
        var currentClass = element
        while (true) {
            val interfaces = currentClass.interfaces
            for (typeMirror in interfaces) {
                if (typeMirror.toString() != "rxhttp.wrapper.param.Param<P>") continue
                return
            }
            val superClassType = currentClass.superclass
            if (superClassType.kind == TypeKind.NONE) {
                throw ProcessingException(element,
                    "The class %s annotated with @%s must inherit from %s",
                    element.qualifiedName.toString(), Param::class.java.simpleName,
                    "rxhttp.wrapper.param.Param")
            }
            currentClass = typeUtils.asElement(superClassType) as TypeElement
        }
    }

    @Throws(ProcessingException::class)
    private fun checkParserValidClass(element: TypeElement) {
        if (!element.modifiers.contains(Modifier.PUBLIC)) {
            throw ProcessingException(element,
                "The class %s is not public",
                Parser::class.java.simpleName)
        }
        if (element.modifiers.contains(Modifier.ABSTRACT)) {
            throw ProcessingException(element,
                "The class %s is abstract. You can't annotate abstract classes with @%",
                element.simpleName.toString(), Parser::class.java.simpleName)
        }
        var currentClass = element
        while (true) {
            val interfaces = currentClass.interfaces
            //遍历实现的接口有没有Parser接口
            for (typeMirror in interfaces) {
                if (typeMirror.toString().contains("rxhttp.wrapper.parse.Parser")) {
                    return
                }
            }
            //未遍历到Parser，则找到父类继续，一直循环下去，直到最顶层的父类
            val superClassType = currentClass.superclass
            if (superClassType.kind == TypeKind.NONE) {
                throw ProcessingException(element,
                    "The class %s annotated with @%s must inherit from %s",
                    element.qualifiedName.toString(), Parser::class.java.simpleName,
                    "rxhttp.wrapper.parse.Parser<T>")
            }
            //TypeMirror转TypeElement
            currentClass = typeUtils.asElement(superClassType) as TypeElement
        }
    }

    @Throws(ProcessingException::class)
    private fun checkConverterValidClass(element: VariableElement) {
        if (!element.modifiers.contains(Modifier.PUBLIC)) {
            throw ProcessingException(element,
                "The variable %s is not public",
                element.simpleName)
        }
        if (!element.modifiers.contains(Modifier.STATIC)) {
            throw ProcessingException(element,
                "The variable %s is not static",
                element.simpleName.toString())
        }
        var classType = element.asType()
        if ("rxhttp.wrapper.callback.IConverter" != classType.toString()) {
            while (true) {
                //TypeMirror转TypeElement
                val currentClass = typeUtils.asElement(classType) as TypeElement
                //遍历实现的接口有没有IConverter接口
                for (mirror in currentClass.interfaces) {
                    if (mirror.toString() == "rxhttp.wrapper.callback.IConverter") {
                        return
                    }
                }
                //未遍历到IConverter，则找到父类继续，一直循环下去，直到最顶层的父类
                classType = currentClass.superclass
                if (classType.kind == TypeKind.NONE) {
                    throw ProcessingException(element,
                        "The variable %s is not a IConverter",
                        element.simpleName.toString())
                }
            }
        }
    }

    @Throws(ProcessingException::class)
    private fun checkVariableValidClass(element: VariableElement) {
        if (!element.modifiers.contains(Modifier.PUBLIC)) {
            throw ProcessingException(element,
                "The variable %s is not public",
                element.simpleName)
        }
        if (!element.modifiers.contains(Modifier.STATIC)) {
            throw ProcessingException(element,
                "The variable %s is not static",
                element.simpleName.toString())
        }
    }

    private fun error(e: Element, msg: String?, vararg args: Any) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg ?: "", *args), e)
    }
}