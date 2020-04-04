package com.rxhttp.compiler

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.annotation.processing.Filer
import javax.lang.model.element.TypeElement

/**
 * User: ljx
 * Date: 2020/3/9
 * Time: 17:04
 */
class RxHttpExtensions {

    private val classTypeName = Class::class.asClassName()
    private val anyTypeName = Any::class.asTypeName()

    private val awaitFunList = ArrayList<FunSpec>()
    private val asFunList = ArrayList<FunSpec>()

    //根据@Parser注解，生成asXxx()、awaitXxx()类型方法
    fun generateAsClassFun(typeElement: TypeElement, key: String) {
        val typeVariableNames = ArrayList<TypeVariableName>()
        val parameterSpecs = ArrayList<ParameterSpec>()

        typeElement.typeParameters.forEach {
            val typeVariableName = it.asTypeVariableName()
            typeVariableNames.add(typeVariableName)
            val parameterSpec = ParameterSpec.builder(
                it.asType().toString().toLowerCase() + "Type",
                classTypeName.parameterizedBy(typeVariableName)).build()
            parameterSpecs.add(parameterSpec)
        }

        //自定义解析器对应的asXxx方法里面的语句
        if (typeVariableNames.size > 0) {  //自定义的解析器泛型数量
            asFunList.add(
                FunSpec.builder("as$key")
                    .addModifiers(KModifier.INLINE)
                    .receiver(ClassName("rxhttp", "BaseRxHttp"))
                    .addStatement("return asParser(object: %T${getTypeVariableString(typeVariableNames)}() {})",
                        typeElement.asClassName()) //方法里面的表达式
                    .addTypeVariables(getTypeVariableNames(typeVariableNames))
                    .build())

            //自定义解析器对应的awaitXxx方法里面的语句
            val awaitName = ClassName("rxhttp", "await")
            awaitFunList.add(
                FunSpec.builder("await$key")
                    .addModifiers(KModifier.SUSPEND, KModifier.INLINE)
                    .receiver(ClassName("rxhttp", "IRxHttp"))
                    .addStatement("return %T(object: %T${getTypeVariableString(typeVariableNames)}() {})",
                        awaitName, typeElement.asClassName())  //方法里面的表达式
                    .addTypeVariables(getTypeVariableNames(typeVariableNames))
                    .build())
        } else {
            val awaitName = ClassName("rxhttp", "await")
            awaitFunList.add(
                FunSpec.builder("await$key")
                    .addModifiers(KModifier.SUSPEND)
                    .receiver(ClassName("rxhttp", "IRxHttp"))
                    .addStatement("return %T(%T())", awaitName, typeElement.asClassName())  //方法里面的表达式
                    .build())
        }

    }


    fun generateClassFile(filer: Filer) {
        val builder = FileSpec.builder("rxhttp.wrapper.param", "KotlinExtensions")
        asFunList.forEach {
            builder.addFunction(it)
        }

        awaitFunList.forEach {
            builder.addFunction(it)
        }

        val t = TypeVariableName("T")
        val schedulerName = ClassName("io.reactivex", "Scheduler")
        val progressName = ClassName("rxhttp.wrapper.entity", "Progress")
        val observableName = ClassName("io.reactivex", "Observable")
        val observableTName = observableName.parameterizedBy(t)
        val parserName = ClassName("rxhttp.wrapper.parse", "Parser")
        val simpleParserName = ClassName("rxhttp.wrapper.parse", "SimpleParser")
        val parserTName = parserName.parameterizedBy(t)
        val anyT = TypeVariableName("T", anyTypeName)
        val parser = ParameterSpec.builder("parser", parserTName).build()

        val coroutineScopeName = ClassName("kotlinx.coroutines", "CoroutineScope").copy(nullable = true)
        val coroutine = ParameterSpec.builder("coroutine", coroutineScopeName)
            .defaultValue("null")
            .build()
        val progressCallbackName = ClassName("rxhttp.wrapper.callback", "ProgressCallback")
        val awaitName = ClassName("rxhttp", "await")
        val launchName = ClassName("kotlinx.coroutines", "launch")
        val rxhttpFormParam = ClassName("rxhttp.wrapper.param", "RxHttpFormParam");

        val observeOnScheduler = ParameterSpec.builder("observeOnScheduler", schedulerName.copy(nullable = true))
            .defaultValue("null")
            .build()

        val progressLambdaName = LambdaTypeName.get(parameters = *arrayOf(progressName),
            returnType = Unit::class.asClassName())

        val consumerName = ClassName("io.reactivex.functions", "Consumer")

        builder.addFunction(
            FunSpec.builder("upload")
                .addKdoc("""
                    调用此方法监听上传进度                                                    
                    @param coroutine  CoroutineScope对象，用于开启协程回调进度，进度回调所在线程取决于协程所在线程
                    @param progress 进度回调  
                    注意：此方法仅在协程环境下才生效                                         
                """.trimIndent())
                .receiver(rxhttpFormParam)
                .addParameter(coroutine)
                .addParameter("progress", progressLambdaName)
                .addCode("""
                    return param.setProgressCallback(%T { currentProgress, currentSize, totalSize ->
                        val p = Progress(currentProgress, currentSize, totalSize)
                        coroutine?.%T { progress(p) } ?: progress(p)
                    })
                    """.trimIndent(), progressCallbackName, launchName)
                .build())

        builder.addFunction(
            FunSpec.builder("upload")
                .addKdoc("""
                    调用此方法监听上传进度                                                    
                    @param observeOnScheduler  用于控制下游回调所在线程(包括进度回调)
                    @param progress 进度回调                                      
                """.trimIndent())
                .receiver(rxhttpFormParam)
                .addParameter(observeOnScheduler)
                .addParameter("progress", progressLambdaName)
                .addStatement("return upload(%T{ progress(it) }, observeOnScheduler)", consumerName)
                .build())

        val deprecatedAnno = AnnotationSpec.builder(Deprecated::class.java)
            .addMember("\"Will be removed in a future release\"").build()

        builder.addFunction(
            FunSpec.builder("asUpload")
                .addKdoc("please use [upload] + asXxx method instead")
                .addAnnotation(deprecatedAnno)
                .receiver(rxhttpFormParam)
                .addModifiers(KModifier.INLINE)
                .addTypeVariable(anyT.copy(reified = true))
                .addParameter(observeOnScheduler)
                .addParameter("progress", progressLambdaName, KModifier.NOINLINE)
                .addStatement("return asUpload(object: %T<T>() {}, observeOnScheduler, progress)", simpleParserName)
                .build())

        builder.addFunction(
            FunSpec.builder("asUpload")
                .addKdoc("please use [upload] + asXxx method instead")
                .addAnnotation(deprecatedAnno)
                .receiver(rxhttpFormParam)
                .addTypeVariable(anyT)
                .addParameter(parser)
                .addParameter(observeOnScheduler)
                .addParameter("progress", progressLambdaName)
                .addStatement("return asUpload(parser, %T{ progress(it) }, observeOnScheduler)", consumerName)
                .returns(observableTName)
                .build())

        builder.addFunction(
            FunSpec.builder("awaitUpload")
                .addKdoc("please use [upload] + awaitXxx method instead")
                .addAnnotation(deprecatedAnno)
                .receiver(rxhttpFormParam)
                .addModifiers(KModifier.SUSPEND, KModifier.INLINE)
                .addTypeVariable(anyT.copy(reified = true))
                .addParameter(coroutine)
                .addParameter("progress", progressLambdaName, KModifier.NOINLINE)
                .addStatement("return awaitUpload(object: SimpleParser<T>() {}, coroutine, progress)")
                .returns(t)
                .build())

        builder.addFunction(
            FunSpec.builder("awaitUpload")
                .addKdoc("please use [upload] + awaitXxx method instead")
                .addAnnotation(deprecatedAnno)
                .receiver(rxhttpFormParam)
                .addModifiers(KModifier.SUSPEND)
                .addTypeVariable(anyT)
                .addParameter(parser)
                .addParameter(coroutine)
                .addParameter("progress", progressLambdaName)
                .addCode("""
                    upload(coroutine, progress)
                    return %T(parser)
                    """.trimIndent(), awaitName)
                .returns(t)
                .build())

        builder.build().writeTo(filer)
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