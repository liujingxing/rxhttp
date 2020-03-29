package com.rxhttp.compiler;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

public class RxHttpGenerator {

    private static final String CLASSNAME = "RxHttp";
    static final String packageName = "rxhttp.wrapper.param";

    static ClassName RXHTTP = ClassName.get(packageName, CLASSNAME);

    private static TypeVariableName P = TypeVariableName.get("P");
    private static ClassName paramName = ClassName.get(packageName, "Param");
    private static TypeName paramPName = ParameterizedTypeName.get(paramName, P);

    private static ClassName rxHttpName = ClassName.get(packageName, CLASSNAME);
    static TypeVariableName p = TypeVariableName.get("P", paramName);
    static TypeVariableName r = TypeVariableName.get("R", rxHttpName);

    private ParamsAnnotatedClass mParamsAnnotatedClass;
    private ParserAnnotatedClass mParserAnnotatedClass;
    private DomainAnnotatedClass mDomainAnnotatedClass;
    private ConverterAnnotatedClass mConverterAnnotatedClass;
    private VariableElement defaultDomain;

    public void setAnnotatedClass(ParamsAnnotatedClass annotatedClass) {
        mParamsAnnotatedClass = annotatedClass;
    }

    public void setAnnotatedClass(ConverterAnnotatedClass annotatedClass) {
        mConverterAnnotatedClass = annotatedClass;
    }

    public void setAnnotatedClass(DomainAnnotatedClass annotatedClass) {
        mDomainAnnotatedClass = annotatedClass;
    }

    public void setAnnotatedClass(ParserAnnotatedClass annotatedClass) {
        mParserAnnotatedClass = annotatedClass;
    }

    public void setAnnotatedClass(VariableElement defaultDomain) {
        this.defaultDomain = defaultDomain;
    }

    public void generateCode(Elements elementUtils, Filer filer, String platform) throws IOException {
        ClassName httpSenderName = ClassName.get("rxhttp", "HttpSender");
        ClassName rxHttpPluginsName = ClassName.get("rxhttp", "RxHttpPlugins");
        ClassName okHttpClientName = ClassName.get("okhttp3", "OkHttpClient");
        ClassName schedulerName = ClassName.get("io.reactivex", "Scheduler");
        ClassName converterName = ClassName.get("rxhttp.wrapper.callback", "IConverter");
        ClassName schedulersName = ClassName.get("io.reactivex.schedulers", "Schedulers");
        ClassName functionsName = ClassName.get("io.reactivex.functions", "Function");
        ClassName jsonObjectName = ClassName.get("com.google.gson", "JsonObject");
        ClassName jsonArrayName = ClassName.get("com.google.gson", "JsonArray");
        ClassName stringName = ClassName.get(String.class);
        ClassName objectName = ClassName.get(Object.class);

        TypeName mapKVName = ParameterizedTypeName.get(functionsName, paramName, paramName);
        TypeName mapStringName = ParameterizedTypeName.get(functionsName, stringName, stringName);
        WildcardTypeName subObject = WildcardTypeName.subtypeOf(TypeName.get(Object.class));
        TypeName listName = ParameterizedTypeName.get(ClassName.get(List.class), subObject);
        TypeName listObjectName = ParameterizedTypeName.get(ClassName.get(List.class), objectName);

        TypeVariableName t = TypeVariableName.get("T");
        TypeName typeName = TypeName.get(String.class);
        ClassName progressName = ClassName.get("rxhttp.wrapper.entity", "Progress");
        ClassName progressTName = ClassName.get("rxhttp.wrapper.entity", "ProgressT");
        TypeName progressTTName = ParameterizedTypeName.get(progressTName, t);
        TypeName progressStringName = ParameterizedTypeName.get(progressName, typeName);
        ClassName consumerName = ClassName.get("io.reactivex.functions", "Consumer");
        ClassName observableName = ClassName.get("io.reactivex", "Observable");
        TypeName observableStringName = ParameterizedTypeName.get(observableName, typeName);
        TypeName consumerProgressName = ParameterizedTypeName.get(consumerName, progressName);
        ClassName parserName = ClassName.get("rxhttp.wrapper.parse", "Parser");
        TypeName parserTName = ParameterizedTypeName.get(parserName, t);
        TypeName observableTName = ParameterizedTypeName.get(observableName, t);
        ClassName simpleParserName = ClassName.get("rxhttp.wrapper.parse", "SimpleParser");


        ClassName upFileName = ClassName.get("rxhttp.wrapper.entity", "UpFile");
        TypeName listUpFileName = ParameterizedTypeName.get(ClassName.get(List.class), upFileName);
        TypeName listFileName = ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(File.class));
        WildcardTypeName subString = WildcardTypeName.subtypeOf(TypeName.get(String.class));
        TypeName mapName = ParameterizedTypeName.get(ClassName.get(Map.class), subString, subObject);

        ClassName noBodyParamName = ClassName.get(packageName, "NoBodyParam");
        ClassName rxHttpNoBodyName = ClassName.get(packageName, "RxHttpNoBodyParam");
        ClassName formParamName = ClassName.get(packageName, "FormParam");
        ClassName rxHttpFormName = ClassName.get(packageName, "RxHttpFormParam");
        ClassName jsonParamName = ClassName.get(packageName, "JsonParam");
        ClassName rxHttpJsonName = ClassName.get(packageName, "RxHttpJsonParam");
        ClassName jsonArrayParamName = ClassName.get(packageName, "JsonArrayParam");
        ClassName rxHttpJsonArrayName = ClassName.get(packageName, "RxHttpJsonArrayParam");

        TypeName rxHttpNoBody = ParameterizedTypeName.get(RXHTTP, noBodyParamName, rxHttpNoBodyName);
        TypeName rxHttpForm = ParameterizedTypeName.get(RXHTTP, formParamName, rxHttpFormName);
        TypeName rxHttpJson = ParameterizedTypeName.get(RXHTTP, jsonParamName, rxHttpJsonName);
        TypeName rxHttpJsonArray = ParameterizedTypeName.get(RXHTTP, jsonArrayParamName, rxHttpJsonArrayName);

        List<MethodSpec> methodList = new ArrayList<>(); //方法集合
        MethodSpec.Builder method = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PROTECTED)
            .addParameter(p, "param")
            .addStatement("this.param = param");
        methodList.add(method.build()); //添加构造方法

        method = MethodSpec.methodBuilder("setDebug")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(boolean.class, "debug")
            .addStatement("$T.setDebug(debug)", httpSenderName)
            .returns(void.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("init")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(okHttpClientName, "okHttpClient")
            .addStatement("$T.init(okHttpClient)", httpSenderName)
            .returns(void.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("init")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(okHttpClientName, "okHttpClient")
            .addParameter(boolean.class, "debug")
            .addStatement("$T.init(okHttpClient,debug)", httpSenderName)
            .returns(void.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setResultDecoder")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addJavadoc("设置统一数据解码/解密器，每次请求成功后会回调该接口并传入Http请求的结果" +
                "\n通过该接口，可以统一对数据解密，并将解密后的数据返回即可" +
                "\n若部分接口不需要回调该接口，发请求前，调用{@link #setDecoderEnabled(boolean)}方法设置false即可\n")
            .addParameter(mapStringName, "decoder")
            .addStatement("$T.setResultDecoder(decoder)", rxHttpPluginsName)
            .returns(void.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setConverter")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addJavadoc("设置默认的转换器\n")
            .addParameter(converterName, "converter")
            .addStatement("$T.setConverter(converter)", rxHttpPluginsName)
            .returns(void.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setOnParamAssembly")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addJavadoc("设置统一公共参数回调接口,通过该接口,可添加公共参数/请求头，每次请求前会回调该接口" +
                "\n若部分接口不需要添加公共参数,发请求前，调用{@link #setAssemblyEnabled(boolean)}方法设置false即可\n")
            .addParameter(mapKVName, "onParamAssembly")
            .addStatement("$T.setOnParamAssembly(onParamAssembly)", rxHttpPluginsName)
            .returns(void.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("getOkHttpClient")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addStatement("return $T.getOkHttpClient()", httpSenderName)
            .returns(okHttpClientName);
        methodList.add(method.build());


        method = MethodSpec.methodBuilder("getParam")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return param")
            .returns(p);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setParam")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(p, "param")
            .addStatement("this.param = param")
            .addStatement("return (R)this")
            .returns(r);
        methodList.add(method.build());

        methodList.addAll(mParamsAnnotatedClass.getMethodList(filer));
        methodList.addAll(mParserAnnotatedClass.getMethodList(filer));
        methodList.addAll(mConverterAnnotatedClass.getMethodList());
        method = MethodSpec.methodBuilder("addDefaultDomainIfAbsent")
            .addJavadoc("给Param设置默认域名(如何缺席的话)，此方法会在请求发起前，被RxHttp内部调用\n")
            .addModifiers(Modifier.PRIVATE)
            .addParameter(p, "param");
        if (defaultDomain != null) {
            method.addStatement("String newUrl = addDomainIfAbsent(param.getSimpleUrl(), $T.$L)",
                ClassName.get(defaultDomain.getEnclosingElement().asType()),
                defaultDomain.getSimpleName().toString())
                .addStatement("param.setUrl(newUrl)");
        }
        method.addStatement("return param")
            .returns(p);
        methodList.add(method.build());

        methodList.addAll(mDomainAnnotatedClass.getMethodList());

        method = MethodSpec.methodBuilder("format")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addParameter(String.class, "url")
            .addParameter(ArrayTypeName.of(Object.class), "formatArgs")
            .varargs()
            .addStatement("return formatArgs == null || formatArgs.length == 0 ? url : String.format(url, formatArgs)")
            .returns(String.class);
        methodList.add(method.build());

        FieldSpec schedulerField = FieldSpec.builder(schedulerName, "scheduler", Modifier.PROTECTED)
            .initializer("$T.io()", schedulersName)
            .addJavadoc("The request is executed on the IO thread by default\n")
            .build();

        FieldSpec converterSpec = FieldSpec.builder(converterName, "converter", Modifier.PROTECTED)
            .initializer("$T.getConverter()", rxHttpPluginsName)
            .build();

        FieldSpec breakDownloadOffSize = FieldSpec.builder(long.class, "breakDownloadOffSize", Modifier.PRIVATE)  //添加变量
            .initializer("0L")
            .build();

        AnnotationSpec build = AnnotationSpec.builder(SuppressWarnings.class)
            .addMember("value", "\"unchecked\"")
            .build();

        ClassName baseRxHttpName = ClassName.get("rxhttp", "BaseRxHttp");
        TypeSpec rxHttp = TypeSpec.classBuilder(CLASSNAME)
            .addJavadoc("Github" +
                "\nhttps://github.com/liujingxing/RxHttp" +
                "\nhttps://github.com/liujingxing/RxLife\n")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(build)
            .addField(p, "param", Modifier.PROTECTED)
            .addField(schedulerField)
            .addField(converterSpec)
            .addField(breakDownloadOffSize)
            .superclass(baseRxHttpName)
            .addTypeVariable(p)
            .addTypeVariable(r)
            .addMethods(methodList)
            .build();

        // Write file
        JavaFile.builder(packageName, rxHttp)
            .build().writeTo(filer);

        methodList.clear();
        method = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(noBodyParamName, "param")
            .addStatement("super(param)");
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("add")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(Object.class, "value")
            .addStatement("param.add(key,value)")
            .addStatement("return this")
            .returns(rxHttpNoBodyName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addEncoded")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(Object.class, "value")
            .addStatement("param.addEncoded(key,value)")
            .addStatement("return this")
            .returns(rxHttpNoBodyName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("add")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(Object.class, "value")
            .addParameter(boolean.class, "isAdd")
            .beginControlFlow("if(isAdd)")
            .addStatement("param.add(key,value)")
            .endControlFlow()
            .addStatement("return this")
            .returns(rxHttpNoBodyName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addAll")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(mapName, "map")
            .addStatement("param.addAll(map)")
            .addStatement("return this")
            .returns(rxHttpNoBodyName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("removeAllBody")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("param.removeAllBody()")
            .addStatement("return this")
            .returns(rxHttpNoBodyName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("removeAllBody")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addStatement("param.removeAllBody(key)")
            .addStatement("return this")
            .returns(rxHttpNoBodyName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("set")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(Object.class, "value")
            .addStatement("param.set(key,value)")
            .addStatement("return this")
            .returns(rxHttpNoBodyName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setEncoded")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(Object.class, "value")
            .addStatement("param.setEncoded(key,value)")
            .addStatement("return this")
            .returns(rxHttpNoBodyName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("queryValue")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addStatement("return param.queryValue(key)")
            .returns(Object.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("queryValues")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addStatement("return param.queryValues(key)")
            .returns(listObjectName);
        methodList.add(method.build());

        TypeSpec rxHttpNoBodySpec = TypeSpec.classBuilder("RxHttpNoBodyParam")
            .addJavadoc("Github" +
                "\nhttps://github.com/liujingxing/RxHttp" +
                "\nhttps://github.com/liujingxing/RxLife\n")
            .addModifiers(Modifier.PUBLIC)
            .superclass(rxHttpNoBody)
            .addMethods(methodList)
            .build();

        JavaFile.builder(packageName, rxHttpNoBodySpec)
            .build().writeTo(filer);

        methodList.clear();
        method = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(formParamName, "param")
            .addStatement("super(param)");
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("add")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(Object.class, "value")
            .addStatement("param.add(key,value)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addEncoded")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(Object.class, "value")
            .addStatement("param.addEncoded(key,value)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("add")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(Object.class, "value")
            .addParameter(boolean.class, "isAdd")
            .beginControlFlow("if(isAdd)")
            .addStatement("param.add(key,value)")
            .endControlFlow()
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addAll")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(mapName, "map")
            .addStatement("param.addAll(map)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("removeAllBody")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("param.removeAllBody()")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("removeAllBody")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addStatement("param.removeAllBody(key)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("set")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(Object.class, "value")
            .addStatement("param.set(key,value)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setEncoded")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(Object.class, "value")
            .addStatement("param.setEncoded(key,value)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("queryValue")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addStatement("return param.queryValue(key)")
            .returns(Object.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("queryValues")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addStatement("return param.queryValues(key)")
            .returns(listObjectName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("add")
            .addAnnotation(Deprecated.class)
            .addJavadoc("@deprecated please user {@link #addFile(String,File)} instead\n")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(File.class, "file")
            .addStatement("param.add(key,file)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(File.class, "file")
            .addStatement("param.addFile(key,file)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(String.class, "filePath")
            .addStatement("param.addFile(key,filePath)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(String.class, "value")
            .addParameter(String.class, "filePath")
            .addStatement("param.addFile(key,value,filePath)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(String.class, "value")
            .addParameter(File.class, "file")
            .addStatement("param.addFile(key,value,file)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(upFileName, "file")
            .addStatement("param.addFile(file)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(listFileName, "fileList")
            .addStatement("param.addFile(key,fileList)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(listUpFileName, "fileList")
            .addStatement("param.addFile(fileList)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("removeFile")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addStatement("param.removeFile(key)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setMultiForm")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("param.setMultiForm()")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setUploadMaxLength")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(long.class, "maxLength")
            .addStatement("param.setUploadMaxLength(maxLength)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        methodList.add(method.build());

        methodList.add(
            MethodSpec.methodBuilder("upload")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(consumerProgressName, "progressConsumer")
                .addStatement("return upload(progressConsumer, null)")
                .returns(rxHttpFormName)
                .build());

        methodList.add(
            MethodSpec.methodBuilder("upload")
                .addJavadoc("监听上传进度")
                .addJavadoc("\n@param progressConsumer   进度回调")
                .addJavadoc("\n@param observeOnScheduler 用于控制下游回调所在线程(包括进度回调) ，仅当 progressConsumer 不为 null 时生效")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(consumerProgressName, "progressConsumer")
                .addParameter(schedulerName, "observeOnScheduler")
                .addStatement("this.progressConsumer = progressConsumer")
                .addStatement("this.observeOnScheduler = observeOnScheduler")
                .addStatement("return this")
                .returns(rxHttpFormName)
                .build());

        methodList.add(
            MethodSpec.methodBuilder("asParser")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addTypeVariable(t)
                .addParameter(parserTName, "parser")
                .beginControlFlow("if(progressConsumer == null)")
                .addStatement("return super.asParser(parser)")
                .endControlFlow()
                .addStatement("doOnStart()")
                .addStatement("Observable<Progress> observable = $T.uploadProgress(param, parser, scheduler)", httpSenderName)
                .beginControlFlow("if(observeOnScheduler != null)")
                .addStatement("observable = observable.observeOn(observeOnScheduler)")
                .endControlFlow()
                .addStatement("return observable.doOnNext(progressConsumer)\n" +
                    ".filter(progress -> progress instanceof ProgressT)\n" +
                    ".map(progress -> (($T) progress).getResult())", progressTTName)
                .returns(observableTName).build());

        methodList.add(
            MethodSpec.methodBuilder("asUpload")
                .addAnnotation(Deprecated.class)
                .addJavadoc("Will be removed in a future release")
                .addJavadoc("\nPlease use {@link RxHttpFormParam#upload(Consumer, Scheduler)} + asXxx method instead")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(consumerProgressName, "progressConsumer")
                .addStatement("return asUpload($T.get(String.class), progressConsumer, null)", simpleParserName)
                .returns(observableStringName)
                .build());

        methodList.add(
            MethodSpec.methodBuilder("asUpload")
                .addAnnotation(Deprecated.class)
                .addJavadoc("Will be removed in a future release")
                .addJavadoc("\nPlease use {@link RxHttpFormParam#upload(Consumer, Scheduler)} + asXxx method instead")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(consumerProgressName, "progressConsumer")
                .addParameter(schedulerName, "observeOnScheduler")
                .addStatement("return asUpload($T.get(String.class), progressConsumer, observeOnScheduler)", simpleParserName)
                .returns(observableStringName)
                .build());

        methodList.add(
            MethodSpec.methodBuilder("asUpload")
                .addAnnotation(Deprecated.class)
                .addJavadoc("Will be removed in a future release")
                .addJavadoc("\nPlease use {@link RxHttpFormParam#upload(Consumer, Scheduler)} + asXxx method instead")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(parserTName, "parser")
                .addParameter(consumerProgressName, "progressConsumer")
                .addParameter(schedulerName, "observeOnScheduler")
                .addStatement("upload(progressConsumer, observeOnScheduler)")
                .addStatement("return asParser(parser)")
                .returns(observableTName)
                .build());

        FieldSpec observeOnSchedulerField = FieldSpec
            .builder(schedulerName, "observeOnScheduler", Modifier.PRIVATE)
            .addJavadoc("用于控制下游回调所在线程(包括进度回调)，仅当{@link progressConsumer}不为 null 时生效")
            .build();

        FieldSpec progressConsumerField = FieldSpec
            .builder(consumerProgressName, "progressConsumer", Modifier.PRIVATE)
            .addJavadoc("用于监听上传进度回调")
            .build();

        TypeSpec rxHttpFormSpec = TypeSpec.classBuilder("RxHttpFormParam")
            .addJavadoc("Github" +
                "\nhttps://github.com/liujingxing/RxHttp" +
                "\nhttps://github.com/liujingxing/RxLife\n")
            .addModifiers(Modifier.PUBLIC)
            .superclass(rxHttpForm)
            .addField(observeOnSchedulerField)
            .addField(progressConsumerField)
            .addMethods(methodList)
            .build();

        JavaFile.builder(packageName, rxHttpFormSpec)
            .build().writeTo(filer);

        methodList.clear();
        method = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(jsonParamName, "param")
            .addStatement("super(param)");
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("add")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(Object.class, "value")
            .addStatement("param.add(key,value)")
            .addStatement("return this")
            .returns(rxHttpJsonName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("add")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(Object.class, "value")
            .addParameter(boolean.class, "isAdd")
            .beginControlFlow("if(isAdd)")
            .addStatement("param.add(key,value)")
            .endControlFlow()
            .addStatement("return this")
            .returns(rxHttpJsonName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addAll")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(mapName, "map")
            .addStatement("param.addAll(map)")
            .addStatement("return this")
            .returns(rxHttpJsonName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addAll")
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("将Json对象里面的key-value逐一取出，添加到另一个Json对象中，" +
                "\n输入非Json对象将抛出{@link IllegalStateException}异常\n")
            .addParameter(String.class, "jsonObject")
            .addStatement("param.addAll(jsonObject)")
            .addStatement("return this")
            .returns(rxHttpJsonName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addAll")
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("将Json对象里面的key-value逐一取出，添加到另一个Json对象中\n")
            .addParameter(jsonObjectName, "jsonObject")
            .addStatement("param.addAll(jsonObject)")
            .addStatement("return this")
            .returns(rxHttpJsonName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addJsonElement")
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("添加一个JsonElement对象(Json对象、json数组等)\n")
            .addParameter(String.class, "key")
            .addParameter(String.class, "jsonElement")
            .addStatement("param.addJsonElement(key,jsonElement)")
            .addStatement("return this")
            .returns(rxHttpJsonName);
        methodList.add(method.build());

        TypeSpec rxHttpJsonSpec = TypeSpec.classBuilder("RxHttpJsonParam")
            .addJavadoc("Github" +
                "\nhttps://github.com/liujingxing/RxHttp" +
                "\nhttps://github.com/liujingxing/RxLife\n")
            .addModifiers(Modifier.PUBLIC)
            .superclass(rxHttpJson)
            .addMethods(methodList)
            .build();

        JavaFile.builder(packageName, rxHttpJsonSpec)
            .build().writeTo(filer);

        methodList.clear();
        method = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(jsonArrayParamName, "param")
            .addStatement("super(param)");
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("add")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Object.class, "object")
            .addStatement("param.add(object)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("add")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(Object.class, "value")
            .addStatement("param.add(key,value)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("add")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(Object.class, "value")
            .addParameter(boolean.class, "isAdd")
            .beginControlFlow("if(isAdd)")
            .addStatement("param.add(key,value)")
            .endControlFlow()
            .addStatement("return this")
            .returns(rxHttpJsonArrayName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addAll")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(mapName, "map")
            .addStatement("param.addAll(map)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addAll")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(listName, "list")
            .addStatement("param.addAll(list)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addAll")
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("添加多个对象，将字符串转JsonElement对象,并根据不同类型,执行不同操作,可输入任意非空字符串\n")
            .addParameter(String.class, "jsonElement")
            .addStatement("param.addAll(jsonElement)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addAll")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(jsonArrayName, "jsonArray")
            .addStatement("param.addAll(jsonArray)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addAll")
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("将Json对象里面的key-value逐一取出，添加到Json数组中，成为单独的对象\n")
            .addParameter(jsonObjectName, "jsonObject")
            .addStatement("param.addAll(jsonObject)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addJsonElement")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "jsonElement")
            .addStatement("param.addJsonElement(jsonElement)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addJsonElement")
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("添加一个JsonElement对象(Json对象、json数组等)\n")
            .addParameter(String.class, "key")
            .addParameter(String.class, "jsonElement")
            .addStatement("param.addJsonElement(key,jsonElement)")
            .addStatement("return this")
            .returns(rxHttpJsonArrayName);
        methodList.add(method.build());

        TypeSpec rxHttpJsonArraySpec = TypeSpec.classBuilder("RxHttpJsonArrayParam")
            .addJavadoc("Github" +
                "\nhttps://github.com/liujingxing/RxHttp" +
                "\nhttps://github.com/liujingxing/RxLife\n")
            .addModifiers(Modifier.PUBLIC)
            .superclass(rxHttpJsonArray)
            .addMethods(methodList)
            .build();

        JavaFile.builder(packageName, rxHttpJsonArraySpec)
            .build().writeTo(filer);

    }
}
