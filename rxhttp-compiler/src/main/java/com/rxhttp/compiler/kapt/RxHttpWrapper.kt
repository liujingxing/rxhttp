package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.rxHttpPackage
import com.rxhttp.compiler.rxhttpClass
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import rxhttp.wrapper.annotation.Converter
import rxhttp.wrapper.annotation.Domain
import rxhttp.wrapper.annotation.OkClient
import rxhttp.wrapper.annotation.Param
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement

/**
 * User: ljx
 * Date: 2020/5/30
 * Time: 19:03
 */
class RxHttpWrapper(private val logger: Messager) {

    private val classMap = LinkedHashMap<String, Wrapper>()

    private val elementMap = LinkedHashMap<String, TypeElement>()

    fun add(typeElement: TypeElement) {
        val annotation = typeElement.getAnnotation(Param::class.java)
        val name: String = annotation.methodName
        elementMap[name] = typeElement
    }

    fun addOkClient(variableElement: VariableElement) {
        val okClient = variableElement.getAnnotation(OkClient::class.java)
        if (okClient.className.isEmpty()) return
        var wrapper = classMap[okClient.className]
        if (wrapper == null) {
            wrapper = Wrapper()
            classMap[okClient.className] = wrapper
        }
        if (wrapper.okClientName != null) {
            val msg = "@OkClient annotation className cannot be the same"
            logger.error(msg, variableElement)
        }
        var name = okClient.name
        if (name.isBlank()) {
            name = variableElement.simpleName.toString().firstLetterUpperCase()
        }
        wrapper.okClientName = name
    }


    fun addConverter(variableElement: VariableElement) {
        val converter = variableElement.getAnnotation(Converter::class.java)
        if (converter.className.isEmpty()) return
        var wrapper = classMap[converter.className]
        if (wrapper == null) {
            wrapper = Wrapper()
            classMap[converter.className] = wrapper
        }
        if (wrapper.converterName != null) {
            val msg = "@Converter annotation className cannot be the same"
            logger.error(msg, variableElement)
        }
        var name = converter.name
        if (name.isBlank()) {
            name = variableElement.simpleName.toString().firstLetterUpperCase()
        }
        wrapper.converterName = name
    }

    fun addDomain(variableElement: VariableElement) {
        val domain = variableElement.getAnnotation(Domain::class.java)
        if (domain.className.isEmpty()) return
        var wrapper = classMap[domain.className]
        if (wrapper == null) {
            wrapper = Wrapper()
            classMap[domain.className] = wrapper
        }
        if (wrapper.domainName != null) {
            val msg = "@Domain annotation className cannot be the same"
            logger.error(msg, variableElement)
        }
        var name = domain.name
        if (name.isBlank()) {
            name = variableElement.simpleName.toString().firstLetterUpperCase()
        }
        wrapper.domainName = name
    }

    fun generateRxWrapper(filer: Filer) {
        val requestFunList = generateRequestFunList()
        val typeVariableR = TypeVariableName.get("R", rxhttpClass)     //泛型R
        //生成多个RxHttp的包装类
        classMap.forEach { (className, wrapper) ->
            val funBody = CodeBlock.builder()
            wrapper.converterName?.let {
                funBody.addStatement("rxHttp.set$it()")
            }
            wrapper.okClientName?.let {
                funBody.addStatement("rxHttp.set$it()")
            }
            wrapper.domainName?.let {
                funBody.addStatement("rxHttp.setDomainTo${it}IfAbsent()")
            }
            val methodList = ArrayList<MethodSpec>()
            MethodSpec.methodBuilder("wrapper")
                .addJavadoc("本类所有方法都会调用本方法")
                .addTypeVariable(typeVariableR)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(typeVariableR, "rxHttp")
                .addCode(funBody.build())
                .addStatement("return rxHttp")
                .returns(typeVariableR)
                .build()
                .apply { methodList.add(this) }
            methodList.addAll(requestFunList)

            val rxHttpBuilder = TypeSpec.classBuilder("Rx${className}Http")
                .addJavadoc(
                    """
                    本类由@Converter、@Domain、@OkClient注解中的className字段生成  类命名方式: Rx + {className字段值} + Http
                    Github
                    https://github.com/liujingxing/rxhttp
                    https://github.com/liujingxing/rxlife
                    https://github.com/liujingxing/rxhttp/wiki/FAQ
                    https://github.com/liujingxing/rxhttp/wiki/更新日志
                """.trimIndent()
                )
                .addModifiers(Modifier.PUBLIC)
                .addMethods(methodList)

            JavaFile.builder(rxHttpPackage, rxHttpBuilder.build())
                .skipJavaLangImports(true)
                .build().writeTo(filer)
        }
    }


    private fun generateRequestFunList(): ArrayList<MethodSpec> {
        val arrayObject = ArrayTypeName.of(TypeName.OBJECT)
        val methodList = ArrayList<MethodSpec>() //方法集合
        val methodMap = LinkedHashMap<String, String>()
        methodMap["get"] = "RxHttpNoBodyParam"
        methodMap["head"] = "RxHttpNoBodyParam"
        methodMap["postBody"] = "RxHttpBodyParam"
        methodMap["putBody"] = "RxHttpBodyParam"
        methodMap["patchBody"] = "RxHttpBodyParam"
        methodMap["deleteBody"] = "RxHttpBodyParam"
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
        methodMap.forEach { (key, value) ->
            val returnType = rxhttpClass.peerClass(value)
            MethodSpec.methodBuilder(key)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(STRING, "url")
                .addParameter(arrayObject, "formatArgs")
                .varargs()
                .addStatement("return wrapper(RxHttp.${key}(url, formatArgs))")
                .returns(returnType)
                .build().apply { methodList.add(this) }
        }

        elementMap.forEach { (key, typeElement) ->
            val rxHttpTypeNames = typeElement.typeParameters.map {
                TypeVariableName.get(it)
            }

            val rxHttpParamName = rxhttpClass.peerClass("RxHttp${typeElement.simpleName}")
            val methodReturnType = if (rxHttpTypeNames.isNotEmpty()) {
                rxHttpParamName.parameterizedBy(*rxHttpTypeNames.toTypedArray())
            } else {
                rxHttpParamName
            }

            //遍历public构造方法
            getConstructorFun(typeElement).forEach { element ->
                //构造方法参数
                val parameterSpecs = element.parameters.mapTo(ArrayList()) { ParameterSpec.get(it) }
                val firstParamIsStringType = parameterSpecs.firstOrNull()?.type == STRING
                if (firstParamIsStringType) {
                    parameterSpecs.add(ParameterSpec.builder(arrayObject, "formatArgs").build())
                }
                val prefix = "return wrapper(RxHttp.$key("
                val postfix = "))"
                val methodBody = parameterSpecs.toParamNames(prefix, postfix)
                MethodSpec.methodBuilder(key)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addTypeVariables(rxHttpTypeNames)
                    .addParameters(parameterSpecs)
                    .varargs(firstParamIsStringType)
                    .addStatement(methodBody)
                    .returns(methodReturnType)
                    .build()
                    .apply { methodList.add(this) }
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

    class Wrapper {
        var domainName: String? = null
        var converterName: String? = null
        var okClientName: String? = null
    }
}