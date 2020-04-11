package com.rxhttp.compiler

import com.squareup.javapoet.*
import rxhttp.wrapper.annotation.Parser
import java.io.IOException
import java.util.*
import javax.annotation.processing.Filer
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypesException
import javax.lang.model.type.TypeMirror

class ParserAnnotatedClass {

    private val mElementMap: MutableMap<String, TypeElement> = LinkedHashMap()
    private val mTypeMap: MutableMap<String, List<TypeMirror>> = LinkedHashMap()
    private val schedulerName = ClassName.get("io.reactivex", "Scheduler")
    private val observableName = ClassName.get("io.reactivex", "Observable")
    private val consumerName = ClassName.get("io.reactivex.functions", "Consumer")

    fun add(typeElement: TypeElement) {
        val annotation = typeElement.getAnnotation(Parser::class.java)
        val name: String = annotation.name
        require(name.isNotEmpty()) {
            String.format("methodName() in @%s for class %s is null or empty! that's not allowed",
                Parser::class.java.simpleName, typeElement.qualifiedName.toString())
        }
        try {
            annotation.wrappers
        } catch (e: MirroredTypesException) {
            val typeMirrors = e.typeMirrors
            mTypeMap[name] = typeMirrors
        }
        mElementMap[name] = typeElement
    }

    fun getMethodList(filer: Filer): List<MethodSpec> {
        val t = TypeVariableName.get("T")
        val k = TypeVariableName.get("K")
        val v = TypeVariableName.get("V")
        val callName = ClassName.get("okhttp3", "Call")
        val okHttpClientName = ClassName.get("okhttp3", "OkHttpClient")
        val responseName = ClassName.get("okhttp3", "Response")
        val httpSenderName = ClassName.get("rxhttp", "HttpSender")
        val requestName = ClassName.get("okhttp3", "Request")
        val parserName = ClassName.get("rxhttp.wrapper.parse", "Parser")
        val progressName = ClassName.get("rxhttp.wrapper.entity", "Progress")
        val progressTName = ClassName.get("rxhttp.wrapper.entity", "ProgressT")
        val typeName = TypeName.get(String::class.java)
        val progressTStringName: TypeName = ParameterizedTypeName.get(progressTName, typeName)
        val observableTName: TypeName = ParameterizedTypeName.get(observableName, t)
        val observableStringName: TypeName = ParameterizedTypeName.get(observableName, typeName)
        val consumerProgressName: TypeName = ParameterizedTypeName.get(consumerName, progressName)
        val parserTName: TypeName = ParameterizedTypeName.get(parserName, t)
        val methodList: MutableList<MethodSpec> = ArrayList()
        var method: MethodSpec.Builder

        methodList.add(
            MethodSpec.methodBuilder("execute")
                .addModifiers(Modifier.PUBLIC)
                .addException(IOException::class.java)
                .addStatement("doOnStart()")
                .addStatement("return \$T.execute(param)", httpSenderName)
                .returns(responseName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("execute")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addException(IOException::class.java)
                .addParameter(parserTName, "parser")
                .addStatement("return parser.onParse(execute())", httpSenderName)
                .returns(t)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("newCall")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return newCall(getOkHttpClient())")
                .returns(callName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("newCall")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(okHttpClientName, "okHttp")
                .addStatement("return \$T.newCall(okHttp, buildRequest())", httpSenderName)
                .returns(callName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("buildRequest")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("doOnStart()")
                .addStatement("return param.buildRequest()")
                .returns(requestName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("doOnStart")
                .addJavadoc("请求开始前内部调用，用于添加默认域名等操作\n")
                .addStatement("setConverter(param)")
                .addStatement("addDefaultDomainIfAbsent(param)")
                .build())


        methodList.add(
            MethodSpec.methodBuilder("subscribeOn")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(schedulerName, "scheduler")
                .addStatement("this.scheduler=scheduler")
                .addStatement("return (R)this")
                .returns(RxHttpGenerator.r)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("subscribeOnCurrent")
                .addJavadoc("设置在当前线程发请求\n")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.scheduler=null")
                .addStatement("return (R)this")
                .returns(RxHttpGenerator.r)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("subscribeOnIo")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.scheduler=Schedulers.io()")
                .addStatement("return (R)this")
                .returns(RxHttpGenerator.r)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("subscribeOnComputation")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.scheduler=Schedulers.computation()")
                .addStatement("return (R)this")
                .returns(RxHttpGenerator.r)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("subscribeOnNewThread")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.scheduler=Schedulers.newThread()")
                .addStatement("return (R)this")
                .returns(RxHttpGenerator.r)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("subscribeOnSingle")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.scheduler=Schedulers.single()")
                .addStatement("return (R)this")
                .returns(RxHttpGenerator.r)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("subscribeOnTrampoline")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this.scheduler=Schedulers.trampoline()")
                .addStatement("return (R)this")
                .returns(RxHttpGenerator.r)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("asParser")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(parserTName, "parser")
                .addStatement("""
                        doOnStart();
                    Observable<T> observable = new ObservableHttp<T>(param, parser);
                    if (scheduler != null) {
                        observable = observable.subscribeOn(scheduler);
                    }
                    return observable
                """.trimIndent())
                .returns(observableTName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("asDownload")
                .addAnnotation(Override::class.java)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "destPath")
                .addParameter(consumerProgressName, "progressConsumer")
                .addParameter(schedulerName, "observeOnScheduler")
                .addStatement("""
                        doOnStart();
                    Observable<Progress> observable = new ObservableDownload(param, destPath, breakDownloadOffSize);
                    if (scheduler != null)
                        observable = observable.subscribeOn(scheduler);
                    if (observeOnScheduler != null) {
                        observable = observable.observeOn(observeOnScheduler);
                    }
                    return observable.doOnNext(progressConsumer)
                        .filter(progress -> progress instanceof ProgressT)
                        .map(progress -> ((${"$"}T) progress).getResult())
                """.trimIndent(), progressTStringName)
                .returns(observableStringName)
                .build())

        val rxHttpExtensions = RxHttpExtensions()

        //获取自定义的解析器
        for ((parserAlias, typeElement) in mElementMap) {
            var returnType: TypeMirror? = null //获取onParse方法的返回类型
            for (element in typeElement.enclosedElements) {
                if (element !is ExecutableElement
                    || !element.getModifiers().contains(Modifier.PUBLIC)
                    || element.getModifiers().contains(Modifier.STATIC)) continue
                if (element.simpleName.toString() == "onParse"
                    && element.parameters.size == 1
                    && element.parameters[0].asType().toString() == "okhttp3.Response") {
                    returnType = element.returnType
                    break
                }
            }
            if (returnType == null) continue
            rxHttpExtensions.generateAsClassFun(typeElement, parserAlias)
            methodList.add(generateAsXxxMethod(typeElement, parserAlias, returnType, null))
            val typeMirrors = mTypeMap[parserAlias]!!
            for (mirror in typeMirrors) {  //遍历Parser注解里面的wrappers数组
                val name = mirror.toString()
                val simpleName = name.substring(name.lastIndexOf(".") + 1)
                val methodName = parserAlias + simpleName
                methodList.add(generateAsXxxMethod(typeElement, methodName, returnType, mirror))
            }
        }
        rxHttpExtensions.generateClassFile(filer)
        return methodList
    }

    private fun generateAsXxxMethod(
        typeElement: TypeElement,
        methodName: String,
        returnTypeMirror: TypeMirror,
        mirror: TypeMirror?
    ): MethodSpec {
        val typeVariableNames = ArrayList<TypeVariableName>()
        val parameterSpecs = ArrayList<ParameterSpec>()
        val typeParameters = typeElement.typeParameters
        for (element in typeParameters) {
            val typeVariableName = TypeVariableName.get(element)
            typeVariableNames.add(typeVariableName)
            val parameterSpec = ParameterSpec.builder(
                ParameterizedTypeName.get(ClassName.get(Class::class.java), typeVariableName),
                element.asType().toString().toLowerCase() + "Type").build()
            parameterSpecs.add(parameterSpec)
        }

        //自定义解析器对应的asXxx方法里面的语句
        val statementBuilder = StringBuilder("return asParser(new \$T")
        var size = typeVariableNames.size
        if (size > 0) statementBuilder.append("<")
        for (i in 0 until size) { //添加泛型
            if (mirror != null) {
                val name = mirror.toString()
                val simpleName = name.substring(name.lastIndexOf('.') + 1)
                statementBuilder.append(simpleName).append("<")
            }
            val variableName = typeVariableNames[i]
            statementBuilder.append(variableName.name)
            if (mirror != null) {
                statementBuilder.append(">")
            }
            statementBuilder.append(if (i == size - 1) ">" else ",")
        }
        if (mirror != null) {
            val name = mirror.toString()
            val simpleName = name.substring(name.lastIndexOf('.') + 1)
            statementBuilder.append("(")
            for (spec in parameterSpecs) {
                statementBuilder.append(spec.name).append(simpleName)
                    .append(",")
            }
            statementBuilder.deleteCharAt(statementBuilder.length - 1).append(")")
        } else {
            statementBuilder.append("(")
            size = parameterSpecs.size
            for (i in 0 until size) { //添加参数
                val parameterSpec = parameterSpecs[i]
                statementBuilder.append(parameterSpec.name)
                if (i < size - 1) {
                    statementBuilder.append(",")
                }
            }
            statementBuilder.append(")")
        }
        statementBuilder.append(")")
        var typeName = TypeName.get(returnTypeMirror)
        if (mirror != null) {
            val className = ClassName.bestGuess(mirror.toString())
            typeName = if (typeName is ParameterizedTypeName) {
                val parameterizedTypeName = typeName
                val typeNames: MutableList<TypeName> = ArrayList()
                for (type in parameterizedTypeName.typeArguments) {
                    val parameterizedReturnType: TypeName = ParameterizedTypeName
                        .get(className, type)
                    typeNames.add(parameterizedReturnType)
                }
                ParameterizedTypeName.get(parameterizedTypeName.rawType,
                    *typeNames.toTypedArray())
            } else {
                ParameterizedTypeName.get(className, typeName)
            }
        }
        val returnType: TypeName = ParameterizedTypeName.get(observableName, typeName)
        val builder = MethodSpec.methodBuilder("as$methodName")
            .addModifiers(Modifier.PUBLIC)
        if (mirror != null) {
            val parameterizedType = ClassName.get("rxhttp.wrapper.entity", "ParameterizedTypeImpl")
            val type = ClassName.get("java.lang.reflect", "Type")
            for (spec in parameterSpecs) {
                val expression = "\$T " + spec.name + "\$T = \$T.get(\$T.class, " + spec.name + ")"
                builder.addStatement(expression, type, TypeName.get(mirror), parameterizedType, TypeName.get(mirror))
            }
        }
        builder.addTypeVariables(typeVariableNames) //添加泛型
            .addParameters(parameterSpecs) //添加参数
            .addStatement(statementBuilder.toString(), ClassName.get(typeElement)) //添加表达式
            .returns(returnType) //设置返回值
        return builder.build()
    }
}