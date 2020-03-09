package com.rxhttp.compiler

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.util.*
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
        //自定义解析器对应的asXxx方法里面的语句
        var statementBuilder = StringBuilder("return asParser(object: %T") //方法里面的表达式
        if (typeVariableNames.size > 0) { //添加泛型
            statementBuilder.append("<")
            var i = 0
            val size = typeVariableNames.size
            while (i < size) {
                val variableName = typeVariableNames[i]
                statementBuilder.append(variableName.name)
                    .append(if (i == size - 1) ">" else ",")
                i++
            }
        }
        statementBuilder.append("() {})")
        var funBuilder = FunSpec.builder("as$key")
            .addModifiers(KModifier.INLINE)
            .receiver(ClassName("rxhttp", "BaseRxHttp"))
            .addStatement(statementBuilder.toString(), typeElement.asClassName())

        typeVariableNames.forEach {
            if (it.bounds.isEmpty()
                || (it.bounds.size == 1 && it.bounds[0].toString() == "java.lang.Object")) {
                funBuilder.addTypeVariable(TypeVariableName(it.name, anyTypeName).copy(reified = true))
            } else {
                funBuilder.addTypeVariable((it.toKClassTypeName() as TypeVariableName).copy(reified = true))
            }
        }
        asFunList.add(funBuilder.build())
        val awaitName = ClassName("rxhttp", "await")

        //自定义解析器对应的awaitXxx方法里面的语句
        statementBuilder = StringBuilder("return %T(object: %T") //方法里面的表达式
        if (typeVariableNames.size > 0) { //添加泛型
            statementBuilder.append("<")
            var i = 0
            val size = typeVariableNames.size
            while (i < size) {
                val variableName = typeVariableNames[i]
                statementBuilder.append(variableName.name)
                    .append(if (i == size - 1) ">" else ",")
                i++
            }
        }

        statementBuilder.append("() {})")
        funBuilder = FunSpec.builder("await$key")
            .addModifiers(KModifier.SUSPEND, KModifier.INLINE)
            .receiver(ClassName("rxhttp", "BaseRxHttp"))
            .addStatement(statementBuilder.toString(), awaitName,typeElement.asClassName())

        typeVariableNames.forEach {
            if (it.bounds.isEmpty()
                || (it.bounds.size == 1 && it.bounds[0].toString() == "java.lang.Object")) {
                funBuilder.addTypeVariable(TypeVariableName(it.name, anyTypeName).copy(reified = true))
            } else {
                funBuilder.addTypeVariable((it.toKClassTypeName() as TypeVariableName).copy(reified = true))
            }
        }
        awaitFunList.add(funBuilder.build())
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
        val progressTName = progressName.parameterizedBy(t)
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

        val progressTLambdaName = LambdaTypeName.get(parameters = *arrayOf(progressTName),
            returnType = Unit::class.asClassName())

        val consumerName = ClassName("io.reactivex.functions", "Consumer")

        builder.addFunction(
            FunSpec.builder("awaitUpload")
                .receiver(rxhttpFormParam)
                .addModifiers(KModifier.SUSPEND, KModifier.INLINE)
                .addTypeVariable(anyT.copy(reified = true))
                .addParameter(coroutine)
                .addParameter("progress", progressTLambdaName, KModifier.NOINLINE)
                .addStatement("return awaitUpload(object: SimpleParser<T>() {}, coroutine, progress)")
                .returns(t)
                .build())

        builder.addFunction(
            FunSpec.builder("awaitUpload")
                .receiver(rxhttpFormParam)
                .addModifiers(KModifier.SUSPEND)
                .addTypeVariable(anyT)
                .addParameter(parser)
                .addParameter(coroutine)
                .addParameter("progress", progressTLambdaName)
                .addCode("""
                    param.setProgressCallback(%T { currentProgress, currentSize, totalSize ->
                        val p = Progress<T>(currentProgress, currentSize, totalSize)
                        coroutine?.%T { progress(p) } ?: progress(p)
                    })
                    return %T(parser)
                    """.trimIndent(), progressCallbackName, launchName, awaitName)
                .returns(t)
                .build())

        builder.addFunction(
            FunSpec.builder("asUpload")
                .receiver(rxhttpFormParam)
                .addModifiers(KModifier.INLINE)
                .addTypeVariable(anyT.copy(reified = true))
                .addParameter(observeOnScheduler)
                .addParameter("progress", progressTLambdaName, KModifier.NOINLINE)
                .addStatement("return asUpload(object: %T<T>() {}, %T{ progress(it) }, observeOnScheduler)", simpleParserName, consumerName)
                .build())

        builder.addFunction(
            FunSpec.builder("asUpload")
                .receiver(rxhttpFormParam)
                .addTypeVariable(anyT)
                .addParameter(parser)
                .addParameter(observeOnScheduler)
                .addParameter("progress", progressTLambdaName)
                .addStatement("return asUpload(parser, %T{ progress(it) }, observeOnScheduler)", consumerName)
                .returns(observableTName)
                .build())

        builder.build().writeTo(filer)
    }
}