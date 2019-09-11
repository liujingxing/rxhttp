package com.rxhttp.compiler;

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

    static final         String CLASSNAME   = "RxHttp";
    private static final String packageName = "rxhttp.wrapper.param";

    static ClassName RXHTTP = ClassName.get(packageName, CLASSNAME);


    static ClassName        paramName  = ClassName.get(packageName, "Param");
    static ClassName        rxHttpName = ClassName.get(packageName, CLASSNAME);
    static TypeVariableName p          = TypeVariableName.get("P", paramName);
    static TypeVariableName r          = TypeVariableName.get("R", rxHttpName);

    private ParamsAnnotatedClass mParamsAnnotatedClass;
    private ParserAnnotatedClass mParserAnnotatedClass;
    private DomainAnnotatedClass mDomainAnnotatedClass;
    private VariableElement      defaultDomain;

    public void setAnnotatedClass(ParamsAnnotatedClass annotatedClass) {
        mParamsAnnotatedClass = annotatedClass;
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

    public void generateCode(Elements elementUtils, Filer filer) throws IOException {
        ClassName httpSenderName = ClassName.get("rxhttp", "HttpSender");
        ClassName rxHttpPluginsName = ClassName.get("rxhttp", "RxHttpPlugins");
        ClassName okHttpClientName = ClassName.get("okhttp3", "OkHttpClient");
        ClassName schedulerName = ClassName.get("io.reactivex", "Scheduler");
        ClassName schedulersName = ClassName.get("io.reactivex.schedulers", "Schedulers");
        ClassName functionsName = ClassName.get("io.reactivex.functions", "Function");

        ClassName stringName = ClassName.get(String.class);

        TypeName mapKVName = ParameterizedTypeName.get(functionsName, paramName, paramName);
        TypeName mapStringName = ParameterizedTypeName.get(functionsName, stringName, stringName);
        List<MethodSpec> methodList = new ArrayList<>(); //方法集合
        MethodSpec.Builder method = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PROTECTED)
            .addParameter(p, "param")
            .addStatement("this.param = param");
        methodList.add(method.build()); //添加构造方法

        method = MethodSpec.methodBuilder("init")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(okHttpClientName, "okHttpClient")
            .addParameter(boolean.class, "debug")
            .addStatement("$T.init(okHttpClient,debug)", httpSenderName)
            .returns(void.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("init")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(okHttpClientName, "okHttpClient")
            .addStatement("$T.init(okHttpClient)", httpSenderName)
            .returns(void.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setDebug")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(boolean.class, "debug")
            .addStatement("$T.setDebug(debug)", httpSenderName)
            .returns(void.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setOnConverter")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addJavadoc("设置统一数据转换接口，每次请求成功后会回调该接口，通过该接口可以拿到Http返回的结果" +
                "\n通过该接口，可以统一对数据解密，并将解密后的数据返回即可" +
                "\n若部分接口不需要回调该接口，发请求前，调用{@link #setConverterEnabled(boolean)}方法设置false即可\n")
            .addParameter(mapStringName, "converter")
            .addStatement("$T.setOnConverter(converter)", rxHttpPluginsName)
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
            .returns(paramName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setParam")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(p, "param")
            .addStatement("this.param = param")
            .addStatement("return (R)this")
            .returns(r);
        methodList.add(method.build());

        WildcardTypeName subString = WildcardTypeName.subtypeOf(TypeName.get(String.class));
        WildcardTypeName subObject = WildcardTypeName.subtypeOf(TypeName.get(Object.class));
        TypeName mapName = ParameterizedTypeName.get(ClassName.get(Map.class), subString, subObject);

        method = MethodSpec.methodBuilder("add")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(mapName, "map")
            .addStatement("param.add(map)")
            .addStatement("return (R)this")
            .returns(r);
        methodList.add(method.build());

        methodList.addAll(mParamsAnnotatedClass.getMethodList());
        methodList.addAll(mParserAnnotatedClass.getMethodList());

        method = MethodSpec.methodBuilder("addDefaultDomainIfAbsent")
            .addParameter(paramName, "param");
        if (defaultDomain != null) {
            method.addStatement("String newUrl = addDomainIfAbsent(param.getSimpleUrl(), $T.$L)",
                ClassName.get(defaultDomain.getEnclosingElement().asType()),
                defaultDomain.getSimpleName().toString())
                .addStatement("param.setUrl(newUrl)");
        }
        method.addStatement("return param")
            .returns(paramName);
        methodList.add(method.build());

        methodList.addAll(mDomainAnnotatedClass.getMethodList());

        FieldSpec fieldSpec = FieldSpec.builder(schedulerName, "scheduler", Modifier.PROTECTED)
            .initializer("$T.io()", schedulersName)
            .addJavadoc("The request is executed on the IO thread by default\n")
            .build();

        TypeSpec rxHttp = TypeSpec.classBuilder(CLASSNAME)
            .addJavadoc("Github" +
                "\nhttps://github.com/liujingxing/RxHttp" +
                "\nhttps://github.com/liujingxing/RxLife\n")
            .addModifiers(Modifier.PUBLIC)
            .addField(p, "param", Modifier.PROTECTED)
            .addField(fieldSpec)
            .addTypeVariable(p)
            .addTypeVariable(r)
            .addMethods(methodList)
            .build();

        // Write file
        JavaFile.builder(packageName, rxHttp)
            .build().writeTo(filer);

        ClassName noBodyParamName = ClassName.get(packageName, "NoBodyParam");
        ClassName rxHttpNoBodyName = ClassName.get(packageName, "RxHttp$NoBodyParam");
        ClassName formParamName = ClassName.get(packageName, "FormParam");
        ClassName rxHttpFormName = ClassName.get(packageName, "RxHttp$FormParam");
        ClassName jsonParamName = ClassName.get(packageName, "JsonParam");
        ClassName rxHttpJsonName = ClassName.get(packageName, "RxHttp$JsonParam");

        TypeName rxHttpNoBody = ParameterizedTypeName.get(RXHTTP, noBodyParamName, rxHttpNoBodyName);
        TypeName rxHttpForm = ParameterizedTypeName.get(RXHTTP, formParamName, rxHttpFormName);
        TypeName rxHttpJson = ParameterizedTypeName.get(RXHTTP, jsonParamName, rxHttpJsonName);

        method = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(noBodyParamName, "param")
            .addStatement("super(param)");

        TypeSpec rxHttpNoBodySpec = TypeSpec.classBuilder("RxHttp$NoBodyParam")
            .addJavadoc("Github" +
                "\nhttps://github.com/liujingxing/RxHttp" +
                "\nhttps://github.com/liujingxing/RxLife\n")
            .addModifiers(Modifier.PUBLIC)
            .superclass(rxHttpNoBody)
            .addMethod(method.build())
            .build();

        JavaFile.builder(packageName, rxHttpNoBodySpec)
            .build().writeTo(filer);

        List<MethodSpec> rxHttpFromMethod = new ArrayList<>();

        method = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(formParamName, "param")
            .addStatement("super(param)");
        rxHttpFromMethod.add(method.build());

        method = MethodSpec.methodBuilder("setMultiForm")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("param.setMultiForm()")
            .addStatement("return this")
            .returns(rxHttpFormName);
        rxHttpFromMethod.add(method.build());

        method = MethodSpec.methodBuilder("setUploadMaxLength")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(long.class, "maxLength")
            .addStatement("param.setUploadMaxLength(maxLength)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        rxHttpFromMethod.add(method.build());

        method = MethodSpec.methodBuilder("add")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(File.class, "file")
            .addStatement("param.add(key,file)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        rxHttpFromMethod.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(File.class, "file")
            .addStatement("param.addFile(key,file)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        rxHttpFromMethod.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(String.class, "filePath")
            .addStatement("param.addFile(key,filePath)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        rxHttpFromMethod.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(String.class, "value")
            .addParameter(String.class, "filePath")
            .addStatement("param.addFile(key,value,filePath)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        rxHttpFromMethod.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(String.class, "value")
            .addParameter(File.class, "file")
            .addStatement("param.addFile(key,value,file)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        rxHttpFromMethod.add(method.build());

        ClassName upFileName = ClassName.get("rxhttp.wrapper.entity", "UpFile");
        TypeName listUpFileName = ParameterizedTypeName.get(ClassName.get(List.class), upFileName);
        TypeName listFileName = ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(File.class));

        method = MethodSpec.methodBuilder("addFile")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(upFileName, "file")
            .addStatement("param.addFile(file)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        rxHttpFromMethod.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(listFileName, "fileList")
            .addStatement("param.addFile(key,fileList)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        rxHttpFromMethod.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(listUpFileName, "fileList")
            .addStatement("param.addFile(fileList)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        rxHttpFromMethod.add(method.build());

        method = MethodSpec.methodBuilder("removeFile")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addStatement("param.removeFile(key)")
            .addStatement("return this")
            .returns(rxHttpFormName);
        rxHttpFromMethod.add(method.build());

        TypeVariableName t = TypeVariableName.get("T");
        TypeName typeName = TypeName.get(String.class);
        ClassName progressName = ClassName.get("rxhttp.wrapper.entity", "Progress");
        TypeName progressTName = ParameterizedTypeName.get(progressName, t);
        TypeName progressStringName = ParameterizedTypeName.get(progressName, typeName);
        ClassName consumerName = ClassName.get("io.reactivex.functions", "Consumer");
        ClassName observableName = ClassName.get("io.reactivex", "Observable");
        TypeName observableStringName = ParameterizedTypeName.get(observableName, typeName);
        TypeName consumerProgressStringName = ParameterizedTypeName.get(consumerName, progressStringName);
        TypeName consumerProgressTName = ParameterizedTypeName.get(consumerName, progressTName);
        ClassName parserName = ClassName.get("rxhttp.wrapper.parse", "Parser");
        TypeName parserTName = ParameterizedTypeName.get(parserName, t);
        TypeName observableTName = ParameterizedTypeName.get(observableName, t);
        ClassName simpleParserName = ClassName.get("rxhttp.wrapper.parse", "SimpleParser");

        method = MethodSpec.methodBuilder("asUpload")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(consumerProgressStringName, "progressConsumer")
            .addStatement("return asUpload($T.get(String.class), progressConsumer, null)", simpleParserName)
            .returns(observableStringName);
        rxHttpFromMethod.add(method.build());

        method = MethodSpec.methodBuilder("asUpload")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(consumerProgressStringName, "progressConsumer")
            .addParameter(schedulerName, "observeOnScheduler")
            .addStatement("return asUpload($T.get(String.class), progressConsumer, observeOnScheduler)", simpleParserName)
            .returns(observableStringName);
        rxHttpFromMethod.add(method.build());

        method = MethodSpec.methodBuilder("asUpload")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addParameter(parserTName, "parser")
            .addParameter(consumerProgressTName, "progressConsumer")
            .addParameter(schedulerName, "observeOnScheduler")
            .addStatement("Observable<Progress<T>> observable = $T\n" +
                ".uploadProgress(addDefaultDomainIfAbsent(param), parser, scheduler)", httpSenderName)
            .beginControlFlow("if(observeOnScheduler != null)")
            .addStatement("observable=observable.observeOn(observeOnScheduler)")
            .endControlFlow()
            .addStatement("return observable.doOnNext(progressConsumer)\n" +
                ".filter(Progress::isCompleted)\n" +
                ".map(Progress::getResult)")
            .returns(observableTName);
        rxHttpFromMethod.add(method.build());


        TypeSpec rxHttpFormSpec = TypeSpec.classBuilder("RxHttp$FormParam")
            .addJavadoc("Github" +
                "\nhttps://github.com/liujingxing/RxHttp" +
                "\nhttps://github.com/liujingxing/RxLife\n")
            .addModifiers(Modifier.PUBLIC)
            .superclass(rxHttpForm)
            .addMethods(rxHttpFromMethod)
            .build();

        JavaFile.builder(packageName, rxHttpFormSpec)
            .build().writeTo(filer);

        List<MethodSpec> rxHttpJsonMethod = new ArrayList<>();


        method = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(jsonParamName, "param")
            .addStatement("super(param)");
        rxHttpJsonMethod.add(method.build());

        method = MethodSpec.methodBuilder("setJsonParams")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "jsonParams")
            .addStatement("param.setJsonParams(jsonParams)")
            .addStatement("return this")
            .returns(rxHttpJsonName);
        rxHttpJsonMethod.add(method.build());

        TypeSpec rxHttpJsonSpec = TypeSpec.classBuilder("RxHttp$JsonParam")
            .addJavadoc("Github" +
                "\nhttps://github.com/liujingxing/RxHttp" +
                "\nhttps://github.com/liujingxing/RxLife\n")
            .addModifiers(Modifier.PUBLIC)
            .superclass(rxHttpJson)
            .addMethods(rxHttpJsonMethod)
            .build();

        JavaFile.builder(packageName, rxHttpJsonSpec)
            .build().writeTo(filer);


    }
}
