package com.rxhttp.compiler.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.rxhttp.compiler.common.getParamsName
import com.rxhttp.compiler.common.getTypeOfString
import com.rxhttp.compiler.common.getTypeVariableString
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage
import com.rxhttp.compiler.rxhttpKClass
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
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
    private val progressName = ClassName("rxhttp.wrapper.entity", "Progress")
    private val toFunList = ArrayList<FunSpec>()
    private val asFunList = ArrayList<FunSpec>()
    private val wrapFunList = ArrayList<FunSpec>()

    //根据@Parser注解，生成toObservableXxx()、toAwaitXxx()、toFlowXxx()系列方法
    @KspExperimental
    fun generateRxHttpExtendFun(ksClass: KSClassDeclaration, key: String) {

        //遍历获取泛型类型
        val typeVariableNames = ksClass.typeParameters
            .map { it.toTypeVariableName().copy(reified = true) }
        val onParserFunReturnType = ksClass.findOnParserFunReturnType() ?: return

        val constructors = ksClass.getPublicConstructors()
        val typeCount = typeVariableNames.size  //泛型数量
        val customParser = ksClass.toClassName()
        //遍历构造方法
        for (constructor in constructors) {
            //参数为空，说明该构造方法无效
            val parameters = constructor.getParametersIfValid(typeCount) ?: continue

            val classTypeParams = ksClass.typeParameters.toTypeParameterResolver()
            val functionTypeParams =
                constructor.typeParameters.toTypeParameterResolver(classTypeParams)
            //根据构造方法参数，获取toObservableXxx方法需要的参数
            val parameterList = parameters.map { it.toKParameterSpec(functionTypeParams) }

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
                val toObservableXxxFunName = "toObservable$key"
                val toObservableXxxFunBody = "return $toObservableXxxFunName$types($finalParams)"
                FunSpec.builder(toObservableXxxFunName)
                    .addModifiers(modifiers)
                    .receiver(baseRxHttpName)
                    .addParameters(parameterList)
                    .addStatement(toObservableXxxFunBody) //方法里面的表达式
                    .addTypeVariables(typeVariableNames)
                    .build()
                    .apply { asFunList.add(this) }
            }

            val wrapCustomParser = MemberName(rxHttpPackage, "BaseRxHttp.wrap${customParser.simpleName}")
            val toAwaitXxxFunBody = if (typeCount == 1 && onParserFunReturnType is TypeVariableName) {
                CodeBlock.of("return toAwait(%M$types($finalParams))", wrapCustomParser)
            } else {
                CodeBlock.of("return toAwait(%T$types($finalParams))", customParser)
            }

            FunSpec.builder("toAwait$key")
                .addOriginatingKSFile(ksClass.containingFile!!)
                .addModifiers(modifiers)
                .receiver(callFactoryName)
                .addParameters(parameterList)
                .addCode(toAwaitXxxFunBody)  //方法里面的表达式
                .addTypeVariables(typeVariableNames)
                .build()
                .apply { toFunList.add(this) }
        }
    }


    fun generateClassFile(codeGenerator: CodeGenerator) {
        val t = TypeVariableName("T")
        val v = TypeVariableName("V")

        val reifiedT = t.copy(reified = true)

        val progressSuspendLambdaName = LambdaTypeName.get(
            parameters = arrayOf(progressName),
            returnType = UNIT
        ).copy(suspending = true)

        val fileBuilder = FileSpec.builder(rxHttpPackage, "RxHttpExtension")
            .addImport("rxhttp.wrapper.utils", "javaTypeOf")
            .addImport("rxhttp", "toAwait")

        FunSpec.builder("executeList")
            .addModifiers(KModifier.INLINE)
            .receiver(baseRxHttpName)
            .addTypeVariable(reifiedT)
            .addStatement("return executeClass<List<T>>()")
            .build()
            .apply { fileBuilder.addFunction(this) }

        FunSpec.builder("executeClass")
            .addModifiers(KModifier.INLINE)
            .receiver(baseRxHttpName)
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

            asFunList.forEach { fileBuilder.addFunction(it) }
        }

        wrapFunList.forEach { fileBuilder.addFunction(it) }

        val toFlow = MemberName("rxhttp", "toFlow")
        val toFlowProgress = MemberName("rxhttp", "toFlowProgress")
        val bodyParamFactory = callFactoryName.peerClass("BodyParamFactory")

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
                    "return %M(toAwait$parseName${getTypeVariableString(typeVariables)}($arguments))",
                    toFlow
                )
                .build()
                .apply { fileBuilder.addFunction(this) }

            if (typeVariables.isNotEmpty()) {
                val capacityParam = ParameterSpec.builder("capacity", INT)
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
        val fileSpec = fileBuilder.build()
        val dependencies = fileSpec.kspDependencies(false)
        fileSpec.writeTo(codeGenerator, dependencies)
    }
}