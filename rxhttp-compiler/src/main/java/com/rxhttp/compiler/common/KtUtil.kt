package com.rxhttp.compiler.common

import com.rxhttp.compiler.K_ARRAY_TYPE
import com.rxhttp.compiler.K_TYPE
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.ksp.isVararg
import com.rxhttp.compiler.ksp.parameterizedBy
import com.rxhttp.compiler.rxHttpPackage
import com.rxhttp.compiler.rxhttpKClass
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import java.util.Locale

/**
 * User: ljx
 * Date: 2023/6/23
 * Time: 20:23
 */

fun getRxHttpExtensionFileSpec(
    toObservableXxxFunList: List<FunSpec>,
    toAwaitXxxFunList: List<FunSpec>,
    toFlowXxxFunList: List<FunSpec>,
): FileSpec {
    val t = TypeVariableName("T")
    val listT = LIST.parameterizedBy(t)
    val reifiedT = t.copy(reified = true)
    val baseRxHttpName = rxhttpKClass.peerClass("BaseRxHttp")
    val observableCall = rxhttpKClass.peerClass("ObservableCall")
    val observableCallT = observableCall.parameterizedBy("T")

    val fileSpecBuilder = FileSpec.builder(rxHttpPackage, "RxHttpExtension")
        .addImport("rxhttp.wrapper.utils", "javaTypeOf")

    FunSpec.builder("executeList")
        .addModifiers(KModifier.INLINE)
        .receiver(baseRxHttpName)
        .addTypeVariable(reifiedT)
        .addStatement("return executeClass<List<T>>()")
        .returns(listT)
        .build()
        .apply { fileSpecBuilder.addFunction(this) }

    FunSpec.builder("executeClass")
        .addModifiers(KModifier.INLINE)
        .receiver(baseRxHttpName)
        .addTypeVariable(reifiedT)
        .addStatement("return executeClass<T>(javaTypeOf<T>())")
        .returns(t)
        .build()
        .apply { fileSpecBuilder.addFunction(this) }

    if (isDependenceRxJava()) {
        FunSpec.builder("toObservableList")
            .addModifiers(KModifier.INLINE)
            .receiver(baseRxHttpName)
            .addTypeVariable(reifiedT)
            .addStatement("return toObservable<List<T>>()")
            .returns(observableCall.parameterizedBy("List<T>"))
            .build()
            .apply { fileSpecBuilder.addFunction(this) }

        FunSpec.builder("toObservable")
            .addModifiers(KModifier.INLINE)
            .receiver(baseRxHttpName)
            .addTypeVariable(reifiedT)
            .addStatement("return toObservable<T>(javaTypeOf<T>())")
            .returns(observableCallT)
            .build()
            .apply { fileSpecBuilder.addFunction(this) }
        toObservableXxxFunList.forEach { fileSpecBuilder.addFunction(it) }
    }

    toAwaitXxxFunList.forEach { fileSpecBuilder.addFunction(it) }
    toFlowXxxFunList.forEach { fileSpecBuilder.addFunction(it) }
    return fileSpecBuilder.build()
}


//根据toAwaitXxx方法生成toFlowXxx方法
fun FunSpec.generateToFlowXxxFun(): List<FunSpec> {
    val callFactoryName = ClassName("rxhttp.wrapper", "CallFactory")
    val progressName = ClassName("rxhttp.wrapper.entity", "Progress")
    val progressTName = ClassName("rxhttp.wrapper.entity", "ProgressT")
    val progressSuspendLambdaName = LambdaTypeName
        .get(parameters = arrayOf(progressName), returnType = UNIT).copy(suspending = true)
    val toFlow = MemberName("rxhttp", "toFlow")
    val toFlowProgress = MemberName("rxhttp", "toFlowProgress")
    val bodyParamFactory = callFactoryName.peerClass("BodyParamFactory")
    val flow = ClassName("kotlinx.coroutines.flow", "Flow")

    val funList = mutableListOf<FunSpec>()
    val parseName = name.substring(7) // Remove the prefix `toAwait`
    val typeVariables = typeVariables
    val paramNames = parameters.toParamNames()
    val toAwaitXxxReturnType = returnType as ParameterizedTypeName
    val toAwaitXxxTypeArguments = toAwaitXxxReturnType.typeArguments
    FunSpec.builder("toFlow$parseName")
        .addModifiers(modifiers)
        .receiver(callFactoryName)
        .addParameters(parameters)
        .addTypeVariables(typeVariables)
        .addStatement(
            "return %M(toAwait$parseName${typeVariables.getTypeVariableString()}($paramNames))",
            toFlow
        )
        .returns(flow.parameterizedBy(toAwaitXxxTypeArguments))
        .build()
        .apply { funList.add(this) }

    if (typeVariables.isNotEmpty()) {
        val capacityParam = ParameterSpec.builder("capacity", INT)
            .defaultValue("2")
            .build()
        val isInLine = KModifier.INLINE in modifiers
        val builder = ParameterSpec.builder("progress", progressSuspendLambdaName)
        if (isInLine) builder.addModifiers(KModifier.NOINLINE)
        FunSpec.builder("toFlow$parseName")
            .addModifiers(modifiers)
            .receiver(bodyParamFactory)
            .addTypeVariables(typeVariables)
            .addParameters(parameters)
            .addParameter(capacityParam)
            .addParameter(builder.build())
            .addStatement(
                "return %M(toAwait$parseName${typeVariables.getTypeVariableString()}($paramNames), capacity, progress)",
                toFlow
            )
            .returns(flow.parameterizedBy(toAwaitXxxTypeArguments))
            .build()
            .apply { funList.add(this) }

        FunSpec.builder("toFlow${parseName}Progress")
            .addModifiers(modifiers)
            .receiver(bodyParamFactory)
            .addTypeVariables(typeVariables)
            .addParameters(parameters)
            .addParameter(capacityParam)
            .addStatement(
                "return %M(toAwait$parseName${typeVariables.getTypeVariableString()}($paramNames), capacity)",
                toFlowProgress
            )
            .returns(flow.parameterizedBy(progressTName.parameterizedBy(toAwaitXxxTypeArguments)))
            .build()
            .apply { funList.add(this) }
    }
    return funList
}

fun List<ParameterSpec>.flapTypeParameterSpecTypes(
    typeVariableNames: List<TypeVariableName>
): List<ParameterSpec> {
    val parameterSpecs = mutableListOf<ParameterSpec>()
    forEachIndexed { index, parameterSpec ->
        if (index == 0 && typeVariableNames.isNotEmpty() &&
            (parameterSpec.isArrayType() || parameterSpec.isVarargType())
        ) {
            typeVariableNames.mapTo(parameterSpecs) {
                val variableName = "${it.name.lowercase(Locale.getDefault())}Type"
                ParameterSpec.builder(variableName, K_TYPE).build()
            }
        } else {
            parameterSpecs.add(parameterSpec)
        }
    }
    return parameterSpecs
}

fun ParameterSpec.isArrayType() = type == K_ARRAY_TYPE
fun ParameterSpec.isVarargType() = isVararg() && type == K_TYPE