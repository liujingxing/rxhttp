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

import rxhttp.wrapper.annotation.Domain;

public class DomainAnnotatedClass {

    private Map<String, VariableElement> mElementMap;

    public DomainAnnotatedClass() {
        mElementMap = new LinkedHashMap<>();
    }

    public void add(VariableElement variableElement) {
        Domain annotation = variableElement.getAnnotation(Domain.class);
        String name = annotation.name();
        if (name.length() <= 0) {
            name = variableElement.getSimpleName().toString();
        }
        mElementMap.put(name, variableElement);
    }

    public List<MethodSpec> getMethodList() {
        List<MethodSpec> methodList = new ArrayList<>();
        MethodSpec.Builder method;
        for (Entry<String, VariableElement> item : mElementMap.entrySet()) {
            method = MethodSpec.methodBuilder("setDomainTo" + item.getKey() + "IfAbsent")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("String newUrl = addDomainIfAbsent(param.getSimpleUrl(), $T.$L)",
                    ClassName.get(item.getValue().getEnclosingElement().asType()),
                    item.getValue().getSimpleName().toString())
                .addStatement("param.setUrl(newUrl)")
                .addStatement("return (R)this")
                .returns(RxHttpGenerator.r);
            methodList.add(method.build());
        }

        //对url添加域名方法
        method = MethodSpec.methodBuilder("addDomainIfAbsent")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .addParameter(String.class, "url")
            .addParameter(String.class, "domain")
            .addCode("if (url.startsWith(\"http\")) return url;\n" +
                "if (url.startsWith(\"/\")) {\n" +
                "    if (domain.endsWith(\"/\"))\n" +
                "        return domain + url.substring(1);\n" +
                "    else\n" +
                "        return domain + url;\n" +
                "} else if (domain.endsWith(\"/\")) {\n" +
                "    return domain + url;\n" +
                "} else {\n" +
                "    return domain + \"/\" + url;\n" +
                "}")
            .returns(String.class);
        methodList.add(method.build());
        return methodList;
    }
}
