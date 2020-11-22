package com.rxhttp.compiler

import com.squareup.javapoet.*
import java.io.File
import java.io.IOException
import java.lang.Deprecated
import java.util.*
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement
import kotlin.Any
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Int
import kotlin.String
import kotlin.Throws

class RxHttpGenerator {
    private var mParamsAnnotatedClass: ParamsAnnotatedClass? = null
    private var mParserAnnotatedClass: ParserAnnotatedClass? = null
    private var mDomainAnnotatedClass: DomainAnnotatedClass? = null
    private var mConverterAnnotatedClass: ConverterAnnotatedClass? = null
    private var mOkClientAnnotatedClass: OkClientAnnotatedClass? = null
    private var defaultDomain: VariableElement? = null
    fun setAnnotatedClass(annotatedClass: ParamsAnnotatedClass?) {
        mParamsAnnotatedClass = annotatedClass
    }

    fun setAnnotatedClass(annotatedClass: ConverterAnnotatedClass?) {
        mConverterAnnotatedClass = annotatedClass
    }

    fun setAnnotatedClass(annotatedClass: OkClientAnnotatedClass?) {
        mOkClientAnnotatedClass = annotatedClass
    }

    fun setAnnotatedClass(annotatedClass: DomainAnnotatedClass?) {
        mDomainAnnotatedClass = annotatedClass
    }

    fun setAnnotatedClass(annotatedClass: ParserAnnotatedClass?) {
        mParserAnnotatedClass = annotatedClass
    }

    fun setAnnotatedClass(defaultDomain: VariableElement?) {
        this.defaultDomain = defaultDomain
    }

    //生成RxHttp类
    @Throws(IOException::class)
    fun generateCode(filer: Filer) {
        val httpSenderName = ClassName.get("rxhttp", "HttpSender")
        val logUtilName = ClassName.get("rxhttp.wrapper.utils", "LogUtil")
        val rxHttpPluginsName = ClassName.get("rxhttp", "RxHttpPlugins")
        val converterName = ClassName.get("rxhttp.wrapper.callback", "IConverter")
        val functionsName = ClassName.get("rxhttp.wrapper.callback", "Function")
        val stringName = ClassName.get(String::class.java)
        val timeUnitName = ClassName.get("java.util.concurrent", "TimeUnit")
        val paramTName = ParameterizedTypeName.get(paramName, TypeVariableName.get("?"))
        val mapKVName = ParameterizedTypeName.get(functionsName, paramTName, paramTName)
        val mapStringName = ParameterizedTypeName.get(functionsName, stringName, stringName)
        val okHttpClientName = ClassName.get("okhttp3", "OkHttpClient")
        val requestName = ClassName.get("okhttp3", "Request")
        val cacheInterceptorName = ClassName.get("rxhttp.wrapper.intercept", "CacheInterceptor")

        val methodList = ArrayList<MethodSpec>() //方法集合

        methodList.add(
            MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(p, "param")
                .addStatement("this.param = param")
                .build()) //添加构造方法

        methodList.add(
            MethodSpec.methodBuilder("setDebug")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Boolean::class.javaPrimitiveType, "debug")
                .addStatement("setDebug(debug, false)")
                .returns(Void.TYPE)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setDebug")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Boolean::class.javaPrimitiveType, "debug")
                .addParameter(Boolean::class.javaPrimitiveType, "segmentPrint")
                .addStatement("\$T.setDebug(debug, segmentPrint)", logUtilName)
                .returns(Void.TYPE)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(okHttpClientName, "okHttpClient")
                .addStatement("\$T.init(okHttpClient)", httpSenderName)
                .returns(Void.TYPE)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("init")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(okHttpClientName, "okHttpClient")
                .addParameter(Boolean::class.javaPrimitiveType, "debug")
                .addStatement("\$T.init(okHttpClient,debug)", httpSenderName)
                .returns(Void.TYPE)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("isInit")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addStatement("return \$T.isInit()", httpSenderName)
                .returns(Boolean::class.java)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setResultDecoder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addJavadoc("""
                    设置统一数据解码/解密器，每次请求成功后会回调该接口并传入Http请求的结果
                    通过该接口，可以统一对数据解密，并将解密后的数据返回即可
                    若部分接口不需要回调该接口，发请求前，调用{@link #setDecoderEnabled(boolean)}方法设置false即可
                """.trimIndent())
                .addParameter(mapStringName, "decoder")
                .addStatement("\$T.setResultDecoder(decoder)", rxHttpPluginsName)
                .returns(Void.TYPE)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setConverter")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addJavadoc("设置默认的转换器\n")
                .addParameter(converterName, "converter")
                .addStatement("\$T.setConverter(converter)", rxHttpPluginsName)
                .returns(Void.TYPE)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setOnParamAssembly")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addJavadoc("""
                    设置统一公共参数回调接口,通过该接口,可添加公共参数/请求头，每次请求前会回调该接口
                    若部分接口不需要添加公共参数,发请求前，调用{@link #setAssemblyEnabled(boolean)}方法设置false即可
                """.trimIndent())
                .addParameter(mapKVName, "onParamAssembly")
                .addStatement("\$T.setOnParamAssembly(onParamAssembly)", rxHttpPluginsName)
                .returns(Void.TYPE)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("connectTimeout")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Int::class.javaPrimitiveType, "connectTimeout")
                .addStatement("connectTimeoutMillis = connectTimeout")
                .addStatement("return (R)this")
                .returns(r)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("readTimeout")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Int::class.javaPrimitiveType, "readTimeout")
                .addStatement("readTimeoutMillis = readTimeout")
                .addStatement("return (R)this")
                .returns(r)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("writeTimeout")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Int::class.javaPrimitiveType, "writeTimeout")
                .addStatement("writeTimeoutMillis = writeTimeout")
                .addStatement("return (R)this")
                .returns(r)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("getOkHttpClient")
                .addModifiers(Modifier.PUBLIC)
                .addCode("""
                    if (realOkClient != null) return realOkClient;
                    final OkHttpClient okHttpClient = okClient;
                    OkHttpClient.Builder builder = null;
                    
                    if (connectTimeoutMillis != 0) {
                      if (builder == null) builder = okHttpClient.newBuilder();
                      builder.connectTimeout(connectTimeoutMillis, ${'$'}T.MILLISECONDS);
                    }
                    
                    if (readTimeoutMillis != 0) {
                      if (builder == null) builder = okHttpClient.newBuilder();
                      builder.readTimeout(readTimeoutMillis, ${'$'}T.MILLISECONDS);
                    }

                    if (writeTimeoutMillis != 0) {
                      if (builder == null) builder = okHttpClient.newBuilder();
                      builder.writeTimeout(writeTimeoutMillis, ${'$'}T.MILLISECONDS);
                    }
                    
                    if (param.getCacheMode() != CacheMode.ONLY_NETWORK) {                      
                      if (builder == null) builder = okHttpClient.newBuilder();              
                      builder.addInterceptor(new ${'$'}T(param.getCacheStrategy()));
                    }
                                                                                            
                    realOkClient = builder != null ? builder.build() : okHttpClient;
                    return realOkClient;
                """.trimIndent(), timeUnitName, timeUnitName, timeUnitName, cacheInterceptorName)
                .returns(okHttpClientName).build())

        if (isDependenceRxJava()) {
            val disposableName = getClassName("Disposable")
            methodList.add(
                MethodSpec.methodBuilder("dispose")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(disposableName, "disposable")
                    .addStatement("if (!isDisposed(disposable)) disposable.dispose()")
                    .build())

            methodList.add(
                MethodSpec.methodBuilder("isDisposed")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(disposableName, "disposable")
                    .addStatement("return disposable == null || disposable.isDisposed()")
                    .returns(Boolean::class.java).build())
        }

        methodList.add(
            MethodSpec.methodBuilder("getParam")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return param")
                .returns(p)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setParam")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(p, "param")
                .addStatement("this.param = param")
                .addStatement("return (R)this")
                .returns(r)
                .build())
        methodList.addAll(mParamsAnnotatedClass!!.getMethodList(filer))
        methodList.addAll(mParserAnnotatedClass!!.getMethodList(filer))
        methodList.addAll(mConverterAnnotatedClass!!.methodList)
        methodList.addAll(mOkClientAnnotatedClass!!.methodList)
        val method = MethodSpec.methodBuilder("addDefaultDomainIfAbsent")
            .addJavadoc("给Param设置默认域名(如何缺席的话)，此方法会在请求发起前，被RxHttp内部调用\n")
            .addModifiers(Modifier.PRIVATE)
            .addParameter(p, "param")
        if (defaultDomain != null) {
            method.addStatement("String newUrl = addDomainIfAbsent(param.getSimpleUrl(), \$T.\$L)",
                ClassName.get(defaultDomain!!.enclosingElement.asType()),
                defaultDomain!!.simpleName.toString())
                .addStatement("param.setUrl(newUrl)")
        }
        method.addStatement("return param")
            .returns(p)
        methodList.add(method.build())
        methodList.addAll(mDomainAnnotatedClass!!.methodList)

        methodList.add(
            MethodSpec.methodBuilder("format")
                .addJavadoc("Returns a formatted string using the specified format string and arguments.")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(String::class.java, "url")
                .addParameter(ArrayTypeName.of(Any::class.java), "formatArgs")
                .varargs()
                .addStatement("return formatArgs == null || formatArgs.length == 0 ? url : String.format(url, formatArgs)")
                .returns(String::class.java)
                .build())
        val converterSpec = FieldSpec.builder(converterName, "converter", Modifier.PROTECTED)
            .initializer("\$T.getConverter()", rxHttpPluginsName)
            .build()

        val okHttpClientSpec = FieldSpec.builder(okHttpClientName, "okClient", Modifier.PRIVATE)
            .initializer("\$T.getOkHttpClient()", httpSenderName)
            .build()
        val build = AnnotationSpec.builder(SuppressWarnings::class.java)
            .addMember("value", "\"unchecked\"")
            .build()
        val baseRxHttpName = ClassName.get(rxHttpPackage, "BaseRxHttp")

        val isAsyncField = FieldSpec
            .builder(Boolean::class.javaPrimitiveType, "isAsync", Modifier.PROTECTED)
            .initializer("true")
            .build()

        val rxHttpBuilder = TypeSpec.classBuilder(RXHttp_CLASS_NAME)
            .addJavadoc("""
                Github
                https://github.com/liujingxing/RxHttp
                https://github.com/liujingxing/RxLife
                https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ
                https://github.com/liujingxing/okhttp-RxHttp/wiki/更新日志
            """.trimIndent())
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(build)
            .addField(p, "param", Modifier.PROTECTED)
            .addField(Int::class.javaPrimitiveType, "connectTimeoutMillis", Modifier.PRIVATE)
            .addField(Int::class.javaPrimitiveType, "readTimeoutMillis", Modifier.PRIVATE)
            .addField(Int::class.javaPrimitiveType, "writeTimeoutMillis", Modifier.PRIVATE)
            .addField(okHttpClientName, "realOkClient", Modifier.PRIVATE)
            .addField(okHttpClientSpec)
            .addField(isAsyncField)
            .addField(converterSpec)
            .addField(requestName, "request", Modifier.PUBLIC)
            .superclass(baseRxHttpName)
            .addTypeVariable(p)
            .addTypeVariable(r)
            .addMethods(methodList)

        // Write file
        JavaFile.builder(rxHttpPackage, rxHttpBuilder.build())
            .build().writeTo(filer)
    }
}

const val RXHttp_CLASS_NAME = "RxHttp"
const val packageName = "rxhttp.wrapper.param"
var RXHTTP = ClassName.get(rxHttpPackage, RXHttp_CLASS_NAME)
private val paramName = ClassName.get(packageName, "Param")
var p = TypeVariableName.get("P", paramName)  //泛型P
var r = TypeVariableName.get("R", RXHTTP)     //泛型R