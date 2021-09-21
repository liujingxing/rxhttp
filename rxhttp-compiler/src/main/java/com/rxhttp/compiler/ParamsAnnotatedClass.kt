package com.rxhttp.compiler

import com.squareup.javapoet.*
import rxhttp.wrapper.annotation.Param
import java.io.IOException
import java.util.*
import javax.annotation.processing.Filer
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeVariable

class ParamsAnnotatedClass {
    private val mElementMap = LinkedHashMap<String, TypeElement>()

    fun add(typeElement: TypeElement) {
        val annotation = typeElement.getAnnotation(Param::class.java)
        val name: String = annotation.methodName
        require(name.isNotEmpty()) {
            String.format("methodName() in @%s for class %s is null or empty! that's not allowed",
                Param::class.java.simpleName, typeElement.qualifiedName.toString())
        }
        mElementMap[name] = typeElement
    }

    @Throws(IOException::class)
    fun getMethodList(filer: Filer?): List<MethodSpec> {
        val rxHttp = r
        val t = TypeVariableName.get("T")
        val superT = WildcardTypeName.supertypeOf(t)
        val classTName = ParameterizedTypeName.get(ClassName.get(Class::class.java), superT)
        val headerName = ClassName.get("okhttp3", "Headers")
        val headerBuilderName = ClassName.get("okhttp3", "Headers.Builder")
        val cacheControlName = ClassName.get("okhttp3", "CacheControl")
        val paramName = ClassName.get(packageName, "Param")
        val cacheModeName = ClassName.get("rxhttp.wrapper.cahce", "CacheMode")
        val cacheStrategyName = ClassName.get("rxhttp.wrapper.cahce", "CacheStrategy")
        val downloadOffSizeName = ClassName.get("rxhttp.wrapper.entity", "DownloadOffSize")
        val stringName = TypeName.get(String::class.java)
        val subObject = WildcardTypeName.subtypeOf(TypeName.get(Any::class.java))
        val listName = ParameterizedTypeName.get(ClassName.get(List::class.java), subObject)
        val mapName = ParameterizedTypeName.get(ClassName.get(Map::class.java), stringName, subObject)
        val mapStringName = ParameterizedTypeName.get(ClassName.get(Map::class.java), stringName, stringName)
        val methodList = ArrayList<MethodSpec>()
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
        var method: MethodSpec.Builder
        val codeBlock = CodeBlock.builder()
            .add("""
                    For example:
                                                             
                    ```                                                  
                    RxHttp.get("/service/%d/...", 1)  
                        .addQuery("size", 20)
                        ...
                    ```
                     url = /service/1/...?size=20
                """.trimIndent())
            .build()

        for ((key, value) in methodMap) {
            val methodBuilder = MethodSpec.methodBuilder(key)
            if (key == "get") {
                methodBuilder.addJavadoc(codeBlock)
            }
            methodList.add(methodBuilder
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String::class.java, "url")
                .addParameter(ArrayTypeName.of(Any::class.java), "formatArgs")
                .varargs()
                .addStatement("return new $value(\$T.\$L(format(url, formatArgs)))", paramName, key)
                .returns(ClassName.get(rxHttpPackage, value))
                .build())
        }
        for ((key, typeElement) in mElementMap) {
            val type = StringBuilder()
            val rxHttpTypeNames = ArrayList<TypeVariableName>()
            val size = typeElement.typeParameters.size;
            for ((i, parameterElement) in typeElement.typeParameters.withIndex()) {
                val typeVariableName = TypeVariableName.get(parameterElement)
                rxHttpTypeNames.add(typeVariableName)
                type.append(if (i == 0) "<" else ",")
                type.append(typeVariableName.name)
                if (i == size - 1) {
                    type.append(">")
                }
            }
            val param = ClassName.get(typeElement)
            val rxHttpName = "RxHttp${typeElement.simpleName}"
            val rxHttpParamName = ClassName.get(rxHttpPackage, rxHttpName)
            val methodReturnType = if (rxHttpTypeNames.size > 0) {
                ParameterizedTypeName.get(rxHttpParamName, *rxHttpTypeNames.toTypedArray())
            } else {
                rxHttpParamName
            }
            //遍历public构造方法
            getConstructorFun(typeElement).forEach {
                val parameterSpecs = ArrayList<ParameterSpec>() //构造方法参数
                val methodBody = StringBuilder("return new \$T(new \$T(") //方法体
                for ((index, element) in it.parameters.withIndex()) {
                    val parameterSpec = ParameterSpec.get(element)
                    parameterSpecs.add(parameterSpec)
                    if (index == 0 && parameterSpec.type.toString().contains("String")) {
                        methodBody.append("format(" + parameterSpecs[0].name + ", formatArgs)")
                        continue
                    } else if (index > 0) {
                        methodBody.append(", ")
                    }
                    methodBody.append(parameterSpec.name)
                }
                methodBody.append("))")
                val methodSpec = MethodSpec.methodBuilder(key)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameters(parameterSpecs)
                    .addTypeVariables(rxHttpTypeNames)
                    .returns(methodReturnType)

                if (parameterSpecs.size > 0 && parameterSpecs[0].type.toString().contains("String")) {
                    methodSpec.addParameter(ArrayTypeName.of(Any::class.java), "formatArgs")
                        .varargs()
                }
                methodSpec.addStatement(methodBody.toString(), rxHttpParamName, param)
                methodList.add(methodSpec.build())
            }
            val superclass = typeElement.superclass
            var prefix = "((" + param.simpleName() + ")param)."
            val rxHttpParam = when (superclass.toString()) {
                "rxhttp.wrapper.param.BodyParam" -> ClassName.get(rxHttpPackage, "RxHttpBodyParam")
                "rxhttp.wrapper.param.FormParam" -> ClassName.get(rxHttpPackage, "RxHttpFormParam")
                "rxhttp.wrapper.param.JsonParam" -> ClassName.get(rxHttpPackage, "RxHttpJsonParam")
                "rxhttp.wrapper.param.JsonArrayParam" -> ClassName.get(rxHttpPackage, "RxHttpJsonArrayParam")
                "rxhttp.wrapper.param.NoBodyParam" -> ClassName.get(rxHttpPackage, "RxHttpNoBodyParam")
                else -> {
                    val typeName = TypeName.get(superclass)
                    if ((typeName as? ParameterizedTypeName)?.rawType?.toString() == "rxhttp.wrapper.param.AbstractBodyParam") {
                        prefix = "param."
                        ClassName.get(rxHttpPackage, "RxHttpAbstractBodyParam").let {
                            ParameterizedTypeName.get(it, param, rxHttpParamName)
                        }
                    } else {
                        prefix = "param."
                        ParameterizedTypeName.get(RXHTTP, param, rxHttpParamName)
                    }
                }
            }
            val rxHttpPostCustomMethod = ArrayList<MethodSpec>()
            rxHttpPostCustomMethod.add(
                MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(param, "param")
                    .addStatement("super(param)")
                    .build())
            for (enclosedElement in typeElement.enclosedElements) {
                if (enclosedElement !is ExecutableElement
                    || enclosedElement.getKind() != ElementKind.METHOD //过滤非方法，
                    || !enclosedElement.getModifiers().contains(Modifier.PUBLIC) //过滤非public修饰符
                    || enclosedElement.getAnnotation(Override::class.java) != null //过滤重写的方法
                ) continue
                var returnType = TypeName.get(enclosedElement.returnType) //方法返回值
                if (returnType.toString() == param.toString()) {
                    returnType = rxHttpParamName
                }
                val parameterSpecs: MutableList<ParameterSpec> = ArrayList() //方法参数
                val methodBody = StringBuilder(enclosedElement.getSimpleName().toString()) //方法体
                    .append("(")
                for (element in enclosedElement.parameters) {
                    val parameterSpec = ParameterSpec.get(element)
                    parameterSpecs.add(parameterSpec)
                    methodBody.append(parameterSpec.name).append(",")
                }
                if (methodBody.toString().endsWith(",")) {
                    methodBody.deleteCharAt(methodBody.length - 1)
                }
                methodBody.append(")")
                val typeVariableNames: MutableList<TypeVariableName> = ArrayList() //方法声明的泛型
                for (element in enclosedElement.typeParameters) {
                    val typeVariableName = TypeVariableName.get(element.asType() as TypeVariable)
                    typeVariableNames.add(typeVariableName)
                }
                val throwTypeName: MutableList<TypeName> = ArrayList() //方法要抛出的异常
                for (mirror in enclosedElement.thrownTypes) {
                    val typeName = TypeName.get(mirror)
                    throwTypeName.add(typeName)
                }
                method = MethodSpec.methodBuilder(enclosedElement.getSimpleName().toString())
                    .addModifiers(enclosedElement.getModifiers())
                    .addTypeVariables(typeVariableNames)
                    .addExceptions(throwTypeName)
                    .addParameters(parameterSpecs)
                if (enclosedElement.isVarArgs) {
                    method.varargs()
                }
                if (returnType === rxHttpParamName) {
                    method.addStatement(prefix + methodBody, param)
                        .addStatement("return this")
                } else if (returnType.toString() == "void") {
                    method.addStatement(prefix + methodBody)
                } else {
                    method.addStatement("return $prefix$methodBody", param)
                }
                method.returns(returnType)
                rxHttpPostCustomMethod.add(method.build())
            }
            val rxHttpPostEncryptFormParamSpec = TypeSpec.classBuilder(rxHttpName)
                .addJavadoc("""
                    Github
                    https://github.com/liujingxing/rxhttp
                    https://github.com/liujingxing/rxlife
                """.trimIndent())
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariables(rxHttpTypeNames)
                .superclass(rxHttpParam)
                .addMethods(rxHttpPostCustomMethod)
                .build()
            JavaFile.builder(rxHttpPackage, rxHttpPostEncryptFormParamSpec)
                .skipJavaLangImports(true)
                .build().writeTo(filer)
        }

        methodList.add(
            MethodSpec.methodBuilder("setUrl")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "url")
                .addStatement("param.setUrl(url)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addQuery")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addStatement("param.addQuery(key, null)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addEncodedQuery")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addStatement("param.addEncodedQuery(key, null)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addQuery")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(Any::class.java, "value")
                .addStatement("param.addQuery(key,value)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addEncodedQuery")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(Any::class.java, "value")
                .addStatement("param.addEncodedQuery(key,value)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addAllQuery")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(listName, "list")
                .addStatement("param.addAllQuery(key, list)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addAllEncodedQuery")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(listName, "list")
                .addStatement("param.addAllEncodedQuery(key, list)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addAllQuery")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mapName, "map")
                .addStatement("param.addAllQuery(map)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addAllEncodedQuery")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mapName, "map")
                .addStatement("param.addAllEncodedQuery(map)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "line")
                .addStatement("param.addHeader(line)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "line")
                .addParameter(Boolean::class.javaPrimitiveType, "isAdd")
                .beginControlFlow("if(isAdd)")
                .addStatement("param.addHeader(line)")
                .endControlFlow()
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addNonAsciiHeader")
                .addJavadoc("Add a header with the specified name and value. Does validation of header names, allowing non-ASCII values.")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(String::class.java, "value")
                .addStatement("param.addNonAsciiHeader(key,value)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setNonAsciiHeader")
                .addJavadoc("Set a header with the specified name and value. Does validation of header names, allowing non-ASCII values.")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(String::class.java, "value")
                .addStatement("param.setNonAsciiHeader(key,value)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(String::class.java, "value")
                .addStatement("param.addHeader(key,value)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(String::class.java, "value")
                .addParameter(Boolean::class.javaPrimitiveType, "isAdd")
                .beginControlFlow("if(isAdd)")
                .addStatement("param.addHeader(key,value)")
                .endControlFlow()
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addAllHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mapStringName, "headers")
                .addStatement("param.addAllHeader(headers)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("addAllHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(headerName, "headers")
                .addStatement("param.addAllHeader(headers)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addParameter(String::class.java, "value")
                .addStatement("param.setHeader(key,value)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setAllHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mapStringName, "headers")
                .addStatement("param.setAllHeader(headers)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setRangeHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Long::class.javaPrimitiveType, "startIndex")
                .addStatement("return setRangeHeader(startIndex, -1, false)")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setRangeHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Long::class.javaPrimitiveType, "startIndex")
                .addParameter(Long::class.javaPrimitiveType, "endIndex")
                .addStatement("return setRangeHeader(startIndex, endIndex, false)")
                .returns(rxHttp).build())

        methodList.add(
            MethodSpec.methodBuilder("setRangeHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Long::class.javaPrimitiveType, "startIndex")
                .addParameter(Boolean::class.javaPrimitiveType, "connectLastProgress")
                .addStatement("return setRangeHeader(startIndex, -1, connectLastProgress)")
                .returns(rxHttp).build())

        methodList.add(
            MethodSpec.methodBuilder("setRangeHeader")
                .addJavadoc("""
                    设置断点下载开始/结束位置
                    @param startIndex 断点下载开始位置
                    @param endIndex 断点下载结束位置，默认为-1，即默认结束位置为文件末尾
                    @param connectLastProgress 是否衔接上次的下载进度，该参数仅在带进度断点下载时生效
                """.trimIndent())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Long::class.javaPrimitiveType, "startIndex")
                .addParameter(Long::class.javaPrimitiveType, "endIndex")
                .addParameter(Boolean::class.javaPrimitiveType, "connectLastProgress")
                .addCode("""
                    param.setRangeHeader(startIndex, endIndex);                         
                    if (connectLastProgress)                                            
                      param.tag(DownloadOffSize.class, new ${'$'}T(startIndex));
                    return (R) this;                                                    
                """.trimIndent(), downloadOffSizeName)
                .returns(rxHttp).build())

        methodList.add(
            MethodSpec.methodBuilder("removeAllHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addStatement("param.removeAllHeader(key)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setHeadersBuilder")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(headerBuilderName, "builder")
                .addStatement("param.setHeadersBuilder(builder)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setAssemblyEnabled")
                .addJavadoc("""
                    设置单个接口是否需要添加公共参数,
                    即是否回调通过{@link RxHttpPlugins#setOnParamAssembly(Function)}方法设置的接口,默认为true
                """.trimIndent())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Boolean::class.javaPrimitiveType, "enabled")
                .addStatement("param.setAssemblyEnabled(enabled)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setDecoderEnabled")
                .addJavadoc("""
                    设置单个接口是否需要对Http返回的数据进行解码/解密,
                    即是否回调通过{@link RxHttpPlugins#setResultDecoder(Function)}方法设置的接口,默认为true
                """.trimIndent())
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Boolean::class.javaPrimitiveType, "enabled")
                .addStatement("param.addHeader(\$T.DATA_DECRYPT,String.valueOf(enabled))", paramName)
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(MethodSpec.methodBuilder("isAssemblyEnabled")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return param.isAssemblyEnabled()")
            .returns(Boolean::class.javaPrimitiveType)
            .build())

        methodList.add(
            MethodSpec.methodBuilder("getUrl")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("addDefaultDomainIfAbsent(param)")
                .addStatement("return param.getUrl()")
                .returns(String::class.java)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("getSimpleUrl")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return param.getSimpleUrl()")
                .returns(String::class.java)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("getHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "key")
                .addStatement("return param.getHeader(key)")
                .returns(String::class.java).build())

        methodList.add(
            MethodSpec.methodBuilder("getHeaders")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return param.getHeaders()")
                .returns(headerName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("getHeadersBuilder")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return param.getHeadersBuilder()")
                .returns(headerBuilderName).build())

        methodList.add(
            MethodSpec.methodBuilder("tag")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Any::class.java, "tag")
                .addStatement("param.tag(tag)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("tag")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(classTName, "type")
                .addParameter(t, "tag")
                .addStatement("param.tag(type,tag)")
                .addStatement("return (R)this")
                .returns(rxHttp).build())

        methodList.add(
            MethodSpec.methodBuilder("cacheControl")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(cacheControlName, "cacheControl")
                .addStatement("param.cacheControl(cacheControl)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("getCacheStrategy")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return param.getCacheStrategy()")
                .returns(cacheStrategyName)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setCacheKey")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String::class.java, "cacheKey")
                .addStatement("param.setCacheKey(cacheKey)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setCacheValidTime")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Long::class.javaPrimitiveType, "cacheValidTime")
                .addStatement("param.setCacheValidTime(cacheValidTime)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())

        methodList.add(
            MethodSpec.methodBuilder("setCacheMode")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(cacheModeName, "cacheMode")
                .addStatement("param.setCacheMode(cacheMode)")
                .addStatement("return (R)this")
                .returns(rxHttp)
                .build())
        return methodList
    }

    //获取构造方法
    private fun getConstructorFun(typeElement: TypeElement): MutableList<ExecutableElement> {
        val funList = ArrayList<ExecutableElement>()
        typeElement.enclosedElements.forEach {
            if (it is ExecutableElement
                && it.kind == ElementKind.CONSTRUCTOR
                && it.getModifiers().contains(Modifier.PUBLIC)
            ) {
                funList.add(it)
            }
        }
        return funList
    }
}