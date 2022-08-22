package com.rxhttp.compiler.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFile
import com.rxhttp.compiler.RXHttp
import com.rxhttp.compiler.getKClassName
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage
import com.rxhttp.compiler.rxhttpKClassName
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.jvm.throws
import com.squareup.kotlinpoet.ksp.writeTo
import java.io.IOException

class RxHttpGenerator(
    private val logger: KSPLogger,
    private val ksFiles: Collection<KSFile>
) {

    var paramsVisitor: ParamsVisitor? = null
    var parserVisitor: ParserVisitor? = null
    var domainVisitor: DomainVisitor? = null
    var converterVisitor: ConverterVisitor? = null
    var okClientVisitor: OkClientVisitor? = null
    var defaultDomainVisitor: DefaultDomainVisitor? = null

    //生成RxHttp类
    @KspExperimental
    @Throws(IOException::class)
    fun generateCode(codeGenerator: CodeGenerator) {

        val paramClassName = ClassName("rxhttp.wrapper.param", "Param")
        val typeVariableP = TypeVariableName("P", paramClassName.parameterizedBy("P"))      //泛型P
        val typeVariableR = TypeVariableName("R", rxhttpKClassName.parameterizedBy("P","R")) //泛型R

        val okHttpClientName = ClassName("okhttp3", "OkHttpClient")
        val requestName = ClassName("okhttp3", "Request")
        val headerName = ClassName("okhttp3", "Headers")
        val headerBuilderName = ClassName("okhttp3", "Headers.Builder")
        val cacheControlName = ClassName("okhttp3", "CacheControl")
        val callName = ClassName("okhttp3", "Call")
        val responseName = ClassName("okhttp3", "Response")

        val timeUnitName = ClassName("java.util.concurrent", "TimeUnit")

        val rxHttpPluginsName = ClassName("rxhttp", "RxHttpPlugins")
        val converterName = ClassName("rxhttp.wrapper.callback", "IConverter")
        val cacheInterceptorName = ClassName("rxhttp.wrapper.intercept", "CacheInterceptor")
        val cacheModeName = ClassName("rxhttp.wrapper.cahce", "CacheMode")
        val cacheStrategyName = ClassName("rxhttp.wrapper.cahce", "CacheStrategy")
        val downloadOffSizeName = ClassName("rxhttp.wrapper.entity", "DownloadOffSize")

        val t = TypeVariableName("T")
        val className = Class::class.asClassName()
        val superT = WildcardTypeName.consumerOf(t)
        val classTName = className.parameterizedBy("T")
        val classSuperTName = className.parameterizedBy(superT)

        val wildcard = TypeVariableName("*")
        val listName = LIST.parameterizedBy("*")
        val mapName = MAP.parameterizedBy(STRING, wildcard)
        val mapStringName = MAP.parameterizedBy(STRING, STRING)
        val listTName = LIST.parameterizedBy("T")

        val parserName = ClassName("rxhttp.wrapper.parse", "Parser")
        val progressName = ClassName("rxhttp.wrapper.entity", "Progress")
        val logUtilName = ClassName("rxhttp.wrapper.utils", "LogUtil")
        val logInterceptorName = ClassName("rxhttp.wrapper.intercept", "LogInterceptor")
        val parserTName = parserName.parameterizedBy("T")
        val simpleParserName = ClassName("rxhttp.wrapper.parse", "SimpleParser")
        val parameterizedType = ClassName("rxhttp.wrapper.entity", "ParameterizedTypeImpl")

        val methodList = ArrayList<FunSpec>() //方法集合

        //添加构造方法
        val constructorFun = FunSpec.constructorBuilder()
            .addModifiers(KModifier.PROTECTED)
            .addParameter("param", typeVariableP)
            .build()

        val propertySpecs = mutableListOf<PropertySpec>()

        PropertySpec.builder("connectTimeoutMillis", LONG, KModifier.PRIVATE)
            .initializer("0L")
            .mutable(true)
            .build()
            .let { propertySpecs.add(it) }

        PropertySpec.builder("readTimeoutMillis", LONG, KModifier.PRIVATE)
            .initializer("0L")
            .mutable(true)
            .build()
            .let { propertySpecs.add(it) }

        PropertySpec.builder("writeTimeoutMillis", LONG, KModifier.PRIVATE)
            .initializer("0L")
            .mutable(true)
            .build()
            .let { propertySpecs.add(it) }

        PropertySpec.builder("converter", converterName, KModifier.PRIVATE)
            .mutable(true)
            .initializer("%T.getConverter()", rxHttpPluginsName)
            .build()
            .let { propertySpecs.add(it) }

        PropertySpec.builder("okClient", okHttpClientName, KModifier.PRIVATE)
            .mutable(true)
            .initializer("%T.getOkHttpClient()", rxHttpPluginsName)
            .build()
            .let { propertySpecs.add(it) }

        PropertySpec.builder("isAsync", BOOLEAN, KModifier.PROTECTED)
            .mutable(true)
            .initializer("true")
            .build()
            .let { propertySpecs.add(it) }

        PropertySpec.builder("param", typeVariableP)
            .initializer("param")
            .build()
            .let { propertySpecs.add(it) }

        PropertySpec.builder("request", requestName.copy(true))
            .mutable(true)
            .initializer("null")
            .build()
            .let { propertySpecs.add(it) }

        val getUrlFun = FunSpec.getterBuilder()
            .addStatement("addDefaultDomainIfAbsent()")
            .addStatement("return param.getUrl()")
            .build()

        PropertySpec.builder("url", STRING)
            .addAnnotation(getJvmName("getUrl"))
            .getter(getUrlFun)
            .build()
            .let { propertySpecs.add(it) }

        val simpleUrlFun = FunSpec.getterBuilder()
            .addStatement("return param.getSimpleUrl()")
            .build()

        PropertySpec.builder("simpleUrl", STRING)
            .addAnnotation(getJvmName("getSimpleUrl"))
            .getter(simpleUrlFun)
            .build()
            .let { propertySpecs.add(it) }

        val headersFun = FunSpec.getterBuilder()
            .addStatement("return param.getHeaders()")
            .build()

        PropertySpec.builder("headers", headerName)
            .addAnnotation(getJvmName("getHeaders"))
            .getter(headersFun)
            .build()
            .let { propertySpecs.add(it) }

        val headersBuilderFun = FunSpec.getterBuilder()
            .addStatement("return param.getHeadersBuilder()")
            .build()

        PropertySpec.builder("headersBuilder", headerBuilderName)
            .addAnnotation(getJvmName("getHeadersBuilder"))
            .getter(headersBuilderFun)
            .build()
            .let { propertySpecs.add(it) }

        val cacheStrategyFun = FunSpec.getterBuilder()
            .addStatement("return param.getCacheStrategy()")
            .build()

        PropertySpec.builder("cacheStrategy", cacheStrategyName)
            .addAnnotation(getJvmName("getCacheStrategy"))
            .getter(cacheStrategyFun)
            .build()
            .let { propertySpecs.add(it) }

        val okClientFun = FunSpec.getterBuilder()
            .addCode(
                """
                if (_okHttpClient != null) return _okHttpClient!!
                val okClient = this.okClient
                var builder : OkHttpClient.Builder? = null
                
                if (%T.isDebug()) {
                    val b = builder ?: okClient.newBuilder().also { builder = it }
                    b.addInterceptor(%T(okClient))
                }
                
                if (connectTimeoutMillis != 0L) {
                    val b = builder ?: okClient.newBuilder().also { builder = it }
                    b.connectTimeout(connectTimeoutMillis, %T.MILLISECONDS)
                }
                
                if (readTimeoutMillis != 0L) {
                    val b = builder ?: okClient.newBuilder().also { builder = it }
                    b.readTimeout(readTimeoutMillis, %T.MILLISECONDS)
                }

                if (writeTimeoutMillis != 0L) {
                   val b = builder ?: okClient.newBuilder().also { builder = it }
                   b.writeTimeout(writeTimeoutMillis, %T.MILLISECONDS)
                }
                
                if (param.getCacheMode() != CacheMode.ONLY_NETWORK) {                      
                    val b = builder ?: okClient.newBuilder().also { builder = it }        
                    b.addInterceptor(%T(cacheStrategy))
                }
                                                                                        
                _okHttpClient = builder?.build() ?: okClient
                return _okHttpClient!!
                """.trimIndent(),
                logUtilName,
                logInterceptorName,
                timeUnitName,
                timeUnitName,
                timeUnitName,
                cacheInterceptorName
            )
            .build()


        PropertySpec.builder("_okHttpClient", okHttpClientName.copy(true), KModifier.PRIVATE)
            .mutable(true)
            .initializer("null")
            .build()
            .let { propertySpecs.add(it) }

        PropertySpec.builder("okHttpClient", okHttpClientName)
            .addAnnotation(getJvmName("getOkHttpClient"))
            .getter(okClientFun)
            .build()
            .let { propertySpecs.add(it) }

        FunSpec.builder("connectTimeout")
            .addParameter("connectTimeout", LONG)
            .addStatement("connectTimeoutMillis = connectTimeout")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("readTimeout")
            .addParameter("readTimeout", LONG)
            .addStatement("readTimeoutMillis = readTimeout")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("writeTimeout")
            .addParameter("writeTimeout", LONG)
            .addStatement("writeTimeoutMillis = writeTimeout")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        val methodMap = LinkedHashMap<String, String>()
        methodMap["get"] = "RxHttpNoBodyParam"
        methodMap["head"] = "RxHttpNoBodyParam"
        methodMap["postBody"] = "RxHttpBodyParam"
        methodMap["putBody"] = "RxHttpBodyParam"
        methodMap["patchBody"] = "RxHttpBodyParam"
        methodMap["deleteBody"] = "RxHttpBodyParam"
        methodMap["postForm"] = "RxHttpFormParam"
        methodMap["putForm"] = "RxHttpFormParam"
        methodMap["patchForm"] = "RxHttpFormParam"
        methodMap["deleteForm"] = "RxHttpFormParam"
        methodMap["postJson"] = "RxHttpJsonParam"
        methodMap["putJson"] = "RxHttpJsonParam"
        methodMap["patchJson"] = "RxHttpJsonParam"
        methodMap["deleteJson"] = "RxHttpJsonParam"
        methodMap["postJsonArray"] = "RxHttpJsonArrayParam"
        methodMap["putJsonArray"] = "RxHttpJsonArrayParam"
        methodMap["patchJsonArray"] = "RxHttpJsonArrayParam"
        methodMap["deleteJsonArray"] = "RxHttpJsonArrayParam"

        val codeBlock =
            """
                For example:
                                                         
                ```                                                  
                RxHttp.get("/service/%L/...", 1)  
                    .addQuery("size", 20)
                    ...
                ```
                 url = /service/1/...?size=20
            """.trimIndent()

        val companionBuilder = TypeSpec.companionObjectBuilder()

        for ((key, value) in methodMap) {
            val methodBuilder = FunSpec.builder(key)
            if (key == "get") {
                methodBuilder.addKdoc(codeBlock, "%d")
            }
            methodBuilder.addAnnotation(JvmStatic::class)
                .addParameter("url", STRING)
                .addParameter("formatArgs", ANY, true, KModifier.VARARG)
                .addStatement(
                    "return $value(%T.${key}(format(url, *formatArgs)))", paramClassName,
                )
                .build()
                .let { companionBuilder.addFunction(it) }
        }

        paramsVisitor?.apply {
            companionBuilder.addFunctions(getFunList(codeGenerator))
        }

        FunSpec.builder("format")
            .addKdoc("Returns a formatted string using the specified format string and arguments.")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("url", STRING)
            .addParameter("formatArgs", ANY, true, KModifier.VARARG)
            .addStatement("return if(formatArgs.isNullOrEmpty()) url else String.format(url, *formatArgs)")
            .build()
            .let { companionBuilder.addFunction(it) }

        FunSpec.builder("setUrl")
            .addParameter("url", STRING)
            .addStatement("param.setUrl(url)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("addPath")
            .addKdoc(
                """
                For example:
                                                         
                ```                                                  
                RxHttp.get("/service/{page}/...")  
                    .addPath("page", 1)
                    ...
                ```
                url = /service/1/...
                """.trimIndent()
            )
            .addParameter("name", STRING)
            .addParameter("value", ANY)
            .addStatement("param.addPath(name, value)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("addEncodedPath")
            .addParameter("name", STRING)
            .addParameter("value", ANY)
            .addStatement("param.addEncodedPath(name, value)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("addQuery")
            .addParameter("key", STRING)
            .addStatement("param.addQuery(key, null)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("addEncodedQuery")
            .addParameter("key", STRING)
            .addStatement("param.addEncodedQuery(key, null)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("addQuery")
            .addParameter("key", STRING)
            .addParameter("value", ANY, true)
            .addStatement("param.addQuery(key, value)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("addEncodedQuery")
            .addParameter("key", STRING)
            .addParameter("value", ANY, true)
            .addStatement("param.addEncodedQuery(key, value)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("addAllQuery")
            .addParameter("key", STRING)
            .addParameter("list", listName)
            .addStatement("param.addAllQuery(key, list)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("addAllEncodedQuery")
            .addParameter("key", STRING)
            .addParameter("list", listName)
            .addStatement("param.addAllEncodedQuery(key, list)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("addAllQuery")
            .addParameter("map", mapName)
            .addStatement("param.addAllQuery(map)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("addAllEncodedQuery")
            .addParameter("map", mapName)
            .addStatement("param.addAllEncodedQuery(map)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        val isAddParam = ParameterSpec.builder("isAdd", BOOLEAN)
            .defaultValue("true")
            .build()
        FunSpec.builder("addHeader")
            .addAnnotation(JvmOverloads::class)
            .addParameter("line", STRING)
            .addParameter(isAddParam)
            .addCode(
                """
                if (isAdd) param.addHeader(line)
                return this as R
                """.trimIndent()
            )
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("addNonAsciiHeader")
            .addKdoc("Add a header with the specified name and value. Does validation of header names, allowing non-ASCII values.")
            .addParameter("key", STRING)
            .addParameter("value", STRING)
            .addStatement("param.addNonAsciiHeader(key,value)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("setNonAsciiHeader")
            .addKdoc("Set a header with the specified name and value. Does validation of header names, allowing non-ASCII values.")
            .addParameter("key", STRING)
            .addParameter("value", STRING)
            .addStatement("param.setNonAsciiHeader(key,value)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("addHeader")
            .addAnnotation(JvmOverloads::class)
            .addParameter("key", STRING)
            .addParameter("value", STRING)
            .addParameter(isAddParam)
            .addCode(
                """
                if (isAdd) param.addHeader(key, value)
                return this as R
                """.trimIndent()
            )
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("addAllHeader")
            .addParameter("headers", mapStringName)
            .addStatement("param.addAllHeader(headers)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("addAllHeader")
            .addParameter("headers", headerName)
            .addStatement("param.addAllHeader(headers)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("setHeader")
            .addParameter("key", STRING)
            .addParameter("value", STRING)
            .addStatement("param.setHeader(key,value)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("setAllHeader")
            .addParameter("headers", mapStringName)
            .addStatement("param.setAllHeader(headers)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        val endIndex = ParameterSpec.builder("endIndex", LONG)
            .defaultValue("-1L")
            .build()

        FunSpec.builder("setRangeHeader")
            .addAnnotation(JvmOverloads::class)
            .addParameter("startIndex", LONG)
            .addParameter(endIndex)
            .addStatement("return setRangeHeader(startIndex, endIndex, false)")
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("setRangeHeader")
            .addParameter("startIndex", LONG)
            .addParameter("connectLastProgress", BOOLEAN)
            .addStatement("return setRangeHeader(startIndex, -1, connectLastProgress)")
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("setRangeHeader")
            .addKdoc(
                """
                设置断点下载开始/结束位置
                @param startIndex 断点下载开始位置
                @param endIndex 断点下载结束位置，默认为-1，即默认结束位置为文件末尾
                @param connectLastProgress 是否衔接上次的下载进度，该参数仅在带进度断点下载时生效
                """.trimIndent()
            )
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("startIndex", LONG)
            .addParameter("endIndex", LONG)
            .addParameter("connectLastProgress", BOOLEAN)
            .addCode(
                """
                param.setRangeHeader(startIndex, endIndex)                         
                if (connectLastProgress)                                            
                    param.tag(DownloadOffSize::class.java, %T(startIndex))
                return this as R                                                    
                """.trimIndent(), downloadOffSizeName
            )
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("removeAllHeader")
            .addParameter("key", STRING)
            .addStatement("param.removeAllHeader(key)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("setHeadersBuilder")
            .addParameter("builder", headerBuilderName)
            .addStatement("param.setHeadersBuilder(builder)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("setAssemblyEnabled")
            .addKdoc(
                """
                设置单个接口是否需要添加公共参数,
                即是否回调通过{@link RxHttpPlugins#setOnParamAssembly(Function)}方法设置的接口,默认为true
                """.trimIndent()
            )
            .addParameter("enabled", BOOLEAN)
            .addStatement("param.setAssemblyEnabled(enabled)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("setDecoderEnabled")
            .addKdoc(
                """
                设置单个接口是否需要对Http返回的数据进行解码/解密,
                即是否回调通过{@link RxHttpPlugins#setResultDecoder(Function)}方法设置的接口,默认为true
                """.trimIndent()
            )
            .addParameter("enabled", BOOLEAN)
            .addStatement(
                "param.addHeader(%T.DATA_DECRYPT, enabled.toString())", paramClassName
            )
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("isAssemblyEnabled")
            .addStatement("return param.isAssemblyEnabled()")
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("getHeader")
            .addParameter("key", STRING)
            .addStatement("return param.getHeader(key)")
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("tag")
            .addParameter("tag", ANY)
            .addStatement("param.tag(tag)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("tag")
            .addTypeVariable(t)
            .addParameter("type", classSuperTName)
            .addParameter("tag", t)
            .addStatement("param.tag(type, tag)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("cacheControl")
            .addParameter("cacheControl", cacheControlName)
            .addStatement("param.cacheControl(cacheControl)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("setCacheKey")
            .addParameter("cacheKey", STRING)
            .addStatement("param.setCacheKey(cacheKey)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("setCacheValidTime")
            .addParameter("cacheValidTime", LONG)
            .addStatement("param.setCacheValidTime(cacheValidTime)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("setCacheMode")
            .addParameter("cacheMode", cacheModeName)
            .addStatement("param.setCacheMode(cacheMode)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("execute")
            .throws(IOException::class)
            .addStatement("return newCall().execute()")
            .returns(responseName)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("execute")
            .addTypeVariable(t)
            .throws(IOException::class)
            .addParameter("parser", parserTName)
            .addStatement("return parser.onParse(execute())")
            .returns(t)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("executeString")
            .throws(IOException::class)
            .addStatement("return executeClass(String::class.java)")
            .returns(STRING)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("executeList")
            .addTypeVariable(t)
            .throws(IOException::class)
            .addParameter("type", classTName)
            .addStatement("val tTypeList = %T.get(List::class.java, type)", parameterizedType)
            .addStatement("return execute(%T(tTypeList))", simpleParserName)
            .returns(listTName)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("executeClass")
            .addTypeVariable(t)
            .throws(IOException::class)
            .addParameter("type", classTName)
            .addStatement("return execute(%T(type))", simpleParserName)
            .returns(t)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("newCall")
            .addModifiers(KModifier.OVERRIDE)
            .addCode(
                """
                val request = buildRequest()
                return okHttpClient.newCall(request)
                """.trimIndent()
            )
            .returns(callName)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("buildRequest")
            .addCode(
                """
                if (request == null) {
                    doOnStart()
                    request = param.buildRequest()
                }
                return request!!
                """.trimIndent()
            )
            .returns(requestName)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("doOnStart")
            .addModifiers(KModifier.PRIVATE)
            .addKdoc("请求开始前内部调用，用于添加默认域名等操作\n")
            .addStatement("setConverterToParam(converter)")
            .addStatement("addDefaultDomainIfAbsent()")
            .build()
            .let { methodList.add(it) }

        if (isDependenceRxJava()) {
            val schedulerName = getKClassName("Scheduler")
            val observableName = getKClassName("Observable")
            val consumerName = getKClassName("Consumer")

            val deprecatedAnnotation = AnnotationSpec.builder(Deprecated::class)
                .addMember(
                    """
                    "please use `setSync()` instead, scheduled to be removed in RxHttp 3.0 release.",
                    ReplaceWith("setSync()"),
                    DeprecationLevel.ERROR
                """.trimIndent()
                )
                .build()

            FunSpec.builder("subscribeOnCurrent")
                .addAnnotation(deprecatedAnnotation)
                .addStatement("return setSync()")
                .build()
                .let { methodList.add(it) }

            FunSpec.builder("setSync")
                .addKdoc("RxJava sync request \n")
                .addStatement("isAsync = false")
                .addStatement("return this as R")
                .returns(typeVariableR)
                .build()
                .let { methodList.add(it) }

            val observableTName = observableName.parameterizedBy(t)
            val consumerProgressName = consumerName.parameterizedBy(progressName)

            FunSpec.builder("asParser")
                .addModifiers(KModifier.OVERRIDE)
                .addTypeVariable(t)
                .addParameter("parser", parserTName)
                .addParameter("scheduler", schedulerName.copy(true))
                .addParameter("progressConsumer", consumerProgressName.copy(true))
                .addCode(
                    """
                    val observableCall = if(isAsync) ObservableCallEnqueue(this)
                        else ObservableCallExecute(this)                                
                    return observableCall.asParser(parser, scheduler, progressConsumer)
                    """.trimIndent()
                )
                .returns(observableTName)
                .build()
                .let { methodList.add(it) }
        }

        parserVisitor?.apply {
            methodList.addAll(getFunList(codeGenerator))
        }

        converterVisitor?.apply {
            methodList.addAll(getFunList())
        }

        FunSpec.builder("setConverter")
            .addParameter("converter", converterName)
            .addCode(
                """
                this.converter = converter
                return this as R
                """.trimIndent()
            )
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("setConverterToParam")
            .addKdoc("给Param设置转换器，此方法会在请求发起前，被RxHttp内部调用\n")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("converter", converterName)
            .addStatement("param.tag(IConverter::class.java, converter)")
            .addStatement("return this as R")
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("setOkClient")
            .addParameter("okClient", okHttpClientName)
            .addCode(
                """
                this.okClient = okClient
                return this as R
                """.trimIndent()
            )
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        okClientVisitor?.apply {
            methodList.addAll(getFunList())
        }

        defaultDomainVisitor?.apply {
            methodList.add(getFun())
        }

        domainVisitor?.apply {
            methodList.addAll(getFunList())
        }

        FunSpec.builder("setDomainIfAbsent")
            .addParameter("domain", STRING)
            .addCode(
                """
                val newUrl = addDomainIfAbsent(param.getSimpleUrl(), domain)
                param.setUrl(newUrl)
                return this as R
                """.trimIndent()
            )
            .returns(typeVariableR)
            .build()
            .let { methodList.add(it) }

        //对url添加域名方法
        FunSpec.builder("addDomainIfAbsent")
            .addModifiers(KModifier.PRIVATE)
            .addParameter("url", STRING)
            .addParameter("domain", STRING)
            .addStatement(
                """
                return if (url.startsWith("http")) {
                    url                             
                } else if (url.startsWith("/")) {   
                    if (domain.endsWith("/"))       
                        domain + url.substring(1)  
                    else                            
                        domain + url               
                } else if (domain.endsWith("/")) {  
                    domain + url                   
                } else {                            
                    domain + "/" + url             
                }                                   
                """.trimIndent()
            )
            .returns(STRING)
            .build()
            .let { methodList.add(it) }

        val baseRxHttpName = ClassName(rxHttpPackage, "BaseRxHttp")

        val suppressAnnotation = AnnotationSpec.builder(Suppress::class)
            .addMember("\"UNCHECKED_CAST\", \"UPPER_BOUND_VIOLATED_BASED_ON_JAVA_ANNOTATIONS\"")
            .build()

        val rxHttpBuilder = TypeSpec.classBuilder(RXHttp)
            .addAnnotation(suppressAnnotation)
            .primaryConstructor(constructorFun)
            .addType(companionBuilder.build())
            .addKdoc(
                """
                Github
                https://github.com/liujingxing/rxhttp
                https://github.com/liujingxing/rxlife
                https://github.com/liujingxing/rxhttp/wiki/FAQ
                https://github.com/liujingxing/rxhttp/wiki/更新日志
            """.trimIndent()
            )
            .addModifiers(KModifier.OPEN)
            .addTypeVariable(typeVariableP)
            .addTypeVariable(typeVariableR)
            .superclass(baseRxHttpName)
            .addProperties(propertySpecs)
            .addFunctions(methodList)
            .build()

        val dependencies = Dependencies(true, *ksFiles.toTypedArray())
        FileSpec.builder(rxHttpPackage, RXHttp)
            .addType(rxHttpBuilder)
            .build()
            .writeTo(codeGenerator, dependencies)
    }
}