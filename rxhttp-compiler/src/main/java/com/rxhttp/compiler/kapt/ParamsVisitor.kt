package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.rxhttpClassName
import com.rxhttp.compiler.rxHttpPackage
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import rxhttp.wrapper.annotation.Param
import java.io.IOException
import java.util.*
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Types

class ParamsVisitor(private val logger: Messager) {

    private val elementMap = LinkedHashMap<String, TypeElement>()

    fun add(element: TypeElement, types: Types) {
        try {
            element.checkParamsValidClass(types)
            val annotation = element.getAnnotation(Param::class.java)
            val name = annotation.methodName
            if (name.isBlank()) {
                val msg = "methodName() in @${Param::class.java.simpleName} for class " +
                        "'${element.qualifiedName}' is null or empty! that's not allowed"
                throw NoSuchElementException(msg)
            }
            elementMap[name] = element
        } catch (e: NoSuchElementException) {
            logger.error(e.message, element)
        }
    }

    @Throws(IOException::class)
    fun getMethodList(filer: Filer): List<MethodSpec> {
        val methodList = ArrayList<MethodSpec>()
        var method: MethodSpec.Builder
        elementMap.forEach { (key, typeElement) ->
            val type = StringBuilder()
            val size = typeElement.typeParameters.size
            val rxHttpTypeNames = typeElement.typeParameters.mapIndexed { i, parameterElement ->
                TypeVariableName.get(parameterElement).also {
                    type.append(if (i == 0) "<" else ",")
                    type.append(it.name)
                    if (i == size - 1) type.append(">")
                }
            }
            val param = ClassName.get(typeElement)
            val rxHttpName = "RxHttp${typeElement.simpleName}"
            val rxHttpParamName = ClassName.get(rxHttpPackage, rxHttpName)
            val methodReturnType = if (rxHttpTypeNames.isNotEmpty()) {
                ParameterizedTypeName.get(rxHttpParamName, *rxHttpTypeNames.toTypedArray())
            } else {
                rxHttpParamName
            }
            //遍历public构造方法
            typeElement.getPublicConstructors().forEach {
                val parameterSpecs = ArrayList<ParameterSpec>() //构造方法参数
                val methodBody = StringBuilder("return new \$T(new \$T(") //方法体
                for ((index, element) in it.parameters.withIndex()) {
                    val parameterSpec = ParameterSpec.get(element)
                    parameterSpecs.add(parameterSpec)
                    if (index == 0 && parameterSpec.type.toString() == "java.lang.String") {
                        methodBody.append("format(" + parameterSpec.name + ", formatArgs)")
                        continue
                    } else if (index > 0) {
                        methodBody.append(", ")
                    }
                    methodBody.append(parameterSpec.name)
                }
                methodBody.append("))")
                val methodSpec = MethodSpec.methodBuilder(key)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameters(parameterSpecs)
                    .addTypeVariables(rxHttpTypeNames)
                    .returns(methodReturnType)

                if (parameterSpecs.firstOrNull()?.type.toString() == "java.lang.String") {
                    methodSpec.addParameter(ArrayTypeName.of(TypeName.OBJECT), "formatArgs")
                        .varargs()
                }
                methodSpec.addStatement(methodBody.toString(), rxHttpParamName, param)
                    .build()
                    .apply { methodList.add(this) }
            }
            val superclass = typeElement.superclass
            var prefix = "((" + typeElement.simpleName + ")param)."
            val rxHttpParam = when (superclass.toString()) {
                "rxhttp.wrapper.param.BodyParam" -> ClassName.get(rxHttpPackage, "RxHttpBodyParam")
                "rxhttp.wrapper.param.FormParam" -> ClassName.get(rxHttpPackage, "RxHttpFormParam")
                "rxhttp.wrapper.param.JsonParam" -> ClassName.get(rxHttpPackage, "RxHttpJsonParam")
                "rxhttp.wrapper.param.JsonArrayParam" ->
                    ClassName.get(rxHttpPackage, "RxHttpJsonArrayParam")
                "rxhttp.wrapper.param.NoBodyParam" ->
                    ClassName.get(rxHttpPackage, "RxHttpNoBodyParam")
                else -> {
                    val typeName = TypeName.get(superclass)
                    if ((typeName as? ParameterizedTypeName)?.rawType?.toString() == "rxhttp.wrapper.param.AbstractBodyParam") {
                        prefix = "param."
                        ClassName.get(rxHttpPackage, "RxHttpAbstractBodyParam").let {
                            ParameterizedTypeName.get(it, param, rxHttpParamName)
                        }
                    } else {
                        prefix = "param."
                        ParameterizedTypeName.get(rxhttpClassName, param, rxHttpParamName)
                    }
                }
            }
            val rxHttpPostCustomMethod = ArrayList<MethodSpec>()
            MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(param, "param")
                .addStatement("super(param)")
                .build()
                .apply { rxHttpPostCustomMethod.add(this) }
            for (enclosedElement in typeElement.enclosedElements) {
                if (enclosedElement !is ExecutableElement
                    || enclosedElement.getKind() != ElementKind.METHOD //过滤非方法，
                    || !enclosedElement.getModifiers().contains(Modifier.PUBLIC) //过滤非public修饰符
                    || enclosedElement.getAnnotation(Override::class.java) != null //过滤重写的方法
                ) continue
                val returnType = TypeName.get(enclosedElement.returnType).let {
                    if (it == param) rxHttpParamName else it
                }

                val parametersSize = enclosedElement.parameters.size
                //方法体
                val methodBody = StringBuilder(enclosedElement.getSimpleName().toString())
                    .append(if (parametersSize == 0) "()" else "")
                //方法参数
                val parameterSpecs = enclosedElement.parameters.mapIndexed { i, variableElement ->
                    ParameterSpec.get(variableElement).apply {
                        methodBody.append(if (i == 0) "(" else ",")
                        methodBody.append(this.name)
                        if (i == parametersSize - 1) methodBody.append(")")
                    }
                }
                //方法声明的泛型
                val typeVariableNames = enclosedElement.typeParameters.map {
                    TypeVariableName.get(it)
                }
                //方法要抛出的异常
                val throwTypeNames = enclosedElement.thrownTypes.map { TypeName.get(it) }
                method = MethodSpec.methodBuilder(enclosedElement.getSimpleName().toString())
                    .addModifiers(enclosedElement.getModifiers())
                    .addTypeVariables(typeVariableNames)
                    .addExceptions(throwTypeNames)
                    .addParameters(parameterSpecs)
                    .varargs(enclosedElement.isVarArgs)
                when {
                    returnType === rxHttpParamName -> {
                        method.addStatement(prefix + methodBody, param)
                            .addStatement("return this")
                    }
                    returnType == TypeName.VOID -> {
                        method.addStatement(prefix + methodBody)
                    }
                    else -> {
                        method.addStatement("return $prefix$methodBody", param)
                    }
                }
                method.returns(returnType)
                rxHttpPostCustomMethod.add(method.build())
            }
            val rxHttpPostEncryptFormParamSpec = TypeSpec.classBuilder(rxHttpName)
                .addJavadoc(
                    """
                    Github
                    https://github.com/liujingxing/rxhttp
                    https://github.com/liujingxing/rxlife
                """.trimIndent()
                )
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariables(rxHttpTypeNames)
                .superclass(rxHttpParam)
                .addMethods(rxHttpPostCustomMethod)
                .build()
            JavaFile.builder(rxHttpPackage, rxHttpPostEncryptFormParamSpec)
                .skipJavaLangImports(true)
                .build().writeTo(filer)
        }
        return methodList
    }
}

@Throws(NoSuchElementException::class)
private fun TypeElement.checkParamsValidClass(types: Types) {
    val paramSimpleName = Param::class.java.simpleName
    val elementQualifiedName = qualifiedName.toString()
    if (!modifiers.contains(Modifier.PUBLIC)) {
        throw NoSuchElementException("The class '$elementQualifiedName' must be public")
    }
    if (modifiers.contains(Modifier.ABSTRACT)) {
        val msg =
            "The class '$elementQualifiedName' is abstract. You can't annotate abstract classes with @$paramSimpleName"
        throw NoSuchElementException(msg)
    }
    val className = "rxhttp.wrapper.param.Param"
    if (!instanceOf(className, types)) {
        val msg =
            "The class '$elementQualifiedName' annotated with @$paramSimpleName must inherit from $className"
        throw NoSuchElementException(msg)
    }
}