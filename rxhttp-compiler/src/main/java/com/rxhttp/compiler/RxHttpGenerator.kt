package com.rxhttp.compiler

import com.squareup.javapoet.*
import java.io.IOException
import java.util.*
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement
import kotlin.Boolean
import kotlin.Int
import kotlin.String

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
        val rxHttpPluginsName = ClassName.get("rxhttp", "RxHttpPlugins")
        val converterName = ClassName.get("rxhttp.wrapper.callback", "IConverter")
        val timeUnitName = ClassName.get("java.util.concurrent", "TimeUnit")
        val okHttpClientName = ClassName.get("okhttp3", "OkHttpClient")
        val requestName = ClassName.get("okhttp3", "Request")
        val cacheInterceptorName = ClassName.get("rxhttp.wrapper.intercept", "CacheInterceptor")

        val methodList = ArrayList<MethodSpec>() //方法集合

        methodList.add(  //添加构造方法
            MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(p, "param")
                .addStatement("this.param = param")
                .build()
        )

        methodList.add(
            MethodSpec.methodBuilder("getParam")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return param")
                .returns(p)
                .build()
        )

        methodList.add(
            MethodSpec.methodBuilder("setParam")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(p, "param")
                .addStatement("this.param = param")
                .addStatement("return (R)this")
                .returns(r)
                .build()
        )

        methodList.add(
            MethodSpec.methodBuilder("connectTimeout")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Int::class.javaPrimitiveType, "connectTimeout")
                .addStatement("connectTimeoutMillis = connectTimeout")
                .addStatement("return (R)this")
                .returns(r)
                .build()
        )

        methodList.add(
            MethodSpec.methodBuilder("readTimeout")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Int::class.javaPrimitiveType, "readTimeout")
                .addStatement("readTimeoutMillis = readTimeout")
                .addStatement("return (R)this")
                .returns(r)
                .build()
        )

        methodList.add(
            MethodSpec.methodBuilder("writeTimeout")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Int::class.javaPrimitiveType, "writeTimeout")
                .addStatement("writeTimeoutMillis = writeTimeout")
                .addStatement("return (R)this")
                .returns(r)
                .build()
        )

        methodList.add(
            MethodSpec.methodBuilder("getOkHttpClient")
                .addModifiers(Modifier.PUBLIC)
                .addCode(
                    """
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
                      builder.addInterceptor(new ${'$'}T(getCacheStrategy()));
                    }
                                                                                            
                    realOkClient = builder != null ? builder.build() : okHttpClient;
                    return realOkClient;
                """.trimIndent(), timeUnitName, timeUnitName, timeUnitName, cacheInterceptorName
                )
                .returns(okHttpClientName).build()
        )

        methodList.addAll(mParamsAnnotatedClass!!.getMethodList(filer))
        methodList.addAll(mParserAnnotatedClass!!.getMethodList(filer))
        methodList.addAll(mConverterAnnotatedClass!!.methodList)
        methodList.addAll(mOkClientAnnotatedClass!!.methodList)
        val method = MethodSpec.methodBuilder("addDefaultDomainIfAbsent")
            .addJavadoc("给Param设置默认域名(如何缺席的话)，此方法会在请求发起前，被RxHttp内部调用\n")
            .addModifiers(Modifier.PRIVATE)
            .addParameter(p, "param")
        if (defaultDomain != null) {
            method.addStatement(
                "String newUrl = addDomainIfAbsent(param.getSimpleUrl(), \$T.\$L)",
                ClassName.get(defaultDomain!!.enclosingElement.asType()),
                defaultDomain!!.simpleName.toString()
            )
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
                .build()
        )
        val converterSpec = FieldSpec.builder(converterName, "converter", Modifier.PROTECTED)
            .initializer("\$T.getConverter()", rxHttpPluginsName)
            .build()

        val okHttpClientSpec = FieldSpec.builder(okHttpClientName, "okClient", Modifier.PRIVATE)
            .initializer("\$T.getOkHttpClient()", rxHttpPluginsName)
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
            .addJavadoc(
                """
                Github
                https://github.com/liujingxing/RxHttp
                https://github.com/liujingxing/RxLife
                https://github.com/liujingxing/okhttp-RxHttp/wiki/FAQ
                https://github.com/liujingxing/okhttp-RxHttp/wiki/更新日志
            """.trimIndent()
            )
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(build)
            .addField(Int::class.javaPrimitiveType, "connectTimeoutMillis", Modifier.PRIVATE)
            .addField(Int::class.javaPrimitiveType, "readTimeoutMillis", Modifier.PRIVATE)
            .addField(Int::class.javaPrimitiveType, "writeTimeoutMillis", Modifier.PRIVATE)
            .addField(okHttpClientName, "realOkClient", Modifier.PRIVATE)
            .addField(okHttpClientSpec)
            .addField(converterSpec)
            .addField(isAsyncField)
            .addField(p, "param", Modifier.PROTECTED)
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