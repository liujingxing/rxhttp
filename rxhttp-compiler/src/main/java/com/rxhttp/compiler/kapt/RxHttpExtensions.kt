package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.common.flapTypeParameterSpecTypes
import com.rxhttp.compiler.common.generateToFlowXxxFun
import com.rxhttp.compiler.common.getRxHttpExtensionFileSpec
import com.rxhttp.compiler.common.getTypeOfString
import com.rxhttp.compiler.common.getTypeVariableString
import com.rxhttp.compiler.common.isArrayType
import com.rxhttp.compiler.common.toParamNames
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage
import com.rxhttp.compiler.rxhttpKClass
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BOOLEAN_ARRAY
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.CHAR_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.DOUBLE_ARRAY
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FLOAT_ARRAY
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.INT_ARRAY
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LONG_ARRAY
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.SHORT_ARRAY
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.U_BYTE_ARRAY
import com.squareup.kotlinpoet.U_INT_ARRAY
import com.squareup.kotlinpoet.U_LONG_ARRAY
import com.squareup.kotlinpoet.U_SHORT_ARRAY
import com.squareup.kotlinpoet.javapoet.JClassName
import com.squareup.kotlinpoet.javapoet.JTypeName
import com.squareup.kotlinpoet.javapoet.JTypeVariableName
import com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview
import com.squareup.kotlinpoet.javapoet.toKClassName
import com.squareup.kotlinpoet.javapoet.toKTypeName
import com.squareup.kotlinpoet.javapoet.toKTypeVariableName
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
            JTypeVariableName.get(it).toKTypeVariableName().copy(reified = true)
        }
        val onParserFunReturnType = typeElement.findOnParserFunReturnType()?.toKTypeName() ?: return
        val typeCount = typeVariableNames.size  //泛型数量
        val customParser = JClassName.get(typeElement).toKClassName()
        //遍历构造方法
        for (constructor in typeElement.getConstructors()) {
            if (!constructor.isValid(typeCount)) continue
            val parameters = constructor.parameters
            val varArgsFun = constructor.isVarArgs  //该构造方法是否携带可变参数，即是否为可变参数方法
            val originParameterSpecs = parameters.mapIndexed { index, variableElement ->
                val variableType = variableElement.asType()
                val variableName = variableElement.simpleName.toString()
                val annotation = variableElement.getAnnotation(Nullable::class.java)
                var typeName = JTypeName.get(variableType).toKTypeName()
                val isVarArg = varArgsFun
                        && index == parameters.lastIndex
                        && variableType.kind == TypeKind.ARRAY
                if (isVarArg) {  //最后一个参数是可变参数
                    typeName = when (typeName) {
                        BOOLEAN_ARRAY -> BOOLEAN
                        BYTE_ARRAY, U_BYTE_ARRAY -> BYTE
                        CHAR_ARRAY -> CHAR
                        SHORT_ARRAY, U_SHORT_ARRAY -> SHORT
                        INT_ARRAY, U_INT_ARRAY -> INT
                        LONG_ARRAY, U_LONG_ARRAY -> LONG
                        FLOAT_ARRAY -> FLOAT
                        DOUBLE_ARRAY -> DOUBLE
                        is ParameterizedTypeName -> typeName.typeArguments.first()
                        else -> typeName
                    }
                }
                if (annotation != null) typeName = typeName.copy(true)
                ParameterSpec.builder(variableName, typeName).apply {
                    if (isVarArg) {
                        addModifiers(KModifier.VARARG)
                    }
                }.build()
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
                    .addModifiers(modifiers)
                    .receiver(baseRxHttpName)
                    .addParameters(parameterList)
                    .addStatement(toObservableXxxFunBody) //方法里面的表达式
                    .addTypeVariables(typeVariableNames)
                    .returns(observableCall.parameterizedBy(onParserFunReturnType))
                    .build()
                    .apply { toObservableXxxFunList.add(this) }
            }

            val wrapCustomParser =
                MemberName(rxHttpPackage, "BaseRxHttp.wrap${customParser.simpleName}")
            val toAwaitXxxFunBody =
                if (typeCount == 1 && onParserFunReturnType is TypeVariableName) {
                    CodeBlock.of("return toAwait(%M$types($finalParams))", wrapCustomParser)
                } else {
                    var params = finalParams
                    if (typeOfs.isNotEmpty() &&
                        originParameterSpecs.first().isArrayType() &&
                        onParserFunReturnType !is TypeVariableName
                    ) {
                        params = params.replace(typeOfs, "arrayOf($typeOfs)")
                    }
                    CodeBlock.of("return toAwait(%T$types($params))", customParser)
                }

            val toAwaitXxxFun = FunSpec.builder("toAwait$key")
                .addModifiers(modifiers)
                .receiver(callFactoryName)
                .addParameters(parameterList)
                .addCode(toAwaitXxxFunBody)  //方法里面的表达式
                .addTypeVariables(typeVariableNames)
                .returns(awaitName.parameterizedBy(onParserFunReturnType))
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