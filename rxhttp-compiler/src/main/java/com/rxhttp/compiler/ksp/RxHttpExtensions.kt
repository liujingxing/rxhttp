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
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.kspDependencies
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * User: ljx
 * Date: 2020/3/9
 * Time: 17:04
 */
class RxHttpExtensions(private val logger: KSPLogger) {

    private val baseRxHttpName = ClassName(rxHttpPackage, "BaseRxHttp")
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

        val constructors = ksClass.getPublicConstructors()

        val customParserClassName = ksClass.toClassName()
        //遍历构造方法
        for (constructor in constructors) {
            val tempParameters = constructor.parameters
            var typeCount = typeVariableNames.size
            val typeArray = "kotlin.Array<java.lang.reflect.Type>"
            if (typeArray == tempParameters.firstOrNull()?.type?.toTypeName()?.toString()) {
                typeCount = 1  //如果是Type是数组传递的，一个参数就行
            } else {
                //如果解析器有n个泛型，则构造方法前n个参数，必须是Type类型
                val match = tempParameters.subList(0, typeCount).all {
                    "java.lang.reflect.Type" == it.type.getQualifiedName()
                }
                if (!match) continue
            }
            //构造方法参数数量小于泛型数量，直接过滤掉
            if (tempParameters.size < typeCount) continue
            //移除前n个Type类型参数，n为泛型数量
            val parameters = tempParameters.subList(typeCount, tempParameters.size)
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
//                        ksClass.toClassName()
//                    ) //方法里面的表达式
//                    .addTypeVariables(typeVariableNames)
//                    .build()
//                    .apply { asFunList.add(this) }
            }

            val toAwaitXxxFunBody = if (typeVariableNames.size == 1) {
                CodeBlock.of("return toAwait(wrap${customParserClassName.simpleName}$types($finalParams))")
            } else {
                CodeBlock.of("return toAwait(%T$types($finalParams))", customParserClassName)
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