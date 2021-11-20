package com.rxhttp.compiler

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.kotlinpoet.javapoet.JTypeName
import com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview
import com.squareup.kotlinpoet.javapoet.toJClassName
import com.squareup.kotlinpoet.javapoet.toJTypeVariableName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import rxhttp.wrapper.annotation.Param
import java.io.IOException
import java.util.*
import kotlin.NoSuchElementException

class ParamsVisitor(
    private val logger: KSPLogger,
    private val resolver: Resolver
) : KSVisitorVoid() {

    private val ksClassMap = LinkedHashMap<String, KSClassDeclaration>()

    @OptIn(KspExperimental::class)
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        try {
            classDeclaration.checkParamsValidClass()
            val annotations = classDeclaration.getAnnotationsByType(Param::class)
            val name = annotations.firstOrNull()?.methodName
            if (name.isNullOrBlank()) {
                val msg = "methodName() in @${Param::class.java.simpleName} for class " +
                        "'${classDeclaration.qualifiedName?.asString()}' is null or empty! that's not allowed"
                throw NoSuchElementException(msg)
            }
            ksClassMap[name] = classDeclaration
        } catch (e: NoSuchElementException) {
            logger.error(e, classDeclaration)
        }
    }

    @KspExperimental
    @KotlinPoetJavaPoetPreview
    @KotlinPoetKspPreview
    @Throws(IOException::class)
    fun getMethodList(codeGenerator: CodeGenerator): List<MethodSpec> {
        val methodList = ArrayList<MethodSpec>()
        var method: MethodSpec.Builder
        ksClassMap.forEach { (key, ksClass) ->
            val type = StringBuilder()
            val size = ksClass.typeParameters.size
            val rxHttpTypeNames = ksClass.typeParameters.mapIndexed { i, typeParameter ->
                typeParameter.toTypeVariableName().toJTypeVariableName().also {
                    type.append(if (i == 0) "<" else ",")
                    type.append(it.name)
                    if (i == size - 1) type.append(">")
                }
            }
            val param = ksClass.toClassName().toJClassName()
            val rxHttpName = "RxHttp${ksClass.simpleName.asString()}"
            val rxHttpParamName = ClassName.get(rxHttpPackage, rxHttpName)
            val methodReturnType = if (rxHttpTypeNames.isNotEmpty()) {
                ParameterizedTypeName.get(rxHttpParamName, *rxHttpTypeNames.toTypedArray())
            } else {
                rxHttpParamName
            }

            val classTypeParams = ksClass.typeParameters.toTypeParameterResolver()
            //遍历public构造方法
            ksClass.getConstructors().filter { it.isPublic() }.forEach {
                val parameterSpecs = arrayListOf<ParameterSpec>() //构造方法参数
                val methodBody = StringBuilder("return new \$T(new \$T(") //方法体
                val functionTypeParams = it.typeParameters.toTypeParameterResolver(classTypeParams)
                for ((index, ksValueParameter) in it.parameters.withIndex()) {
                    val parameterSpec = ksValueParameter.toJParameterSpec(functionTypeParams)
                    parameterSpecs.add(parameterSpec)
                    if (index == 0 && parameterSpec.type.toString() == "java.lang.String") {
                        methodBody.append("format(" + parameterSpecs[0].name + ", formatArgs)")
                        continue
                    } else if (index > 0) {
                        methodBody.append(", ")
                    }
                    methodBody.append(parameterSpec.name)
                }
                methodBody.append("))")
                val methodSpec = MethodSpec.methodBuilder(key)
                    .addModifiers(JModifier.PUBLIC, JModifier.STATIC)
                    .addParameters(parameterSpecs)
                    .addTypeVariables(rxHttpTypeNames)
                    .returns(methodReturnType)

                if (parameterSpecs.firstOrNull()?.type.toString() == "java.lang.String") {
                    methodSpec.addParameter(ArrayTypeName.of(JTypeName.OBJECT), "formatArgs")
                        .varargs()
                }
                methodSpec.addStatement(methodBody.toString(), rxHttpParamName, param)
                    .build()
                    .apply { methodList.add(this) }
            }
            val superclass = ksClass.superclass()
            var prefix = "((" + ksClass.simpleName.asString() + ")param)."
            val rxHttpParam = when (superclass?.getQualifiedName()) {
                "rxhttp.wrapper.param.BodyParam" -> ClassName.get(rxHttpPackage, "RxHttpBodyParam")
                "rxhttp.wrapper.param.FormParam" -> ClassName.get(rxHttpPackage, "RxHttpFormParam")
                "rxhttp.wrapper.param.JsonParam" -> ClassName.get(rxHttpPackage, "RxHttpJsonParam")
                "rxhttp.wrapper.param.JsonArrayParam" ->
                    ClassName.get(rxHttpPackage, "RxHttpJsonArrayParam")
                "rxhttp.wrapper.param.NoBodyParam" ->
                    ClassName.get(rxHttpPackage, "RxHttpNoBodyParam")
                else -> {
                    val typeName = superclass?.toJavaTypeName()
                    if ((typeName as? ParameterizedTypeName)?.rawType?.toString() == "rxhttp.wrapper.param.AbstractBodyParam") {
                        prefix = "param."
                        ClassName.get(rxHttpPackage, "RxHttpAbstractBodyParam").let {
                            ParameterizedTypeName.get(it, param, rxHttpParamName)
                        }
                    } else {
                        prefix = "param."
                        ParameterizedTypeName.get(RXHTTP, param, rxHttpParamName)
                    }
                }
            }
            val rxHttpPostCustomMethod = ArrayList<MethodSpec>()
            MethodSpec.constructorBuilder()
                .addModifiers(JModifier.PUBLIC)
                .addParameter(param, "param")
                .addStatement("super(param)")
                .build()
                .apply { rxHttpPostCustomMethod.add(this) }

            ksClass.getDeclaredFunctions().filter {
                it.isPublic() && !it.isConstructor() &&
                        Modifier.OVERRIDE !in it.modifiers &&
                        it.getAnnotationsByType(Override::class).firstOrNull() == null
            }.forEach { ksFunction ->
                val returnType = ksFunction.returnType?.toJavaTypeName().let {
                    if (it == param) rxHttpParamName else it
                }

                val parametersSize = ksFunction.parameters.size
                val functionTypeParams =
                    ksFunction.typeParameters.toTypeParameterResolver(classTypeParams)
                //方法体
                val methodBody = StringBuilder(ksFunction.simpleName.asString())
                    .append(if (parametersSize == 0) "()" else "")
                //方法参数
                val parameterSpecs = ksFunction.parameters.mapIndexed { i, ksValueParameter ->
                    ksValueParameter.toJParameterSpec(functionTypeParams).apply {
                        methodBody.append(if (i == 0) "(" else ",")
                        methodBody.append(this.name)
                        if (i == parametersSize - 1) methodBody.append(")")
                    }
                }
                val typeVariableNames = ksFunction.typeParameters.map {
                    it.toJavaTypeVariableName()
                }

                val throwTypeNames = resolver.getJvmCheckedException(ksFunction).map {
                    it.toClassName().toJClassName()
                }.toList()

                val modifiers = ksFunction.modifiers.mapNotNull { it.toJModifier() }

                method = MethodSpec.methodBuilder(ksFunction.simpleName.asString())
                    .addModifiers(modifiers)
                    .addTypeVariables(typeVariableNames)
                    .addExceptions(throwTypeNames)
                    .addParameters(parameterSpecs)
                    .varargs(ksFunction.parameters.lastOrNull()?.isVararg == true)
                when {
                    returnType === rxHttpParamName -> {
                        method.addStatement(prefix + methodBody, param)
                            .addStatement("return this")
                    }
                    returnType == JTypeName.VOID -> {
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
                .addModifiers(JModifier.PUBLIC)
                .addTypeVariables(rxHttpTypeNames)
                .superclass(rxHttpParam)
                .addMethods(rxHttpPostCustomMethod)
                .build()
            JavaFile.builder(rxHttpPackage, rxHttpPostEncryptFormParamSpec)
                .skipJavaLangImports(true)
                .build()
                .writeTo(codeGenerator)
        }
        return methodList
    }
}


@Throws(NoSuchElementException::class)
private fun KSClassDeclaration.checkParamsValidClass() {
    val paramSimpleName = Param::class.java.simpleName
    val elementQualifiedName = qualifiedName?.asString()
    if (!isPublic()) {
        throw NoSuchElementException("The class '$elementQualifiedName' must be public")
    }
    if (isAbstract()) {
        val msg =
            "The class '$elementQualifiedName' is abstract. You can't annotate abstract classes with @$paramSimpleName"
        throw NoSuchElementException(msg)
    }

    val className = "rxhttp.wrapper.param.Param"
    if (!instanceOf(className)) {
        val msg =
            "The class '$elementQualifiedName' annotated with @$paramSimpleName must inherit from $className"
        throw NoSuchElementException(msg)
    }
}