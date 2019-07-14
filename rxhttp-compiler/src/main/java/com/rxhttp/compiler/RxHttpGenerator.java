package com.rxhttp.compiler;

import com.squareup.javapoet.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

public class RxHttpGenerator {

    static final String CLASSNAME   = "RxHttp";
    private static final String packageName = "rxhttp.wrapper.param";

    static ClassName RXHTTP = ClassName.get(packageName, CLASSNAME);

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
        ClassName paramName = ClassName.get(packageName, "Param");
        ClassName stringName = ClassName.get(String.class);

        TypeName mapKVName = ParameterizedTypeName.get(functionsName, paramName, paramName);
        TypeName mapStringName = ParameterizedTypeName.get(functionsName, stringName, stringName);
        List<MethodSpec> methodList = new ArrayList<>(); //方法集合
        MethodSpec.Builder method = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(paramName, "param")
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
            .addJavadoc("Set Converter")
            .addParameter(mapStringName, "converter")
            .addStatement("$T.setOnConverter(converter)", rxHttpPluginsName)
            .returns(void.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setOnParamAssembly")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addJavadoc("Set common parameters")
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
                .addParameter(paramName, "param")
                .addStatement("this.param = param")
                .addStatement("return this")
                .returns(RxHttpGenerator.RXHTTP);
        methodList.add(method.build());

        WildcardTypeName subString = WildcardTypeName.subtypeOf(TypeName.get(String.class));
        WildcardTypeName subObject = WildcardTypeName.subtypeOf(TypeName.get(Object.class));
        TypeName mapName = ParameterizedTypeName.get(ClassName.get(Map.class), subString, subObject);

        method = MethodSpec.methodBuilder("add")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mapName, "map")
                .addStatement("param.add(map)")
                .addStatement("return this")
                .returns(RxHttpGenerator.RXHTTP);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("with")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(paramName, "param")
                .addStatement("return new $L(param)", CLASSNAME)
                .returns(RxHttpGenerator.RXHTTP);
        methodList.add(method.build());

        methodList.addAll(mParamsAnnotatedClass.getMethodList());
        methodList.addAll(mDomainAnnotatedClass.getMethodList());
        methodList.addAll(mParserAnnotatedClass.getMethodList());


        method = MethodSpec.methodBuilder("addDefaultDomainIfAbsent")
                .addModifiers(Modifier.PUBLIC)
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

        FieldSpec fieldSpec = FieldSpec.builder(schedulerName, "scheduler", Modifier.PRIVATE)
            .initializer("$T.io()", schedulersName)
            .addJavadoc("The request is executed on the IO thread by default")
            .build();
        TypeSpec typeSpec = TypeSpec
            .classBuilder(CLASSNAME)
            .addJavadoc("Github：https://github.com/liujingxing/RxHttp")
            .addModifiers(Modifier.PUBLIC)
            .addField(paramName, "param", Modifier.PRIVATE)
            .addField(fieldSpec)
            .addMethods(methodList)
            .build();

        // Write file
        JavaFile.builder(packageName, typeSpec).build().writeTo(filer);
    }
}
