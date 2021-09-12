package com.rxhttp.compiler

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.Filer
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind

/**
 * User: ljx
 * Date: 2020/3/9
 * Time: 17:04
 */
class RxHttpExtensions {

    private val classTypeName = Class::class.asClassName()
    private val anyTypeName = Any::class.asTypeName()

    private val baseRxHttpName = ClassName(rxHttpPackage, "BaseRxHttp")
    private val iRxHttpName = ClassName("rxhttp", "IRxHttp")
    private val toFunList = ArrayList<FunSpec>()
    private val asFunList = ArrayList<FunSpec>()

    //根据@Parser注解，生成asXxx()、awaitXxx()类型方法
    fun generateRxHttpExtendFun(typeElement: TypeElement, key: String) {

        val typeVariableNames = ArrayList<TypeVariableName>()
        //遍历获取泛型类型
        typeElement.typeParameters.forEach {
            typeVariableNames.add(it.asTypeVariableName())
        }

        //遍历构造方法
        for (executableElement in getConstructorFun(typeElement)) {

            if (typeVariableNames.size > 0
                && executableElement.modifiers.contains(Modifier.PUBLIC)
            ) {
                if (executableElement.parameters.size == 1
                    && executableElement.parameters[0].asType()
                        .toString() == "java.lang.reflect.Type[]"
                ) {
                    continue
                }

                var allTypeArg = true
                //构造方法参数数量等于泛型数量
                if (executableElement.parameters.size > typeVariableNames.size) {
                    for (variableElement in executableElement.parameters) {
                        if (variableElement.asType().toString() != "java.lang.reflect.Type") {
                            allTypeArg = false
                            break
                        }
                    }
                }
                if (allTypeArg) continue
            }

            //根据构造方法参数，获取asXxx方法需要的参数
            val parameterList = ArrayList<ParameterSpec>()
            var typeIndex = 0
            val varArgsFun = executableElement.isVarArgs  //该构造方法是否携带可变参数，即是否为可变参数方法
            executableElement.parameters.forEachIndexed { index, variableElement ->
                val variableType = variableElement.asType()
                val variableName = variableElement.simpleName.toString()
                val parameterSpec = if (variableType.toString() == "java.lang.reflect.Type"
                    && typeIndex < typeVariableNames.size
                ) {  //Type类型参数转Class<T>类型
                    ParameterSpec.builder(
                        variableName,
                        classTypeName.parameterizedBy(typeVariableNames[typeIndex++])
                    ).build()
                } else {
                    val annotation = variableElement.getAnnotation(Nullable::class.java)
                    var type = variableType.asTypeName()
                    val isVarArg = varArgsFun
                        && index == executableElement.parameters.lastIndex
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
                        .jvmModifiers(variableElement.modifiers)
                    if (isVarArg) {
                        parameterSpecBuilder.addModifiers(KModifier.VARARG)
                    }
                    parameterSpecBuilder.build()
                }
                parameterList.add(parameterSpec)
            }

            val modifiers = ArrayList<KModifier>()
            if (typeVariableNames.size > 0) {
                modifiers.add(KModifier.INLINE)
            }

            var funBody = if (typeVariableNames.size == 0 || executableElement.modifiers.contains(Modifier.PUBLIC)) {
                "return asParser(%T${getTypeVariableString(typeVariableNames)}(${getParamsName(parameterList)}))"
            } else {
                "return asParser(object: %T${getTypeVariableString(typeVariableNames)}(${getParamsName(parameterList)}) {})"
            }

            if (typeVariableNames.size > 0) {  //对声明了泛型的解析器，生成kotlin编写的asXxx方法
                asFunList.add(
                    FunSpec.builder("as$key")
                        .addModifiers(modifiers)
                        .receiver(baseRxHttpName)
                        .addParameters(parameterList)
                        .addStatement(funBody, typeElement.asClassName()) //方法里面的表达式
                        .addTypeVariables(getTypeVariableNames(typeVariableNames))
                        .build())
            }

            funBody = if (typeVariableNames.size == 0 || executableElement.modifiers.contains(Modifier.PUBLIC)) {
                "return %T(%T${getTypeVariableString(typeVariableNames)}(${getParamsName(parameterList)}))"
            } else {
                "return %T(object: %T${getTypeVariableString(typeVariableNames)}(${getParamsName(parameterList)}) {})"
            }

            val toParserName = ClassName("rxhttp", "toParser")
            toFunList.add(
                FunSpec.builder("to$key")
                    .addModifiers(modifiers)
                    .receiver(ClassName("rxhttp", "IRxHttp"))
                    .addParameters(parameterList)
                    .addStatement(funBody, toParserName, typeElement.asClassName())  //方法里面的表达式
                    .addTypeVariables(getTypeVariableNames(typeVariableNames))
                    .build())
        }
    }


    fun generateClassFile(filer: Filer) {
        val t = TypeVariableName("T")
        val k = TypeVariableName("K")
        val v = TypeVariableName("V")
        val tAny = TypeVariableName("T", anyTypeName)

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
        val rxHttpBodyWildcardParamName = ClassName(rxHttpPackage, "RxHttpAbstractBodyParam")
            .parameterizedBy(wildcard, wildcard)
        val pBound = TypeVariableName("P", bodyParamName)
        val rBound = TypeVariableName("R", rxHttpBodyParamName)


        val progressLambdaName = LambdaTypeName.get(
            parameters = arrayOf(progressName),
            returnType = Unit::class.asClassName()
        )

        val fileBuilder = FileSpec.builder(rxHttpPackage, "RxHttp")

        val rxHttpName =
            ClassName(rxHttpPackage, RXHttp_CLASS_NAME).parameterizedBy(wildcard, wildcard)
        fileBuilder.addFunction(
            FunSpec.builder("executeList")
                .addModifiers(KModifier.INLINE)
                .receiver(rxHttpName)
                .addTypeVariable(t.copy(reified = true))
                .addStatement("return executeClass<List<T>>()")
                .build()
        )

        fileBuilder.addFunction(
            FunSpec.builder("executeClass")
                .addModifiers(KModifier.INLINE)
                .receiver(rxHttpName)
                .addTypeVariable(t.copy(reified = true))
                .addStatement("return execute(object : %T<T>() {})", simpleParserName)
                .build())

        if (isDependenceRxJava()) {
            fileBuilder.addFunction(FunSpec.builder("asList")
                .addModifiers(KModifier.INLINE)
                .receiver(baseRxHttpName)
                .addTypeVariable(t.copy(reified = true))
                .addStatement("return asClass<List<T>>()")
                .build())

            fileBuilder.addFunction(FunSpec.builder("asMap")
                .addModifiers(KModifier.INLINE)
                .receiver(baseRxHttpName)
                .addTypeVariable(k.copy(reified = true))
                .addTypeVariable(v.copy(reified = true))
                .addStatement("return asClass<Map<K,V>>()")
                .build())

            fileBuilder.addFunction(FunSpec.builder("asClass")
                .addModifiers(KModifier.INLINE)
                .receiver(baseRxHttpName)
                .addTypeVariable(t.copy(reified = true))
                .addStatement("return asParser(object : %T<T>() {})", simpleParserName)
                .build())

            asFunList.forEach {
                fileBuilder.addFunction(it)
            }
        }

        fileBuilder.addFunction(
            FunSpec.builder("upload")
                .addKdoc(
                    """
                    调用此方法监听上传进度                                                    
                    @param coroutine  CoroutineScope对象，用于开启协程回调进度，进度回调所在线程取决于协程所在线程
                    @param progress 进度回调  
                """.trimIndent()
                )
                .receiver(rxHttpBodyParamName)
                .addTypeVariable(pBound)
                .addTypeVariable(rBound)
                .addParameter("coroutine", coroutineScopeName)
                .addParameter("progress", progressLambdaName.copy(suspending = true))
                .addCode(
                    """
                    param.setProgressCallback {
                        coroutine.%T { progress(it) }
                    }
                    @Suppress("UNCHECKED_CAST")
                    return this as R
                    """.trimIndent(), launchName
                )
                .returns(r)
                .build()
        )

        val channelFlow = MemberName("kotlinx.coroutines.flow", "channelFlow")
        val flowClassName = ClassName("kotlinx.coroutines.flow", "Flow")
        val flowMemberName = MemberName("kotlinx.coroutines.flow", "flow")
        val toClass = MemberName("rxhttp", "toClass")
        val buffer = MemberName("kotlinx.coroutines.flow", "buffer")
        val filter = MemberName("kotlinx.coroutines.flow", "filter")
        val map = MemberName("kotlinx.coroutines.flow", "map")
        val progressT = ClassName("rxhttp.wrapper.entity", "ProgressT")
        val bufferOverflow = ClassName("kotlinx.coroutines.channels", "BufferOverflow")
        val experimentalCoroutinesApi = ClassName("kotlinx.coroutines", "ExperimentalCoroutinesApi")
        val progressParam = ParameterSpec.builder(
            "progress",
            progressLambdaName.copy(suspending = true),
            KModifier.CROSSINLINE
        ).build()
        fileBuilder.addFunction(
            FunSpec.builder("toFlow")
                .addAnnotation(experimentalCoroutinesApi)
                .addModifiers(KModifier.INLINE)
                .receiver(rxHttpBodyWildcardParamName)
                .addTypeVariable(tAny.copy(reified = true))
                .addParameter(progressParam)
                .addStatement(
                    """
                    return 
                      %M {                                                      
                          getParam().setProgressCallback { trySend(%T<T>(it)) }           
                          %M<T>().await().also { trySend(ProgressT<T>(it)) }           
                      }                                                                     
                          .%M(1, %T.DROP_OLDEST)                            
                          .%M { 
                              if (it.result == null)
                                  progress(it)
                              it.result != null
                           }    
                          .%M { it.result }                                                
                """.trimIndent(),
                    channelFlow, progressT, toClass, buffer, bufferOverflow, filter, map
                )
                .returns(flowClassName.parameterizedBy(t))
                .build()
        )


        fileBuilder.addFunction(
            FunSpec.builder("toFlow")
                .addModifiers(KModifier.INLINE)
                .receiver(iRxHttpName)
                .addTypeVariable(tAny.copy(reified = true))
                .addStatement(
                    """
                    return %M<T> { emit(toClass<T>().await()) }                                              
                """.trimIndent(), flowMemberName
                )
                .build()
        )

        toFunList.forEach {
            fileBuilder.addFunction(it)
            val parseName = it.name.substring(2)
            val parameters = it.parameters
            val arguments = StringBuilder()
            parameters.forEach { p ->
                arguments.append(p.name).append(",")
            }
            if (arguments.isNotEmpty()) arguments.deleteCharAt(arguments.length - 1)
            fileBuilder.addFunction(
                FunSpec.builder("toFlow$parseName")
                    .addModifiers(KModifier.INLINE)
                    .receiver(iRxHttpName)
                    .addParameters(it.parameters)
                    .addTypeVariable(tAny.copy(reified = true))
                    .addStatement(
                        """
                    return 
                      %M { emit(to$parseName<T>($arguments).await()) }                                              
                """.trimIndent(), flowMemberName
                    ).build()
            )

            fileBuilder.addFunction(
                FunSpec.builder("toFlow$parseName")
                    .addAnnotation(experimentalCoroutinesApi)
                    .addModifiers(KModifier.INLINE)
                    .receiver(rxHttpBodyWildcardParamName)
                    .addParameters(it.parameters)
                    .addTypeVariable(tAny.copy(reified = true))
                    .addParameter(progressParam)
                    .addStatement(
                        """
                    return 
                      %M {                                                      
                          getParam().setProgressCallback { trySend(%T<T>(it)) }           
                          to$parseName<T>($arguments).await().also { trySend(ProgressT<T>(it)) }           
                      }                                                                     
                          .%M(1, %T.DROP_OLDEST)                            
                          .%M { 
                              if (it.result == null)
                                  progress(it)
                              it.result != null
                           }    
                          .%M { it.result }                                                
                """.trimIndent(),
                        channelFlow, progressT, buffer, bufferOverflow, filter, map
                    )
                    .returns(flowClassName.parameterizedBy(t))
                    .build()
            )
        }
        fileBuilder.build().writeTo(filer)
    }


    //获取构造方法
    private fun getConstructorFun(typeElement: TypeElement): MutableList<ExecutableElement> {
        val funList = ArrayList<ExecutableElement>()
        typeElement.enclosedElements.forEach {
            if (it is ExecutableElement
                && it.kind == ElementKind.CONSTRUCTOR
                && (it.getModifiers().contains(Modifier.PUBLIC) || it.getModifiers().contains(Modifier.PROTECTED))
            ) {
                funList.add(it)
            }
        }
        return funList
    }

    private fun getParamsName(parameterSpecs: MutableList<ParameterSpec>): String {
        val paramsName = StringBuilder()
        parameterSpecs.forEachIndexed { index, parameterSpec ->
            if (index > 0) paramsName.append(", ")
            if (KModifier.VARARG in parameterSpec.modifiers) paramsName.append("*")
            paramsName.append(parameterSpec.name)
        }
        return paramsName.toString()
    }

    //获取泛型字符串 比如:<T> 、<K,V>等等
    private fun getTypeVariableString(typeVariableNames: ArrayList<TypeVariableName>): String {
        val type = StringBuilder()
        val size = typeVariableNames.size
        for (i in typeVariableNames.indices) {
            if (i == 0) type.append("<")
            type.append(typeVariableNames[i].name)
            type.append(if (i < size - 1) "," else ">")
        }
        return type.toString()
    }

    //获取泛型对象列表
    private fun getTypeVariableNames(typeVariableNames: ArrayList<TypeVariableName>): ArrayList<TypeVariableName> {
        val newTypeVariableNames = ArrayList<TypeVariableName>()
        typeVariableNames.forEach {
            val bounds = it.bounds //泛型边界
            val typeVariableName =
                if (bounds.isEmpty() || (bounds.size == 1 && bounds[0].toString() == "java.lang.Object")) {
                    TypeVariableName(it.name, anyTypeName).copy(reified = true)
                } else {
                    (it.toKClassTypeName() as TypeVariableName).copy(reified = true)
                }
            newTypeVariableNames.add(typeVariableName)
        }
        return newTypeVariableNames
    }
}