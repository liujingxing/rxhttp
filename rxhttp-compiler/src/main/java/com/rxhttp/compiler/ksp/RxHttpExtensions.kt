package com.rxhttp.compiler.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.rxhttp.compiler.common.flapTypeParameterSpecTypes
import com.rxhttp.compiler.common.getRxHttpExtensionFileSpec
import com.rxhttp.compiler.common.getTypeOfString
import com.rxhttp.compiler.common.getTypeVariableString
import com.rxhttp.compiler.common.isArrayType
import com.rxhttp.compiler.common.toParamNames
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage
import com.rxhttp.compiler.rxhttpKClass
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.kspDependencies
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * User: ljx
 * Date: 2020/3/9
 * Time: 17:04
 */
class RxHttpExtensions(private val logger: KSPLogger) {

    private val baseRxHttpName = rxhttpKClass.peerClass("BaseRxHttp")
    private val callFactoryName = ClassName("rxhttp.wrapper", "CallFactory")
    private val awaitName = ClassName("rxhttp.wrapper.coroutines", "CallAwait")
    private val observableCall = rxhttpKClass.peerClass("ObservableCall")

    private val toFlowXxxFunList = ArrayList<FunSpec>()
    private val toAwaitXxxFunList = ArrayList<FunSpec>()
    private val toObservableXxxFunList = ArrayList<FunSpec>()

    //根据@Parser注解，生成toObservableXxx()、toAwaitXxx()、toFlowXxx()系列方法
    @KspExperimental
    fun generateRxHttpExtendFun(ksClass: KSClassDeclaration, key: String) {

        //遍历获取泛型类型
        val typeVariableNames = ksClass.typeParameters.map {
            it.toTypeVariableName().copy(reified = true)
        }
        val onParserFunReturnType = ksClass.getParserTypeParam() ?: return

        val typeCount = typeVariableNames.size  //泛型数量
        val customParser = ksClass.toClassName()
        //遍历构造方法
        for (constructor in ksClass.getConstructors()) {
            if (!constructor.isValid(typeCount)) continue
            val classTypeParams = ksClass.typeParameters.toTypeParameterResolver()
            val functionTypeParams =
                constructor.typeParameters.toTypeParameterResolver(classTypeParams)
            val originParameterSpecs = constructor.parameters.map {
                it.toKParameterSpec(functionTypeParams)
            }
            val typeParameterSpecs = originParameterSpecs.flapTypeParameterSpecTypes(typeVariableNames)
            //根据构造方法参数，获取toObservableXxx方法需要的参数
            val parameterList = typeParameterSpecs.subList(typeCount, typeParameterSpecs.size)

            val modifiers = ArrayList<KModifier>()
            if (typeVariableNames.isNotEmpty()) {
                modifiers.add(KModifier.INLINE)
            }

            val types = typeVariableNames.getTypeVariableString() // <T>, <K, V> 等
            val typeOfs = typeVariableNames.getTypeOfString()  // javaTypeOf<T>()等
            val paramNames = parameterList.toParamNames()  //构造方法参数名列表
            val finalParams = listOf(typeOfs, paramNames)
                .filter { it.isNotEmpty() }
                .joinToString()

            if (typeVariableNames.isNotEmpty() && isDependenceRxJava()) {  //对声明了泛型的解析器，生成kotlin编写的toObservableXxx方法
                val toObservableXxxFunName = "toObservable$key"
                val toObservableXxxFunBody = "return $toObservableXxxFunName$types($finalParams)"
                FunSpec.builder(toObservableXxxFunName)
                    .addOriginatingKSFile(ksClass.containingFile!!)
                    .addModifiers(modifiers)
                    .receiver(baseRxHttpName)
                    .addParameters(parameterList)
                    .addStatement(toObservableXxxFunBody) //方法里面的表达式
                    .addTypeVariables(typeVariableNames)
                    .returns(observableCall.parameterizedBy(onParserFunReturnType))
                    .build()
                    .apply { toObservableXxxFunList.add(this) }
            }

            val funBody: String
            val argType: TypeName =
                if (typeCount == 1 && onParserFunReturnType is TypeVariableName) {
                    val baseRxHttp = ClassName(rxHttpPackage, "BaseRxHttp")
                    val wrapFun = "%T.wrap${customParser.simpleName}"
                    funBody = "return %M($wrapFun($finalParams))"
                    baseRxHttp
                } else {
                    var params = finalParams
                    if (typeOfs.isNotEmpty() &&
                        originParameterSpecs.first().isArrayType() &&
                        onParserFunReturnType !is TypeVariableName
                    ) {
                        params = params.replace(typeOfs, "arrayOf($typeOfs)")
                    }
                    funBody = "return %M(%T$types($params))"
                    customParser
                }
            val toAwait = MemberName("rxhttp", "toAwait")
            FunSpec.builder("toAwait$key")
                .addOriginatingKSFile(ksClass.containingFile!!)
                .addModifiers(modifiers)
                .receiver(callFactoryName)
                .addParameters(parameterList)
                .addTypeVariables(typeVariableNames)
                .addCode(funBody, toAwait, argType)
                .returns(awaitName.parameterizedBy(onParserFunReturnType))
                .build()
                .apply { toAwaitXxxFunList.add(this) }

            val callFlow = ClassName("rxhttp.wrapper.coroutines", "CallFlow")
            val toFlow = MemberName("rxhttp", "toFlow")
            FunSpec.builder("toFlow$key")
                .addOriginatingKSFile(ksClass.containingFile!!)
                .addModifiers(modifiers)
                .receiver(callFactoryName)
                .addParameters(parameterList)
                .addTypeVariables(typeVariableNames)
                .addCode(funBody, toFlow, argType)
                .returns(callFlow.parameterizedBy(onParserFunReturnType))
                .build()
                .apply { toFlowXxxFunList.add(this) }
        }
    }


    fun generateClassFile(codeGenerator: CodeGenerator) {
        val fileSpec =
            getRxHttpExtensionFileSpec(toObservableXxxFunList, toAwaitXxxFunList, toFlowXxxFunList)
        val dependencies = fileSpec.kspDependencies(false)
        fileSpec.writeTo(codeGenerator, dependencies)
    }
}