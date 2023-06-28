package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.common.generateToFlowXxxFun
import com.rxhttp.compiler.common.getParamsName
import com.rxhttp.compiler.common.getRxHttpExtensionFileSpec
import com.rxhttp.compiler.common.getTypeOfString
import com.rxhttp.compiler.common.getTypeVariableString
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.ksp.parameterizedBy
import com.rxhttp.compiler.rxHttpPackage
import com.rxhttp.compiler.rxhttpKClass
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.asTypeVariableName
import com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview
import com.squareup.kotlinpoet.javapoet.toKTypeName
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.Filer
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind

/**
 * User: ljx
 * Date: 2020/3/9
 * Time: 17:04
 */
class RxHttpExtensions {

    private val baseRxHttpName = rxhttpKClass.peerClass("BaseRxHttp")
    private val callFactoryName = ClassName("rxhttp.wrapper", "CallFactory")
    private val awaitName = ClassName("rxhttp.wrapper.coroutines", "Await")
    private val observableCall = rxhttpKClass.peerClass("ObservableCall")

    private val toFlowXxxFunList = ArrayList<FunSpec>()
    private val toAwaitXxxFunList = ArrayList<FunSpec>()
    private val toObservableXxxFunList = ArrayList<FunSpec>()

    //根据@Parser注解，生成toObservableXxx()、toAwaitXxx()、toFlowXxx()系列方法
    @OptIn(KotlinPoetJavaPoetPreview::class)
    fun generateRxHttpExtendFun(typeElement: TypeElement, key: String) {
        //遍历获取泛型类型
        val typeVariableNames = typeElement.typeParameters.map {
            it.asTypeVariableName().toKTypeVariableNames()
        }
        val onParserFunReturnType = typeElement.getOnParserFunReturnType() ?: return
        val onParserFunReturnKType = onParserFunReturnType.toKTypeName()
        val constructors = typeElement.getPublicConstructors()
        val typeCount = typeVariableNames.size  //泛型数量
        val customParser = typeElement.asClassName()
        //遍历构造方法
        for (constructor in constructors) {
            //参数为空，说明该构造方法无效
            val parameters = constructor.getParametersIfValid(typeCount) ?: continue

            //根据构造方法参数，获取toObservableXxx方法需要的参数
            val varArgsFun = constructor.isVarArgs  //该构造方法是否携带可变参数，即是否为可变参数方法
            val parameterList = parameters.mapIndexed { index, variableElement ->
                val variableType = variableElement.asType()
                val variableName = variableElement.simpleName.toString()
                val annotation = variableElement.getAnnotation(Nullable::class.java)
                var type = variableType.asTypeName()
                val isVarArg = varArgsFun
                        && index == constructor.parameters.lastIndex
                        && variableType.kind == TypeKind.ARRAY
                if (isVarArg) {  //最后一个参数是可变参数
                    if (type is ParameterizedTypeName) {
                        type = type.typeArguments[0].toKClassTypeName()
                    }
                } else {
                    type = type.toKClassTypeName()
                }
                if (annotation != null) type = type.copy(true)
                val parameterSpecBuilder = ParameterSpec.builder(variableName, type)
                if (isVarArg) {
                    parameterSpecBuilder.addModifiers(KModifier.VARARG)
                }
                parameterSpecBuilder.build()
            }

            val modifiers = ArrayList<KModifier>()
            if (typeVariableNames.isNotEmpty()) {
                modifiers.add(KModifier.INLINE)
            }

            val types = getTypeVariableString(typeVariableNames) // <T>, <K, V> 等
            val typeOfs = getTypeOfString(typeVariableNames)  // javaTypeOf<T>()等
            val params = getParamsName(parameterList)  //构造方法参数名列表
            val finalParams = when {
                typeOfs.isEmpty() -> params
                params.isEmpty() -> typeOfs
                else -> "$typeOfs, $params"
            }

            if (typeVariableNames.isNotEmpty() && isDependenceRxJava()) {  //对声明了泛型的解析器，生成kotlin编写的toObservableXxx方法
                val toObservableFunName = "toObservable$key"
                val toObservableFunBody = "return $toObservableFunName$types($finalParams)"
                FunSpec.builder(toObservableFunName)
                    .addModifiers(modifiers)
                    .receiver(baseRxHttpName)
                    .addParameters(parameterList)
                    .addStatement(toObservableFunBody) //方法里面的表达式
                    .addTypeVariables(typeVariableNames)
                    .returns(observableCall.parameterizedBy(onParserFunReturnKType))
                    .build()
                    .apply { toObservableXxxFunList.add(this) }
            }

            val wrapCustomParser =
                MemberName(rxHttpPackage, "BaseRxHttp.wrap${customParser.simpleName}")
            val toAwaitXxxFunBody =
                if (typeCount == 1 && onParserFunReturnType is com.squareup.javapoet.TypeVariableName) {
                    CodeBlock.of("return toAwait(%M$types($finalParams))", wrapCustomParser)
                } else {
                    CodeBlock.of("return toAwait(%T$types($finalParams))", customParser)
                }

            val toAwaitXxxFun = FunSpec.builder("toAwait$key")
                .addModifiers(modifiers)
                .receiver(callFactoryName)
                .addParameters(parameterList)
                .addCode(toAwaitXxxFunBody)  //方法里面的表达式
                .addTypeVariables(typeVariableNames)
                .returns(awaitName.parameterizedBy(onParserFunReturnKType))
                .build()

            toAwaitXxxFunList.add(toAwaitXxxFun)
            toFlowXxxFunList.addAll(toAwaitXxxFun.generateToFlowXxxFun())
        }
    }

    fun generateClassFile(filer: Filer) {
        val fileSpec =
            getRxHttpExtensionFileSpec(toObservableXxxFunList, toAwaitXxxFunList, toFlowXxxFunList)
        fileSpec.writeTo(filer)
    }
}

//获取泛型对象列表
private fun TypeVariableName.toKTypeVariableNames(): TypeVariableName {
    val bounds = bounds //泛型边界
    return if (bounds.isEmpty() || (bounds.size == 1 && bounds[0].toString() == "java.lang.Object")) {
        TypeVariableName(name).copy(reified = true)
    } else {
        (toKClassTypeName() as TypeVariableName).copy(reified = true)
    }
}