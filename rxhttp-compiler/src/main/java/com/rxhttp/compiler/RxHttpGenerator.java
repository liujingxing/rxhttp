package com.rxhttp.compiler;

import com.squareup.javapoet.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
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
        ClassName schedulerName = ClassName.get("io.reactivex", "Scheduler");
        ClassName schedulersName = ClassName.get("io.reactivex.schedulers", "Schedulers");
        TypeElement superClassName = elementUtils.getTypeElement(packageName + ".Param");

        List<MethodSpec> methodList = new ArrayList<>(); //方法集合
        MethodSpec constructorMethod = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(TypeName.get(superClassName.asType()), "param")
                .addStatement("this.param = param")
                .build();
        methodList.add(constructorMethod); //添加构造方法

        MethodSpec.Builder method = MethodSpec.methodBuilder("getParam")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return param")
                .returns(TypeName.get(superClassName.asType()));
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setParam")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(superClassName.asType()), "param")
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
                .addParameter(TypeName.get(superClassName.asType()), "param")
                .addStatement("return new $L(param)", CLASSNAME)
                .returns(RxHttpGenerator.RXHTTP);
        methodList.add(method.build());

        methodList.addAll(mParamsAnnotatedClass.getMethodList());
        methodList.addAll(mDomainAnnotatedClass.getMethodList());
        methodList.addAll(mParserAnnotatedClass.getMethodList());


        method = MethodSpec.methodBuilder("addDefaultDomainIfAbsent")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(superClassName.asType()), "param");
        if (defaultDomain != null) {
            method.addStatement("String newUrl = addDomainIfAbsent(param.getSimpleUrl(), $T.$L)",
                    ClassName.get(defaultDomain.getEnclosingElement().asType()),
                    defaultDomain.getSimpleName().toString())
                    .addStatement("param.setUrl(newUrl)");
        }
        method.addStatement("return param")
                .returns(TypeName.get(superClassName.asType()));
        methodList.add(method.build());

        FieldSpec fieldSpec = FieldSpec.builder(schedulerName, "scheduler", Modifier.PRIVATE)
            .initializer("$T.io()", schedulersName)
            .addJavadoc("The request is executed on the IO thread by default")
            .build();
        TypeSpec typeSpec = TypeSpec
            .classBuilder(CLASSNAME)
            .addJavadoc("Github：https://github.com/liujingxing/RxHttp")
            .addModifiers(Modifier.PUBLIC)
            .addField(TypeName.get(superClassName.asType()), "param", Modifier.PRIVATE)
            .addField(fieldSpec)
            .addMethods(methodList)
            .build();

        // Write file
        JavaFile.builder(packageName, typeSpec).build().writeTo(filer);
    }
}
