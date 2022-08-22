package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.RXHttp
import com.rxhttp.compiler.getClassName
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage
import com.rxhttp.compiler.rxhttpClassName
import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import com.squareup.javapoet.WildcardTypeName
import java.io.IOException
import java.lang.Deprecated
import java.util.*
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier
import kotlin.String
import kotlin.Throws
import kotlin.apply

class RxHttpGenerator {
    var paramsVisitor: ParamsVisitor? = null
    var parserVisitor: ParserVisitor? = null
    var domainVisitor: DomainVisitor? = null
    var converterVisitor: ConverterVisitor? = null
    var okClientVisitor: OkClientVisitor? = null
    var defaultDomainVisitor: DefaultDomainVisitor? = null

    //生成RxHttp类
    @Throws(IOException::class)
    fun generateCode(filer: Filer) {

        val paramClassName = ClassName.get("rxhttp.wrapper.param", "Param")
        val typeVariableP = TypeVariableName.get("P", paramClassName)      //泛型P
        val typeVariableR = TypeVariableName.get("R", rxhttpClassName)     //泛型R

        val okHttpClientName = ClassName.get("okhttp3", "OkHttpClient")
        val requestName = ClassName.get("okhttp3", "Request")
        val headerName = ClassName.get("okhttp3", "Headers")
        val headerBuilderName = ClassName.get("okhttp3", "Headers.Builder")
        val cacheControlName = ClassName.get("okhttp3", "CacheControl")
        val callName = ClassName.get("okhttp3", "Call")
        val responseName = ClassName.get("okhttp3", "Response")

        val timeUnitName = ClassName.get("java.util.concurrent", "TimeUnit")

        val rxHttpPluginsName = ClassName.get("rxhttp", "RxHttpPlugins")
        val converterName = ClassName.get("rxhttp.wrapper.callback", "IConverter")
        val cacheInterceptorName = ClassName.get("rxhttp.wrapper.intercept", "CacheInterceptor")
        val cacheModeName = ClassName.get("rxhttp.wrapper.cahce", "CacheMode")
        val cacheStrategyName = ClassName.get("rxhttp.wrapper.cahce", "CacheStrategy")
        val downloadOffSizeName = ClassName.get("rxhttp.wrapper.entity", "DownloadOffSize")

        val t = TypeVariableName.get("T")
        val wildcard = TypeVariableName.get("?")
        val className = ClassName.get(Class::class.java)
        val superT = WildcardTypeName.supertypeOf(t)
        val classSuperTName = ParameterizedTypeName.get(className, superT)

        val list = ClassName.get(List::class.java)
        val map = ClassName.get(Map::class.java)
        val string = TypeName.get(String::class.java)
        val listName = ParameterizedTypeName.get(list, wildcard)
        val mapName = ParameterizedTypeName.get(map, string, wildcard)
        val mapStringName = ParameterizedTypeName.get(map, string, string)

        val classTName = ParameterizedTypeName.get(className, t)

        val listTName = ParameterizedTypeName.get(list, t)

        val parserName = ClassName.get("rxhttp.wrapper.parse", "Parser")
        val progressName = ClassName.get("rxhttp.wrapper.entity", "Progress")
        val logUtilName = ClassName.get("rxhttp.wrapper.utils", "LogUtil")
        val logInterceptorName = ClassName.get("rxhttp.wrapper.intercept", "LogInterceptor")
        val parserTName = ParameterizedTypeName.get(parserName, t)
        val simpleParserName = ClassName.get("rxhttp.wrapper.parse", "SimpleParser")
        val type = ClassName.get("java.lang.reflect", "Type")
        val parameterizedType = ClassName.get("rxhttp.wrapper.entity", "ParameterizedTypeImpl")


        val methodList = ArrayList<MethodSpec>() //方法集合

        MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PROTECTED)
            .addParameter(typeVariableP, "param")
            .addStatement("this.param = param")
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("getParam")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return param")
            .returns(typeVariableP)
            .build()
            .apply { methodList.add(this) }


        MethodSpec.methodBuilder("setParam")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(typeVariableP, "param")
            .addStatement("this.param = param")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("connectTimeout")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.LONG, "connectTimeout")
            .addStatement("connectTimeoutMillis = connectTimeout")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("readTimeout")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.LONG, "readTimeout")
            .addStatement("readTimeoutMillis = readTimeout")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("writeTimeout")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.LONG, "writeTimeout")
            .addStatement("writeTimeoutMillis = writeTimeout")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

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

        val codeBlock = CodeBlock.builder()
            .add(
                """
                    For example:
                                                             
                    ```                                                  
                    RxHttp.get("/service/%d/...", 1)  
                        .addQuery("size", 20)
                        ...
                    ```
                     url = /service/1/...?size=20
                """.trimIndent()
            )
            .build()

        for ((key, value) in methodMap) {
            val methodBuilder = MethodSpec.methodBuilder(key)
            if (key == "get") {
                methodBuilder.addJavadoc(codeBlock)
            }
            methodBuilder
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(string, "url")
                .addParameter(ArrayTypeName.of(TypeName.OBJECT), "formatArgs")
                .varargs()
                .addStatement(
                    "return new $value(\$T.${key}(format(url, formatArgs)))",
                    paramClassName,
                )
                .returns(ClassName.get(rxHttpPackage, value))
                .build()
                .apply { methodList.add(this) }
        }

        paramsVisitor?.apply {
            methodList.addAll(getMethodList(filer))
        }

        MethodSpec.methodBuilder("setUrl")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "url")
            .addStatement("param.setUrl(url)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addPath")
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc(
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
            .addParameter(string, "name")
            .addParameter(TypeName.OBJECT, "value")
            .addStatement("param.addPath(name, value)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addEncodedPath")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "name")
            .addParameter(TypeName.OBJECT, "value")
            .addStatement("param.addEncodedPath(name, value)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addQuery")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "key")
            .addStatement("param.addQuery(key, null)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addEncodedQuery")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "key")
            .addStatement("param.addEncodedQuery(key, null)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addQuery")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "key")
            .addParameter(TypeName.OBJECT, "value")
            .addStatement("param.addQuery(key,value)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addEncodedQuery")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "key")
            .addParameter(TypeName.OBJECT, "value")
            .addStatement("param.addEncodedQuery(key,value)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addAllQuery")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "key")
            .addParameter(listName, "list")
            .addStatement("param.addAllQuery(key, list)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addAllEncodedQuery")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "key")
            .addParameter(listName, "list")
            .addStatement("param.addAllEncodedQuery(key, list)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addAllQuery")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(mapName, "map")
            .addStatement("param.addAllQuery(map)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addAllEncodedQuery")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(mapName, "map")
            .addStatement("param.addAllEncodedQuery(map)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "line")
            .addStatement("param.addHeader(line)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "line")
            .addParameter(TypeName.BOOLEAN, "isAdd")
            .addCode(
                """
                if (isAdd) 
                    param.addHeader(line);
                return (R) this;
                """.trimIndent()
            )
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addNonAsciiHeader")
            .addJavadoc("Add a header with the specified name and value. Does validation of header names, allowing non-ASCII values.")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "key")
            .addParameter(string, "value")
            .addStatement("param.addNonAsciiHeader(key,value)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("setNonAsciiHeader")
            .addJavadoc("Set a header with the specified name and value. Does validation of header names, allowing non-ASCII values.")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "key")
            .addParameter(string, "value")
            .addStatement("param.setNonAsciiHeader(key,value)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "key")
            .addParameter(string, "value")
            .addStatement("param.addHeader(key,value)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "key")
            .addParameter(string, "value")
            .addParameter(TypeName.BOOLEAN, "isAdd")
            .addCode(
                """
                if (isAdd)
                    param.addHeader(key, value);
                return (R) this;
                """.trimIndent()
            )
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addAllHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(mapStringName, "headers")
            .addStatement("param.addAllHeader(headers)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("addAllHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(headerName, "headers")
            .addStatement("param.addAllHeader(headers)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("setHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "key")
            .addParameter(string, "value")
            .addStatement("param.setHeader(key,value)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("setAllHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(mapStringName, "headers")
            .addStatement("param.setAllHeader(headers)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("setRangeHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.LONG, "startIndex")
            .addStatement("return setRangeHeader(startIndex, -1, false)")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("setRangeHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.LONG, "startIndex")
            .addParameter(TypeName.LONG, "endIndex")
            .addStatement("return setRangeHeader(startIndex, endIndex, false)")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("setRangeHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.LONG, "startIndex")
            .addParameter(TypeName.BOOLEAN, "connectLastProgress")
            .addStatement("return setRangeHeader(startIndex, -1, connectLastProgress)")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("setRangeHeader")
            .addJavadoc(
                """
                设置断点下载开始/结束位置
                @param startIndex 断点下载开始位置
                @param endIndex 断点下载结束位置，默认为-1，即默认结束位置为文件末尾
                @param connectLastProgress 是否衔接上次的下载进度，该参数仅在带进度断点下载时生效
                """.trimIndent()
            )
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.LONG, "startIndex")
            .addParameter(TypeName.LONG, "endIndex")
            .addParameter(TypeName.BOOLEAN, "connectLastProgress")
            .addCode(
                """
                param.setRangeHeader(startIndex, endIndex);                         
                if (connectLastProgress)                                            
                    param.tag(DownloadOffSize.class, new ${'$'}T(startIndex));
                return (R) this;                                                    
                """.trimIndent(), downloadOffSizeName
            )
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("removeAllHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "key")
            .addStatement("param.removeAllHeader(key)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("setHeadersBuilder")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(headerBuilderName, "builder")
            .addStatement("param.setHeadersBuilder(builder)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("setAssemblyEnabled")
            .addJavadoc(
                """
                设置单个接口是否需要添加公共参数,
                即是否回调通过{@link RxHttpPlugins#setOnParamAssembly(Function)}方法设置的接口,默认为true
                """.trimIndent()
            )
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.BOOLEAN, "enabled")
            .addStatement("param.setAssemblyEnabled(enabled)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("setDecoderEnabled")
            .addJavadoc(
                """
                设置单个接口是否需要对Http返回的数据进行解码/解密,
                即是否回调通过{@link RxHttpPlugins#setResultDecoder(Function)}方法设置的接口,默认为true
                """.trimIndent()
            )
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.BOOLEAN, "enabled")
            .addStatement(
                "param.addHeader(\$T.DATA_DECRYPT,String.valueOf(enabled))",
                paramClassName
            )
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("isAssemblyEnabled")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return param.isAssemblyEnabled()")
            .returns(TypeName.BOOLEAN)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("getUrl")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("addDefaultDomainIfAbsent()")
            .addStatement("return param.getUrl()")
            .returns(string)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("getSimpleUrl")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return param.getSimpleUrl()")
            .returns(string)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("getHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "key")
            .addStatement("return param.getHeader(key)")
            .returns(string).build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("getHeaders")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return param.getHeaders()")
            .returns(headerName)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("getHeadersBuilder")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return param.getHeadersBuilder()")
            .returns(headerBuilderName).build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("tag")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.OBJECT, "tag")
            .addStatement("param.tag(tag)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("tag")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addParameter(classSuperTName, "type")
            .addParameter(t, "tag")
            .addStatement("param.tag(type,tag)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("cacheControl")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(cacheControlName, "cacheControl")
            .addStatement("param.cacheControl(cacheControl)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("getCacheStrategy")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return param.getCacheStrategy()")
            .returns(cacheStrategyName)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("setCacheKey")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "cacheKey")
            .addStatement("param.setCacheKey(cacheKey)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("setCacheValidTime")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(TypeName.LONG, "cacheValidTime")
            .addStatement("param.setCacheValidTime(cacheValidTime)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("setCacheMode")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(cacheModeName, "cacheMode")
            .addStatement("param.setCacheMode(cacheMode)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("execute")
            .addModifiers(Modifier.PUBLIC)
            .addException(IOException::class.java)
            .addStatement("return newCall().execute()")
            .returns(responseName)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("execute")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addException(IOException::class.java)
            .addParameter(parserTName, "parser")
            .addStatement("return parser.onParse(execute())")
            .returns(t)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("executeString")
            .addModifiers(Modifier.PUBLIC)
            .addException(IOException::class.java)
            .addStatement("return executeClass(String.class)")
            .returns(string)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("executeList")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addException(IOException::class.java)
            .addParameter(classTName, "type")
            .addStatement("\$T tTypeList = \$T.get(List.class, type)", type, parameterizedType)
            .addStatement("return execute(new \$T<>(tTypeList))", simpleParserName)
            .returns(listTName)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("executeClass")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addException(IOException::class.java)
            .addParameter(classTName, "type")
            .addStatement("return execute(new \$T<>(type))", simpleParserName)
            .returns(t)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("newCall")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addCode(
                """
                Request request = buildRequest();
                OkHttpClient okClient = getOkHttpClient();
                return okClient.newCall(request);
                """.trimIndent()
            )
            .returns(callName)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("buildRequest")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addCode(
                """
                if (request == null) {
                    doOnStart();
                    request = param.buildRequest();
                }
                return request;
                """.trimIndent()
            )
            .returns(requestName)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("getOkHttpClient")
            .addModifiers(Modifier.PUBLIC)
            .addCode(
                """
                if (realOkClient != null) return realOkClient;
                final OkHttpClient okClient = this.okClient;
                OkHttpClient.Builder builder = null;
                
                if (${'$'}T.isDebug()) {
                    builder = okClient.newBuilder();
                    builder.addInterceptor(new ${'$'}T(okClient));
                }
                
                if (connectTimeoutMillis != 0L) {
                    if (builder == null) builder = okClient.newBuilder();
                    builder.connectTimeout(connectTimeoutMillis, ${'$'}T.MILLISECONDS);
                }
                
                if (readTimeoutMillis != 0L) {
                    if (builder == null) builder = okClient.newBuilder();
                    builder.readTimeout(readTimeoutMillis, ${'$'}T.MILLISECONDS);
                }

                if (writeTimeoutMillis != 0L) {
                   if (builder == null) builder = okClient.newBuilder();
                   builder.writeTimeout(writeTimeoutMillis, ${'$'}T.MILLISECONDS);
                }
                
                if (param.getCacheMode() != CacheMode.ONLY_NETWORK) {                      
                    if (builder == null) builder = okClient.newBuilder();              
                    builder.addInterceptor(new ${'$'}T(getCacheStrategy()));
                }
                                                                                        
                realOkClient = builder != null ? builder.build() : okClient;
                return realOkClient;
                """.trimIndent(),
                logUtilName,
                logInterceptorName,
                timeUnitName,
                timeUnitName,
                timeUnitName,
                cacheInterceptorName
            )
            .returns(okHttpClientName)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("doOnStart")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .addJavadoc("请求开始前内部调用，用于添加默认域名等操作\n")
            .addStatement("setConverterToParam(converter)")
            .addStatement("addDefaultDomainIfAbsent()")
            .build()
            .apply { methodList.add(this) }

        if (isDependenceRxJava()) {
            val schedulerName = getClassName("Scheduler")
            val observableName = getClassName("Observable")
            val consumerName = getClassName("Consumer")

            MethodSpec.methodBuilder("subscribeOnCurrent")
                .addAnnotation(Deprecated::class.java)
                .addJavadoc("@deprecated please user {@link #setSync()} instead, scheduled to be removed in RxHttp 3.0 release.\n")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return setSync()")
                .returns(typeVariableR)
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("setSync")
                .addJavadoc("RxJava sync request \n")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("isAsync = false")
                .addStatement("return (R) this")
                .returns(typeVariableR)
                .build()
                .apply { methodList.add(this) }

            val observableTName = ParameterizedTypeName.get(observableName, t)
            val consumerProgressName = ParameterizedTypeName.get(consumerName, progressName)

            MethodSpec.methodBuilder("asParser")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(parserTName, "parser")
                .addParameter(schedulerName, "scheduler")
                .addParameter(consumerProgressName, "progressConsumer")
                .addCode(
                    """
                    ObservableCall observableCall = isAsync ? new ObservableCallEnqueue(this)
                        : new ObservableCallExecute(this);                                
                    return observableCall.asParser(parser, scheduler, progressConsumer);
                    """.trimIndent()
                )
                .returns(observableTName)
                .build()
                .apply { methodList.add(this) }
        }
        parserVisitor?.apply {
            methodList.addAll(getMethodList(filer))
        }

        converterVisitor?.apply {
            methodList.addAll(getMethodList())
        }

        MethodSpec.methodBuilder("setConverter")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(converterName, "converter")
            .addCode(
                """
                if (converter == null)
                    throw new IllegalArgumentException("converter can not be null");
                this.converter = converter;
                return (R) this;
                """.trimIndent()
            )
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("setConverterToParam")
            .addJavadoc("给Param设置转换器，此方法会在请求发起前，被RxHttp内部调用\n")
            .addModifiers(Modifier.PRIVATE)
            .addParameter(converterName, "converter")
            .addStatement("param.tag(IConverter.class, converter)")
            .addStatement("return (R) this")
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("setOkClient")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(okHttpClientName, "okClient")
            .addCode(
                """
                if (okClient == null) 
                    throw new IllegalArgumentException("okClient can not be null");
                this.okClient = okClient;
                return (R) this;
                """.trimIndent()
            )
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        okClientVisitor?.apply {
            methodList.addAll(getMethodList())
        }
        defaultDomainVisitor?.apply {
            methodList.add(getMethod())
        }
        domainVisitor?.apply {
            methodList.addAll(getMethodList())
        }

        MethodSpec.methodBuilder("setDomainIfAbsent")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(string, "domain")
            .addCode(
                """
                String newUrl = addDomainIfAbsent(param.getSimpleUrl(), domain);
                param.setUrl(newUrl);
                return (R) this;
                """.trimIndent()
            )
            .returns(typeVariableR)
            .build()
            .apply { methodList.add(this) }

        //对url添加域名方法
        MethodSpec.methodBuilder("addDomainIfAbsent")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addParameter(string, "url")
            .addParameter(string, "domain")
            .addCode(
                """
                if (url.startsWith("http")) return url;
                if (url.startsWith("/")) {
                    if (domain.endsWith("/"))
                        return domain + url.substring(1);
                    else
                        return domain + url;
                } else if (domain.endsWith("/")) {
                    return domain + url;
                } else {
                    return domain + "/" + url;
                }
                """.trimIndent()
            )
            .returns(string)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("format")
            .addJavadoc("Returns a formatted string using the specified format string and arguments.")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addParameter(string, "url")
            .addParameter(ArrayTypeName.of(TypeName.OBJECT), "formatArgs")
            .varargs()
            .addStatement("return formatArgs == null || formatArgs.length == 0 ? url : String.format(url, formatArgs)")
            .returns(string)
            .build()
            .apply { methodList.add(this) }

        val converterSpec = FieldSpec.builder(converterName, "converter", Modifier.PROTECTED)
            .initializer("\$T.getConverter()", rxHttpPluginsName)
            .build()

        val okHttpClientSpec = FieldSpec.builder(okHttpClientName, "okClient", Modifier.PRIVATE)
            .initializer("\$T.getOkHttpClient()", rxHttpPluginsName)
            .build()
        val suppressWarningsAnnotation = AnnotationSpec.builder(SuppressWarnings::class.java)
            .addMember("value", "\"unchecked\"")
            .build()
        val baseRxHttpName = ClassName.get(rxHttpPackage, "BaseRxHttp")

        val isAsyncField = FieldSpec
            .builder(TypeName.BOOLEAN, "isAsync", Modifier.PROTECTED)
            .initializer("true")
            .build()

        val rxHttpBuilder = TypeSpec.classBuilder(RXHttp)
            .addJavadoc(
                """
                Github
                https://github.com/liujingxing/rxhttp
                https://github.com/liujingxing/rxlife
                https://github.com/liujingxing/rxhttp/wiki/FAQ
                https://github.com/liujingxing/rxhttp/wiki/更新日志
            """.trimIndent()
            )
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(suppressWarningsAnnotation)
            .addField(TypeName.LONG, "connectTimeoutMillis", Modifier.PRIVATE)
            .addField(TypeName.LONG, "readTimeoutMillis", Modifier.PRIVATE)
            .addField(TypeName.LONG, "writeTimeoutMillis", Modifier.PRIVATE)
            .addField(okHttpClientName, "realOkClient", Modifier.PRIVATE)
            .addField(okHttpClientSpec)
            .addField(converterSpec)
            .addField(isAsyncField)
            .addField(typeVariableP, "param", Modifier.PROTECTED)
            .addField(requestName, "request", Modifier.PUBLIC)
            .superclass(baseRxHttpName)
            .addTypeVariable(typeVariableP)
            .addTypeVariable(typeVariableR)
            .addMethods(methodList)

        // Write file
        JavaFile.builder(rxHttpPackage, rxHttpBuilder.build())
            .skipJavaLangImports(true)
            .build().writeTo(filer)
    }
}