package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.common.getParamsName
import com.rxhttp.compiler.common.getTypeOfString
import com.rxhttp.compiler.common.getTypeVariableString
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage
import com.rxhttp.compiler.rxhttpKClassName
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.asTypeVariableName
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

    private val baseRxHttpName = ClassName(rxHttpPackage, "BaseRxHttp")
    private val callFactoryName = ClassName("rxhttp.wrapper", "CallFactory")
    private val wildcard = TypeVariableName("*")
    private val rxHttpBodyParamName = ClassName(rxHttpPackage, "RxHttpAbstractBodyParam")
        .parameterizedBy(wildcard, wildcard)
    private val progressName = ClassName("rxhttp.wrapper.entity", "Progress")
    private val toFunList = ArrayList<FunSpec>()
    private val asFunList = ArrayList<FunSpec>()
    private val wrapFunList = ArrayList<FunSpec>()

    //根据@Parser注解，生成asXxx()、toXxx()、toFlowXxx()系列方法
    fun generateRxHttpExtendFun(typeElement: TypeElement, key: String) {

        //遍历获取泛型类型
        val typeVariableNames = typeElement.typeParameters.map {
            it.asTypeVariableName()
        }

        val customParserClassName = typeElement.asClassName()
        //遍历构造方法
        for (constructor in typeElement.getPublicConstructors()) {
            val tempParameters = constructor.parameters
            var typeCount = typeVariableNames.size
            if ("java.lang.reflect.Type[]" == tempParameters.firstOrNull()?.asType()?.toString()) {
                typeCount = 1  //如果是Type是数组传递的，一个参数就行
            } else {
                //如果解析器有n个泛型，则构造方法前n个参数，必须是Type类型
                val match = tempParameters.subList(0, typeCount).all {
                    "java.lang.reflect.Type" == it.asType().toString()
                }
                if (!match) continue
            }
            //构造方法参数数量小于泛型数量，直接过滤掉
            if (tempParameters.size < typeCount) continue
            //移除前n个Type类型参数，n为泛型数量
            val parameters = tempParameters.subList(typeCount, tempParameters.size)

            //根据构造方法参数，获取asXxx方法需要的参数
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

            if (typeVariableNames.isNotEmpty() && isDependenceRxJava()) {  //对声明了泛型的解析器，生成kotlin编写的asXxx方法
                val asXxxFunName = "toObservable$key"
                val asXxxFunBody = "return $asXxxFunName$types($finalParams)"
                val rxHttpName = rxhttpKClassName.parameterizedBy(wildcard, wildcard)
                FunSpec.builder(asXxxFunName)
                    .addModifiers(modifiers)
                    .receiver(rxHttpName)
                    .addParameters(parameterList)
                    .addStatement(asXxxFunBody) //方法里面的表达式
                    .addTypeVariables(typeVariableNames.getTypeVariableNames())
                    .build()
                    .apply { asFunList.add(this) }

//                val schedulerParam = ParameterSpec
//                    .builder("scheduler", getKClassName("Scheduler").copy(nullable = true))
//                    .defaultValue("null")
//                    .build()

//                FunSpec.builder("as$key")
//                    .addModifiers(modifiers)
//                    .receiver(rxHttpBodyParamName)
//                    .addParameters(parameterList)
//                    .addParameter(schedulerParam)
//                    .addParameter(
//                        "progressConsumer",
//                        getKClassName("Consumer").parameterizedBy(progressName)
//                    )
//                    .addStatement(
//                        "return asParser($parser).onUploadProgress(scheduler, progressConsumer)",
//                        typeElement.asClassName()
//                    ) //方法里面的表达式
//                    .addTypeVariables(typeVariableNames.getTypeVariableNames())
//                    .build()
//                    .apply { asFunList.add(this) }
            }

            val parser = "%T$types($finalParams)"

            val toXxxFunBody = if (typeVariableNames.size == 1) {
                CodeBlock.of("return toAwait(wrap${customParserClassName.simpleName}$types($finalParams))")
            } else {
                CodeBlock.of("return toAwait(%T$types($finalParams))", customParserClassName)
            }

            FunSpec.builder("toAwait$key")
                .addModifiers(modifiers)
                .receiver(callFactoryName)
                .addParameters(parameterList)
                .addCode(toXxxFunBody)  //方法里面的表达式
                .addTypeVariables(typeVariableNames.getTypeVariableNames())
                .build()
                .apply { toFunList.add(this) }

            if (typeVariableNames.size == 1) {
                val t = TypeVariableName("T")
                val type = ClassName("java.lang.reflect", "Type")
                val parameterizedType = ClassName("java.lang.reflect", "ParameterizedType")
                val okResponse = ClassName("rxhttp.wrapper.entity", "OkResponse")
                val okResponseParser = ClassName("rxhttp.wrapper.parse", "OkResponseParser")
                val parserClass = ClassName("rxhttp.wrapper.parse", "Parser").parameterizedBy(t)

                val suppressAnnotation = AnnotationSpec.builder(Suppress::class)
                    .addMember("\"UNCHECKED_CAST\"")
                    .build()

                FunSpec.builder("wrap${customParserClassName.simpleName}")
                    .addTypeVariable(t)
                    .addParameter("type", type)
                    .addAnnotation(suppressAnnotation)
                    .addParameters(parameterList)
                    .returns(parserClass)
                    .addCode(
                        """
                return 
                    if (type is %T && type.rawType === %T::class.java) {
                        val actualType = type.actualTypeArguments[0]
                        %T(%T<Any>(actualType)) as Parser<T>
                    } else {
                        %T(type)
                    }
                """.trimIndent(), parameterizedType, okResponse, okResponseParser,
                        customParserClassName, customParserClassName
                    ).build().apply { wrapFunList.add(this) }
            }
        }
    }


    fun generateClassFile(filer: Filer) {
        val t = TypeVariableName("T")
        val v = TypeVariableName("V")

        val reifiedT = t.copy(reified = true)

        val progressSuspendLambdaName = LambdaTypeName.get(
            parameters = arrayOf(progressName),
            returnType = Unit::class.asClassName()
        ).copy(suspending = true)

        val fileBuilder = FileSpec.builder(rxHttpPackage, "RxHttp")
            .addImport("rxhttp.wrapper.utils", "javaTypeOf")
            .addImport("rxhttp", "toAwait")

        val rxHttpName = rxhttpKClassName.parameterizedBy(wildcard, wildcard)
        FunSpec.builder("executeList")
            .addModifiers(KModifier.INLINE)
            .receiver(rxHttpName)
            .addTypeVariable(reifiedT)
            .addStatement("return executeClass<List<T>>()")
            .build()
            .apply { fileBuilder.addFunction(this) }

        FunSpec.builder("executeClass")
            .addModifiers(KModifier.INLINE)
            .receiver(rxHttpName)
            .addTypeVariable(reifiedT)
            .addStatement("return executeClass<T>(javaTypeOf<T>())")
            .build()
            .apply { fileBuilder.addFunction(this) }

        if (isDependenceRxJava()) {
            FunSpec.builder("toObservableList")
                .addModifiers(KModifier.INLINE)
                .receiver(baseRxHttpName)
                .addTypeVariable(reifiedT)
                .addStatement("return toObservable<List<T>>()")
                .build()
                .apply { fileBuilder.addFunction(this) }

            FunSpec.builder("toObservableMapString")
                .addModifiers(KModifier.INLINE)
                .receiver(baseRxHttpName)
                .addTypeVariable(v.copy(reified = true))
                .addStatement("return toObservable<Map<String, V>>()")
                .build()
                .apply { fileBuilder.addFunction(this) }

            FunSpec.builder("toObservable")
                .addModifiers(KModifier.INLINE)
                .receiver(baseRxHttpName)
                .addTypeVariable(reifiedT)
                .addStatement("return toObservable<T>(javaTypeOf<T>())")
                .build()
                .apply { fileBuilder.addFunction(this) }

//            val schedulerParam = ParameterSpec
//                .builder("scheduler", getKClassName("Scheduler").copy(nullable = true))
//                .defaultValue("null")
//                .build()

//            FunSpec.builder("asClass")
//                .addModifiers(KModifier.INLINE)
//                .receiver(rxHttpBodyParamName)
//                .addTypeVariable(reifiedT)
//                .addParameter(schedulerParam)
//                .addParameter(
//                    "progressConsumer",
//                    getKClassName("Consumer").parameterizedBy(progressName)
//                )
//                .addCode("return asParser(SmartParser<T>(javaTypeOf<T>())).onUploadProgress(scheduler, progressConsumer)")
//                .build()
//                .apply { fileBuilder.addFunction(this) }

            asFunList.forEach {
                fileBuilder.addFunction(it)
            }
        }

        wrapFunList.forEach { fileBuilder.addFunction(it) }

        val toFlow = MemberName("rxhttp", "toFlow")
        val toFlowProgress = MemberName("rxhttp", "toFlowProgress")
        val bodyParamFactory = ClassName("rxhttp.wrapper", "BodyParamFactory")

        toFunList.forEach {
            fileBuilder.addFunction(it)
            val parseName = it.name.substring(7) // Remove the prefix `toAwait`
            val typeVariables = it.typeVariables
            val arguments = StringBuilder()
            it.parameters.forEach { p ->
                if (KModifier.VARARG in p.modifiers) {
                    arguments.append("*")
                }
                arguments.append(p.name).append(",")
            }
            if (arguments.isNotEmpty()) arguments.deleteCharAt(arguments.length - 1)
            FunSpec.builder("toFlow$parseName")
                .addModifiers(it.modifiers)
                .receiver(callFactoryName)
                .addParameters(it.parameters)
                .addTypeVariables(typeVariables)
                .addStatement(
                    """return %M(toAwait$parseName${getTypeVariableString(typeVariables)}($arguments))""",
                    toFlow
                )
                .build()
                .apply { fileBuilder.addFunction(this) }

            if (typeVariables.isNotEmpty()){
                val capacityParam = ParameterSpec.builder("capacity", Int::class)
                    .defaultValue("1")
                    .build()
                val isInLine = KModifier.INLINE in it.modifiers
                val builder = ParameterSpec.builder("progress", progressSuspendLambdaName)
                if (isInLine) builder.addModifiers(KModifier.NOINLINE)
                FunSpec.builder("toFlow$parseName")
                    .addModifiers(it.modifiers)
                    .receiver(bodyParamFactory)
                    .addTypeVariables(typeVariables)
                    .addParameters(it.parameters)
                    .addParameter(capacityParam)
                    .addParameter(builder.build())
                    .addCode(
                        "return %M(toAwait$parseName${getTypeVariableString(typeVariables)}($arguments), capacity, progress)",
                        toFlow
                    )
                    .build()
                    .apply { fileBuilder.addFunction(this) }

                FunSpec.builder("toFlow${parseName}Progress")
                    .addModifiers(it.modifiers)
                    .receiver(bodyParamFactory)
                    .addTypeVariables(typeVariables)
                    .addParameters(it.parameters)
                    .addParameter(capacityParam)
                    .addCode(
                        "return %M(toAwait$parseName${getTypeVariableString(typeVariables)}($arguments), capacity)",
                        toFlowProgress
                    )
                    .build()
                    .apply { fileBuilder.addFunction(this) }
            }
        }

        fileBuilder.build().writeTo(filer)
    }
}

//获取泛型对象列表
private fun List<TypeVariableName>.getTypeVariableNames(): List<TypeVariableName> {
    val anyTypeName = Any::class.asTypeName()
    return map {
        val bounds = it.bounds //泛型边界
        if (bounds.isEmpty() || (bounds.size == 1 && bounds[0].toString() == "java.lang.Object")) {
            TypeVariableName(it.name, anyTypeName).copy(reified = true)
        } else {
            (it.toKClassTypeName() as TypeVariableName).copy(reified = true)
        }
    }
}