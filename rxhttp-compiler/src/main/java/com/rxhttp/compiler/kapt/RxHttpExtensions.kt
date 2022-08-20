package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.common.getParamsName
import com.rxhttp.compiler.common.getTypeOfString
import com.rxhttp.compiler.common.getTypeVariableString
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage
import com.rxhttp.compiler.rxhttpKClassName
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
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
    private val toFunList = ArrayList<FunSpec>()
    private val asFunList = ArrayList<FunSpec>()

    //根据@Parser注解，生成asXxx()、toXxx()、toFlowXxx()系列方法
    fun generateRxHttpExtendFun(typeElement: TypeElement, key: String) {

        //遍历获取泛型类型
        val typeVariableNames = typeElement.typeParameters.map {
            it.asTypeVariableName()
        }

        //遍历构造方法
        for (constructor in typeElement.getPublicConstructors()) {
            val tempParameters = constructor.parameters
            var fromIndex = typeVariableNames.size
            if ("java.lang.reflect.Type[]" ==
                tempParameters.firstOrNull()?.asType()?.toString()
            ) {
                fromIndex = 1
            }
            //构造方法参数数量小于泛型数量，直接过滤掉
            if (tempParameters.size < fromIndex) continue
            //移除前n个Type类型参数，n为泛型数量
            val parameters = tempParameters.subList(fromIndex, tempParameters.size)

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

            val parser = "%T$types($finalParams)"

            if (typeVariableNames.isNotEmpty()) {  //对声明了泛型的解析器，生成kotlin编写的asXxx方法
                FunSpec.builder("as$key")
                    .addModifiers(modifiers)
                    .receiver(baseRxHttpName)
                    .addParameters(parameterList)
                    .addStatement("return asParser($parser)", typeElement.asClassName()) //方法里面的表达式
                    .addTypeVariables(typeVariableNames.getTypeVariableNames())
                    .build()
                    .apply { asFunList.add(this) }
            }

            FunSpec.builder("to$key")
                .addModifiers(modifiers)
                .receiver(callFactoryName)
                .addParameters(parameterList)
                .addStatement("return toParser($parser)", typeElement.asClassName())  //方法里面的表达式
                .addTypeVariables(typeVariableNames.getTypeVariableNames())
                .build()
                .apply { toFunList.add(this) }
        }
    }


    fun generateClassFile(filer: Filer) {
        val t = TypeVariableName("T")
        val k = TypeVariableName("K")
        val v = TypeVariableName("V")

        val launchName = ClassName("kotlinx.coroutines", "launch")
        val progressName = ClassName("rxhttp.wrapper.entity", "Progress")
        val simpleParserName = ClassName("rxhttp.wrapper.parse", "SimpleParser")
        val coroutineScopeName = ClassName("kotlinx.coroutines", "CoroutineScope")

        val p = TypeVariableName("P")
        val r = TypeVariableName("R")
        val wildcard = TypeVariableName("*")
        val bodyParamName =
            ClassName("rxhttp.wrapper.param", "AbstractBodyParam").parameterizedBy(p)
        val rxHttpBodyParamName =
            ClassName(rxHttpPackage, "RxHttpAbstractBodyParam").parameterizedBy(p, r)
        val pBound = TypeVariableName("P", bodyParamName)
        val rBound = TypeVariableName("R", rxHttpBodyParamName)

        val progressSuspendLambdaName = LambdaTypeName.get(
            parameters = arrayOf(progressName),
            returnType = Unit::class.asClassName()
        ).copy(suspending = true)

        val fileBuilder = FileSpec.builder(rxHttpPackage, "RxHttp")
            .addImport("rxhttp.wrapper.utils", "javaTypeOf")
            .addImport("rxhttp", "toParser")

        val rxHttpName = rxhttpKClassName.parameterizedBy(wildcard, wildcard)
        FunSpec.builder("executeList")
            .addModifiers(KModifier.INLINE)
            .receiver(rxHttpName)
            .addTypeVariable(t.copy(reified = true))
            .addStatement("return executeClass<List<T>>()")
            .build()
            .apply { fileBuilder.addFunction(this) }

        FunSpec.builder("executeClass")
            .addModifiers(KModifier.INLINE)
            .receiver(rxHttpName)
            .addTypeVariable(t.copy(reified = true))
            .addStatement("return execute(%T<T>(javaTypeOf<T>()))", simpleParserName)
            .build()
            .apply { fileBuilder.addFunction(this) }

        if (isDependenceRxJava()) {
            FunSpec.builder("asList")
                .addModifiers(KModifier.INLINE)
                .receiver(baseRxHttpName)
                .addTypeVariable(t.copy(reified = true))
                .addStatement("return asClass<List<T>>()")
                .build()
                .apply { fileBuilder.addFunction(this) }

            FunSpec.builder("asMap")
                .addModifiers(KModifier.INLINE)
                .receiver(baseRxHttpName)
                .addTypeVariable(k.copy(reified = true))
                .addTypeVariable(v.copy(reified = true))
                .addStatement("return asClass<Map<K,V>>()")
                .build()
                .apply { fileBuilder.addFunction(this) }

            FunSpec.builder("asClass")
                .addModifiers(KModifier.INLINE)
                .receiver(baseRxHttpName)
                .addTypeVariable(t.copy(reified = true))
                .addStatement("return asParser(%T<T>(javaTypeOf<T>()))", simpleParserName)
                .build()
                .apply { fileBuilder.addFunction(this) }

            asFunList.forEach {
                fileBuilder.addFunction(it)
            }
        }

        val deprecatedAnnotation = AnnotationSpec.builder(Deprecated::class)
            .addMember(
                """
                "scheduled to be removed in RxHttp 3.0 release.", 
                level = DeprecationLevel.ERROR
            """.trimIndent()
            )
            .build()

        FunSpec.builder("upload")
            .addKdoc(
                """
                调用此方法监听上传进度                                                    
                @param coroutine  CoroutineScope对象，用于开启协程回调进度，进度回调所在线程取决于协程所在线程
                @param progress 进度回调  
                
                
                此方法已废弃，请使用Flow监听上传进度，性能更优，且更简单，如：
                
                ```
                RxHttp.postForm("/server/...")
                    .addFile("file", File("xxx/1.png"))
                    .toFlow<T> {   //这里也可选择你解析器对应的toFlowXxx方法
                        val currentProgress = it.progress //当前进度 0-100
                        val currentSize = it.currentSize  //当前已上传的字节大小
                        val totalSize = it.totalSize      //要上传的总字节大小    
                    }.catch {
                        //异常回调
                    }.collect {
                        //成功回调
                    }
                ```                   
                """.trimIndent()
            )
            .addAnnotation(deprecatedAnnotation)
            .receiver(rxHttpBodyParamName)
            .addTypeVariable(pBound)
            .addTypeVariable(rBound)
            .addParameter("coroutine", coroutineScopeName)
            .addParameter("progressCallback", progressSuspendLambdaName)
            .addCode(
                """
                param.setProgressCallback { progress, currentSize, totalSize ->
                    coroutine.%T { progressCallback(Progress(progress, currentSize, totalSize)) }
                }
                @Suppress("UNCHECKED_CAST")
                return this as R
                """.trimIndent(), launchName
            )
            .returns(r)
            .build()
            .apply { fileBuilder.addFunction(this) }

        val toFlow = MemberName("rxhttp", "toFlow")
        val toFlowProgress = MemberName("rxhttp", "toFlowProgress")
        val onEachProgress = MemberName("rxhttp", "onEachProgress")
        val bodyParamFactory = ClassName("rxhttp.wrapper", "BodyParamFactory")

        toFunList.forEach {
            fileBuilder.addFunction(it)
            val parseName = it.name.substring(2)
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
                    """return %M(to$parseName${getTypeVariableString(typeVariables)}($arguments))""",
                    toFlow
                )
                .build()
                .apply { fileBuilder.addFunction(this) }

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
                    """
                    return 
                        %M(to$parseName${getTypeVariableString(typeVariables)}($arguments), capacity)
                            .%M(progress)
                    """.trimIndent(), toFlowProgress, onEachProgress
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
                    """return %M(to$parseName${getTypeVariableString(typeVariables)}($arguments), capacity)""",
                    toFlowProgress
                )
                .build()
                .apply { fileBuilder.addFunction(this) }
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