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

    private val classMap = LinkedHashMap<String, Wrapper>()

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

    fun addOkClient(variableElement: VariableElement) {
        val okClient = variableElement.getAnnotation(OkClient::class.java)
        if (okClient.className.isEmpty()) return
        var wrapper = classMap[okClient.className]
        if (wrapper == null) {
            wrapper = Wrapper()
            classMap[okClient.className] = wrapper
        }
        if (wrapper.okClientName != null) {
            throw ProcessingException(variableElement,
                "@OkClient annotation className cannot be the same")
        }
        wrapper.okClientName = okClient.name
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
            throw ProcessingException(variableElement,
                "@Converter annotation className cannot be the same")
        }
        wrapper.converterName = converter.name
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
            throw ProcessingException(variableElement,
                "@Domain annotation className cannot be the same")
        }
        wrapper.domainName = if (domain.name.isEmpty()) variableElement.simpleName.toString() else domain.name
    }

    fun generateRxWrapper(filer: Filer) {
        val requestFunList = generateRequestFunList()

        //生成多个RxHttp的包装类
        for ((className, wrapper) in classMap) {
            val funBody = CodeBlock.builder()
            if (wrapper.converterName != null) {
                funBody.addStatement("rxHttp.set${wrapper.converterName}()")
            }
            if (wrapper.okClientName != null) {
                funBody.addStatement("rxHttp.set${wrapper.okClientName}()")
            }
            if (wrapper.domainName != null) {
                funBody.addStatement("rxHttp.setDomainTo${wrapper.domainName}IfAbsent()")
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

    class Wrapper {
        var domainName: String? = null
        var converterName: String? = null
        var okClientName: String? = null
    }
}