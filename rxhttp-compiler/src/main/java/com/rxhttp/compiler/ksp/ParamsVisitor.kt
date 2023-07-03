package com.rxhttp.compiler.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.isAbstract
import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Modifier
import com.rxhttp.compiler.common.joinToStringIndexed
import com.rxhttp.compiler.common.toParamNames
import com.rxhttp.compiler.rxHttpPackage
import com.rxhttp.compiler.rxhttpKClass
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.jvm.jvmStatic
import com.squareup.kotlinpoet.jvm.throws
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import com.squareup.kotlinpoet.ksp.writeTo
import rxhttp.wrapper.annotation.Param
import java.io.IOException

class ParamsVisitor(
    private val logger: KSPLogger,
    private val resolver: Resolver
) : KSVisitorVoid() {

    private val ksClassMap = LinkedHashMap<String, KSClassDeclaration>()

    @KspExperimental
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
    @Throws(IOException::class)
    fun getFunList(codeGenerator: CodeGenerator): List<FunSpec> {
        val funList = ArrayList<FunSpec>()
        var funSpecBuilder: FunSpec.Builder
        ksClassMap.forEach { (key, ksClass) ->
            val rxHttpTypeNames = ksClass.typeParameters.map { it.toTypeVariableName() }
            val param = ksClass.toClassName()
            val rxHttpName = "RxHttp${ksClass.simpleName.asString()}"
            val rxHttpParamName = rxhttpKClass.peerClass(rxHttpName)
            val methodReturnType = if (rxHttpTypeNames.isNotEmpty()) {
                rxHttpParamName.parameterizedBy(*rxHttpTypeNames.toTypedArray())
            } else {
                rxHttpParamName
            }

            val classTypeParams = ksClass.typeParameters.toTypeParameterResolver()
            //遍历public构造方法
            ksClass.getPublicConstructors().forEach { function ->
                val functionTypeParams = function.typeParameters
                    .toTypeParameterResolver(classTypeParams)
                //构造方法参数
                val parameterSpecs = function.parameters
                    .mapTo(ArrayList()) { it.toKParameterSpec(functionTypeParams) }
                val prefix = "return %T(%T("
                val postfix = "))"
                val methodBody = parameterSpecs
                    .joinToStringIndexed(", ", prefix, postfix) { index, it ->
                        if (index == 0 && it.type == STRING) {
                            "format(${it.name}, *formatArgs)"
                        } else if (it.isVararg()) "*${it.name}" else it.name
                    }
                if (parameterSpecs.firstOrNull()?.type == STRING) {
                    parameterSpecs.add(newParameterSpec("formatArgs", ANY, true, KModifier.VARARG))
                }
                FunSpec.builder(key)
                    .jvmStatic()
                    .addParameters(parameterSpecs)
                    .addTypeVariables(rxHttpTypeNames)
                    .addStatement(methodBody, rxHttpParamName, param)
                    .returns(methodReturnType)
                    .build()
                    .apply { funList.add(this) }
            }
            val superclass = ksClass.superclass()
            var prefix = "(param as ${ksClass.simpleName.asString()})."
            val rxHttpParam = when (superclass?.getQualifiedName()) {
                "rxhttp.wrapper.param.BodyParam" -> rxhttpKClass.peerClass("RxHttpBodyParam")
                "rxhttp.wrapper.param.FormParam" -> rxhttpKClass.peerClass("RxHttpFormParam")
                "rxhttp.wrapper.param.JsonParam" -> rxhttpKClass.peerClass("RxHttpJsonParam")
                "rxhttp.wrapper.param.JsonArrayParam" -> rxhttpKClass.peerClass("RxHttpJsonArrayParam")
                "rxhttp.wrapper.param.NoBodyParam" -> rxhttpKClass.peerClass("RxHttpNoBodyParam")
                else -> {
                    val typeName = superclass?.toTypeName()
                    if ((typeName as? ParameterizedTypeName)?.rawType?.toString() == "rxhttp.wrapper.param.AbstractBodyParam") {
                        prefix = "param."
                        rxhttpKClass.peerClass("RxHttpAbstractBodyParam")
                            .parameterizedBy(param, rxHttpParamName)
                    } else {
                        prefix = "param."
                        rxhttpKClass.parameterizedBy(param, rxHttpParamName)
                    }
                }
            }
            val rxHttpPostCustomFun = ArrayList<FunSpec>()
            FunSpec.constructorBuilder()
                .addParameter("param", param)
                .callSuperConstructor("param")
                .build()
                .apply { rxHttpPostCustomFun.add(this) }

            ksClass.getDeclaredFunctions().filter {
                it.isPublic() && !it.isConstructor() &&
                        Modifier.OVERRIDE !in it.modifiers &&
                        it.getAnnotationsByType(Override::class).firstOrNull() == null
            }.forEach { ksFunction ->
                val returnType = ksFunction.returnType?.toTypeName().let {
                    if (it == param) rxHttpParamName else it
                }

                val functionTypeParams = ksFunction.typeParameters
                    .toTypeParameterResolver(classTypeParams)
                //方法参数
                val parameterSpecs = ksFunction.parameters.map { ksValueParameter ->
                    ksValueParameter.toKParameterSpec(functionTypeParams)
                }
                //方法参数名字
                val paramNames = parameterSpecs.toParamNames()
                //方法体
                val methodBody = "${ksFunction.simpleName.asString()}($paramNames)"
                val typeVariableNames = ksFunction.typeParameters.map { it.toTypeVariableName() }

                val throwTypeNames = resolver.getJvmCheckedException(ksFunction)
                    .map { it.toClassName() }.toList()

                val modifiers = ksFunction.modifiers.mapNotNull { it.toKModifier() }

                funSpecBuilder = FunSpec.builder(ksFunction.simpleName.asString())
                    .addModifiers(modifiers)
                    .addTypeVariables(typeVariableNames)
                    .addParameters(parameterSpecs)

                if (throwTypeNames.isNotEmpty()) {
                    funSpecBuilder.throws(throwTypeNames)
                }
                when {
                    returnType === rxHttpParamName -> {
                        funSpecBuilder.addCode("""
                            return apply {
                              $prefix$methodBody 
                            }
                        """.trimIndent(), param)
                    }
                    returnType == UNIT -> {
                        funSpecBuilder.addStatement(prefix + methodBody)
                    }
                    else -> {
                        funSpecBuilder.addStatement("return $prefix$methodBody", param)
                    }
                }
                returnType?.apply { funSpecBuilder.returns(this) }

                rxHttpPostCustomFun.add(funSpecBuilder.build())
            }
            val rxHttpPostEncryptFormParamSpec = TypeSpec.classBuilder(rxHttpName)
                .addKdoc(
                    """
                    Github
                    https://github.com/liujingxing/rxhttp
                    https://github.com/liujingxing/rxlife
                """.trimIndent()
                )
                .addTypeVariables(rxHttpTypeNames)
                .superclass(rxHttpParam)
                .addFunctions(rxHttpPostCustomFun)
                .build()

            FileSpec.builder(rxHttpPackage, rxHttpName)
                .addType(rxHttpPostEncryptFormParamSpec)
                .build().writeTo(codeGenerator, Dependencies(false, ksClass.containingFile!!))
        }
        return funList
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