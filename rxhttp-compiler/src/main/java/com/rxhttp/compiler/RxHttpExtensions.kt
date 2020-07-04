package com.rxhttp.compiler

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.annotation.processing.Filer
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * User: ljx
 * Date: 2020/3/9
 * Time: 17:04
 */
class RxHttpExtensions {

    private val classTypeName = Class::class.asClassName()
    private val anyTypeName = Any::class.asTypeName()

    private val baseRxHttpName = ClassName(rxHttpPackage, "BaseRxHttp")
    private val toFunList = ArrayList<FunSpec>()
    private val asFunList = ArrayList<FunSpec>()

    //根据@Parser注解，生成asXxx()、awaitXxx()类型方法
    fun generateRxHttpExtendFun(typeElement: TypeElement, key: String) {

        val typeVariableNames = ArrayList<TypeVariableName>()
        //遍历获取泛型类型
        typeElement.typeParameters.forEach {
            typeVariableNames.add(it.asTypeVariableName())
        }

        for (executableElement in getConstructorFun(typeElement)) {

            if (executableElement.parameters.size == typeVariableNames.size
                && executableElement.modifiers.contains(Modifier.PUBLIC)
            ) {
                var allTypeArg = true
                for (variableElement in executableElement.parameters) {
                    if (variableElement.asType().toString() != "java.lang.reflect.Type") {
                        allTypeArg = false
                        break
                    }
                }
                if (allTypeArg) continue
            }

            //根据构造方法参数，获取asXxx方法需要的参数
            val parameterList = ArrayList<ParameterSpec>()
            var typeIndex = 0
            executableElement.parameters.forEach {
                if (it.asType().toString() == "java.lang.reflect.Type"
                    && typeIndex < typeVariableNames.size
                ) {
                    //Type类型参数转Class<T>类型
                    val parameterSpec = ParameterSpec.builder(
                        it.simpleName.toString(),
                        classTypeName.parameterizedBy(typeVariableNames[typeIndex++])
                    ).build()
                    parameterList.add(parameterSpec)
                } else {
                    parameterList.add(ParameterSpec.get(it))
                }
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

            asFunList.add(
                FunSpec.builder("as$key")
                    .addModifiers(modifiers)
                    .receiver(baseRxHttpName)
                    .addParameters(parameterList)
                    .addStatement(funBody, typeElement.asClassName()) //方法里面的表达式
                    .addTypeVariables(getTypeVariableNames(typeVariableNames))
                    .build())

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

        val launchName = ClassName("kotlinx.coroutines", "launch")
        val progressName = ClassName("rxhttp.wrapper.entity", "Progress")
        val simpleParserName = ClassName("rxhttp.wrapper.parse", "SimpleParser")
        val coroutineScopeName = ClassName("kotlinx.coroutines", "CoroutineScope")
        val progressCallbackName = ClassName("rxhttp.wrapper.callback", "ProgressCallback")
        val rxhttpFormParam = ClassName(rxHttpPackage, "RxHttpFormParam");

        val progressLambdaName: LambdaTypeName = LambdaTypeName.get(parameters = *arrayOf(progressName),
            returnType = Unit::class.asClassName())

        val fileBuilder = FileSpec.builder(rxHttpPackage, "RxHttp")
        if (isDependenceRxJava()) {
            val schedulerName = getKClassName("Scheduler")
            val observableName = getKClassName("Observable")
            val consumerName = getKClassName("Consumer")
            val observableTName = observableName.parameterizedBy(t)
            val observeOnScheduler = ParameterSpec.builder("observeOnScheduler", schedulerName.copy(nullable = true))
                .defaultValue("null")
                .build()

            fileBuilder.addImport("kotlinx.coroutines", "suspendCancellableCoroutine")
            fileBuilder.addImport("kotlin.coroutines", "resume", "resumeWithException")
            fileBuilder.addFunction(FunSpec.builder("await")
                .addModifiers(KModifier.SUSPEND)
                .receiver(observableTName)
                .addTypeVariable(t)
                .addStatement("""
                return suspendCancellableCoroutine { continuation ->
                    val subscribe = subscribe({                      
                        continuation.resume(it)                     
                    }, {                                             
                        continuation.resumeWithException(it)        
                    })                                              
                                                                    
                    continuation.invokeOnCancellation {              
                        subscribe.dispose()                         
                    }                                               
                }                                                   
            """.trimIndent())
                .returns(t)
                .build())

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
                .addKdoc("""
                    调用此方法监听上传进度                                                    
                    @param coroutine  CoroutineScope对象，用于开启协程回调进度，进度回调所在线程取决于协程所在线程
                    @param progress 进度回调  
                    注意：此方法仅在协程环境下才生效                                         
                """.trimIndent())
                .receiver(rxhttpFormParam)
                .addParameter("coroutine", coroutineScopeName)
                .addParameter("progress", progressLambdaName.copy(suspending = true))
                .addCode("""
                    param.setProgressCallback(%T { currentProgress, currentSize, totalSize ->
                        val p = Progress(currentProgress, currentSize, totalSize)
                        coroutine.%T { progress(p) }
                    })
                    return this
                    """.trimIndent(), progressCallbackName, launchName)
                .returns(rxhttpFormParam)
                .build())

        toFunList.forEach {
            fileBuilder.addFunction(it)
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

    private fun getParamsName(variableElements: MutableList<ParameterSpec>): String {
        val paramsName = StringBuilder()
        for ((index, element) in variableElements.withIndex()) {
            if (index > 0) paramsName.append(", ")
            paramsName.append(element.name)
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
        return newTypeVariableNames;
    }
}