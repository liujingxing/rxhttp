package com.rxhttp.compiler

import com.rxhttp.compiler.exception.ProcessingException
import com.squareup.javapoet.*
import rxhttp.wrapper.annotation.Converter
import rxhttp.wrapper.annotation.Domain
import rxhttp.wrapper.annotation.OkClient
import rxhttp.wrapper.annotation.Param
import java.util.*
import javax.annotation.processing.Filer
import javax.lang.model.element.*
import kotlin.collections.ArrayList

/**
 * User: ljx
 * Date: 2020/5/30
 * Time: 19:03
 */
class RxHttpWrapper {

    private val classMap = LinkedHashMap<String, MutableList<Annotation>>()

    private val mElementMap = LinkedHashMap<String, TypeElement>()

    fun add(typeElement: TypeElement) {
        val annotation = typeElement.getAnnotation(Param::class.java)
        val name: String = annotation.methodName
        require(name.isNotEmpty()) {
            String.format("methodName() in @%s for class %s is null or empty! that's not allowed",
                Param::class.java.simpleName, typeElement.qualifiedName.toString())
        }
        mElementMap[name] = typeElement
    }

    fun addAnnotation(okClient: OkClient, element: Element) {
        var annotationList = classMap[okClient.className]
        if (annotationList == null) {
            annotationList = ArrayList()
            classMap[okClient.className] = annotationList
        }
        annotationList.forEach {
            if (it is OkClient)
                throw ProcessingException(element,
                    "@OkClient annotation className cannot be the same")
        }
        annotationList.add(okClient)
    }

    fun addAnnotation(converter: Converter, element: Element) {
        if (converter.className.isEmpty()) return
        var annotationList = classMap[converter.className]
        if (annotationList == null) {
            annotationList = ArrayList()
            classMap[converter.className] = annotationList
        }
        annotationList.forEach {
            if (it is Converter)
                throw ProcessingException(element,
                    "@Converter annotation className cannot be the same")
        }
        annotationList.add(converter)
    }

    fun addAnnotation(domain: Domain, element: Element) {
        var annotationList = classMap[domain.className]
        if (annotationList == null) {
            annotationList = ArrayList()
            classMap[domain.className] = annotationList
        }
        annotationList.forEach {
            if (it is Domain)
                throw ProcessingException(element,
                    "@Domain annotation className cannot be the same")
        }
        annotationList.add(domain)
    }

    fun generateRxWrapper(filer: Filer) {
        val requestFunList = generateRequestFunList()

        for ((className, annotationList) in classMap) {
            val funBody = CodeBlock.builder()
            annotationList.forEach {
                when (it) {
                    is OkClient -> funBody.addStatement("rxHttp.set${it.name}()")
                    is Converter -> funBody.addStatement("rxHttp.set${it.name}()")
                    is Domain -> funBody.addStatement("rxHttp.setDomainTo${it.name}IfAbsent()")
                }
            }
            val methodList = ArrayList<MethodSpec>()
            methodList.add(
                MethodSpec.methodBuilder("wrapper")
                    .addJavadoc("本类所有方法都会调用本方法\n")
                    .addTypeVariable(r)
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .addParameter(r, "rxHttp")
                    .addCode(funBody.build())
                    .returns(Void.TYPE)
                    .build())
            methodList.addAll(requestFunList)

            val rxHttpBuilder = TypeSpec.classBuilder("Rx${className}Http")
                .addJavadoc("""
                    本类由@Converter、@Domain、@OkClient注解中的className字段生成  类命名方式: Rx + {className字段值} + Http
                    Github
                    https://github.com/liujingxing/RxHttp
                    https://github.com/liujingxing/RxLife
                    https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ
                    https://github.com/liujingxing/okhttp-RxHttp/wiki/更新日志
                """.trimIndent())
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methodList)

            JavaFile.builder(rxHttpPackage, rxHttpBuilder.build())
                .build().writeTo(filer)
        }
    }


    private fun generateRequestFunList(): ArrayList<MethodSpec> {
        val methodList = ArrayList<MethodSpec>() //方法集合
        val methodMap = LinkedHashMap<String, String>()
        methodMap["get"] = "RxHttpNoBodyParam"
        methodMap["head"] = "RxHttpNoBodyParam"
        methodMap["postForm"] = "RxHttpFormParam"
        methodMap["putForm"] = "RxHttpFormParam"
        methodMap["patchForm"] = "RxHttpFormParam"
        methodMap["deleteForm"] = "RxHttpFormParam"
        methodMap["postJson"] = "RxHttpJsonParam"
        methodMap["putJson"] = "RxHttpJsonParam"
        methodMap["patchJson"] = "RxHttpJsonParam"
        methodMap["deleteJson"] = "RxHttpJsonParam"
        methodMap["postJsonArray"] = "RxHttpJsonArrayParam"
        methodMap["putJsonArray"] = "RxHttpJsonArrayParam"
        methodMap["patchJsonArray"] = "RxHttpJsonArrayParam"
        methodMap["deleteJsonArray"] = "RxHttpJsonArrayParam"
        for ((key, value) in methodMap) {
            val returnType = ClassName.get(rxHttpPackage, value);
            methodList.add(
                MethodSpec.methodBuilder(key)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(String::class.java, "url")
                    .addParameter(ArrayTypeName.of(Any::class.java), "formatArgs")
                    .varargs()
                    .addStatement("\$T rxHttp = RxHttp.${key}(url, formatArgs)", returnType)
                    .addStatement("wrapper(rxHttp)")
                    .addStatement("return rxHttp")
                    .returns(returnType)
                    .build())
        }

        for ((key, typeElement) in mElementMap) {
            val rxHttpTypeNames = ArrayList<TypeVariableName>()
            typeElement.typeParameters.forEach {
                rxHttpTypeNames.add(TypeVariableName.get(it))
            }

            val rxHttpParamName = ClassName.get(rxHttpPackage, "RxHttp${typeElement.simpleName}")
            val methodReturnType = if (rxHttpTypeNames.size > 0) {
                ParameterizedTypeName.get(rxHttpParamName, *rxHttpTypeNames.toTypedArray())
            } else {
                rxHttpParamName
            }

            //遍历public构造方法
            getConstructorFun(typeElement).forEach {
                val parameterSpecs = java.util.ArrayList<ParameterSpec>() //构造方法参数
                val methodBody = StringBuilder("\$T rxHttp = RxHttp.$key(") //方法体
                for ((index, element) in it.parameters.withIndex()) {
                    val parameterSpec = ParameterSpec.get(element)
                    parameterSpecs.add(parameterSpec)
                    if (index > 0) {
                        methodBody.append(", ")
                    }
                    methodBody.append(parameterSpec.name)
                }
                if (parameterSpecs.size > 0 && parameterSpecs[0].type.toString().contains("String")) {
                    methodBody.append(", formatArgs")
                }
                methodBody.append(")")

                val methodSpec = MethodSpec.methodBuilder(key)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameters(parameterSpecs)
                    .addTypeVariables(rxHttpTypeNames)
                    .returns(methodReturnType)

                if (parameterSpecs.size > 0 && parameterSpecs[0].type.toString().contains("String")) {
                    methodSpec.addParameter(ArrayTypeName.of(Any::class.java), "formatArgs")
                        .varargs()
                }
                methodSpec.addStatement(methodBody.toString(), rxHttpParamName)
                    .addStatement("wrapper(rxHttp)")
                    .addStatement("return rxHttp")
                methodList.add(methodSpec.build())
            }
        }
        return methodList
    }

    //获取构造方法
    private fun getConstructorFun(typeElement: TypeElement): MutableList<ExecutableElement> {
        val funList = java.util.ArrayList<ExecutableElement>()
        typeElement.enclosedElements.forEach {
            if (it is ExecutableElement
                && it.kind == ElementKind.CONSTRUCTOR
                && it.getModifiers().contains(Modifier.PUBLIC)
            ) {
                funList.add(it)
            }
        }
        return funList
    }
}