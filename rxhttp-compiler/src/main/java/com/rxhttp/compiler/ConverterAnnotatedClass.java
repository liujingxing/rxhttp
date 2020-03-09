package com.rxhttp.compiler;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

import rxhttp.wrapper.annotation.Converter;

public class ConverterAnnotatedClass {

    private Map<String, VariableElement> mElementMap;

    public ConverterAnnotatedClass() {
        mElementMap = new LinkedHashMap<>();
    }

    public void add(VariableElement variableElement) {
        Converter annotation = variableElement.getAnnotation(Converter.class);
        String name = annotation.name();
        if (name.length() <= 0) {
            name = variableElement.getSimpleName().toString();
        }
        mElementMap.put(name, variableElement);
    }

    public List<MethodSpec> getMethodList() {
        ClassName paramName = ClassName.get(RxHttpGenerator.packageName, "Param");

        List<MethodSpec> methodList = new ArrayList<>();
        MethodSpec.Builder method;
        for (Entry<String, VariableElement> item : mElementMap.entrySet()) {
            method = MethodSpec.methodBuilder("set" + item.getKey())
                .addModifiers(Modifier.PUBLIC)
                .addStatement("if ($T.$L == null)\n" +
                        "throw new IllegalArgumentException(\"converter can not be null\");",
                    ClassName.get(item.getValue().getEnclosingElement().asType()),
                    item.getValue().getSimpleName().toString())
                .addStatement("this.converter = $T.$L",
                    ClassName.get(item.getValue().getEnclosingElement().asType()),
                    item.getValue().getSimpleName().toString())
                .addStatement("return (R)this")
                .returns(RxHttpGenerator.r);
            methodList.add(method.build());
        }

        method = MethodSpec.methodBuilder("setConverter")
            .addJavadoc("给Param设置转换器，此方法会在请求发起前，被RxHttp内部调用\n")
            .addModifiers(Modifier.PRIVATE)
            .addParameter(RxHttpGenerator.p, "param")
            .addStatement("param.tag(IConverter.class,converter)")
            .addStatement("return (R)this")
            .returns(RxHttpGenerator.r);
        methodList.add(method.build());
        return methodList;
    }
}
