package com.http.compiler;


import httpsender.wrapper.annotation.Parser;
import com.squareup.javapoet.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class ParserAnnotatedClass {

    private Map<String, TypeElement> mElementMap;

    public ParserAnnotatedClass() {
        mElementMap = new LinkedHashMap<>();
    }

    public void add(TypeElement typeElement) {
        Parser annotation = typeElement.getAnnotation(Parser.class);
        String name = annotation.name();
        if (name.length() == 0) {
            throw new IllegalArgumentException(
                    String.format("methodName() in @%s for class %s is null or empty! that's not allowed",
                            Parser.class.getSimpleName(), typeElement.getQualifiedName().toString()));
        }
        mElementMap.put(name, typeElement);
    }

    public List<MethodSpec> getMethodList() {
        TypeVariableName t = TypeVariableName.get("T");
        ClassName responseName = ClassName.get("okhttp3", "Response");
        ClassName httpSenderName = ClassName.get("httpsender", "HttpSender");
        ClassName observableName = ClassName.get("io.reactivex", "Observable");
        ClassName parserName = ClassName.get("httpsender.wrapper.parse", "Parser");
        ClassName progressName = ClassName.get("httpsender.wrapper.entity", "Progress");
        ClassName simpleParserName = ClassName.get("httpsender.wrapper.parse", "SimpleParser");

        TypeName typeName = TypeName.get(String.class);
        TypeName classTName = ParameterizedTypeName.get(ClassName.get(Class.class), t);
        TypeName progressTName = ParameterizedTypeName.get(progressName, t);
        TypeName progressStringName = ParameterizedTypeName.get(progressName, typeName);
        TypeName observableTName = ParameterizedTypeName.get(observableName, t);
        TypeName observableStringName = ParameterizedTypeName.get(observableName, typeName);
        TypeName observableProgressTName = ParameterizedTypeName.get(observableName, progressTName);
        TypeName observableProgressStringName = ParameterizedTypeName.get(observableName, progressStringName);
        TypeName parserTName = ParameterizedTypeName.get(parserName, t);

        List<MethodSpec> methodList = new ArrayList<>();
        MethodSpec.Builder method;
        method = MethodSpec.methodBuilder("execute")
                .addModifiers(Modifier.PUBLIC)
                .addException(IOException.class)
                .addStatement("return $T.execute(param)", httpSenderName)
                .returns(responseName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("execute")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addException(IOException.class)
                .addParameter(parserTName, "parser")
                .addStatement("return $T.execute(param,parser)", httpSenderName)
                .returns(t);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("from")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return $T.from(param)", httpSenderName)
                .returns(observableStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("from")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(parserTName, "parser")
                .addStatement("return $T.from(param,parser)", httpSenderName)
                .returns(observableTName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromSimpleParser")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(classTName, "type")
                .addStatement("return $T.from(param,$T.get(type))", httpSenderName, simpleParserName)
                .returns(observableTName);
        methodList.add(method.build());

        for (Entry<String, TypeElement> item : mElementMap.entrySet()) {
            method = MethodSpec.methodBuilder("from" + item.getKey())
                    .addModifiers(Modifier.PUBLIC)
                    .addTypeVariable(t)
                    .addParameter(classTName, "type")
                    .addStatement("return $T.from(param,$T.get(type))", httpSenderName, ClassName.get(item.getValue()))
                    .returns(observableTName);
            methodList.add(method.build());
        }

        method = MethodSpec.methodBuilder("syncFrom")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(parserTName, "parser")
                .addStatement("return $T.syncFrom(param,parser)", httpSenderName)
                .returns(observableTName);
        methodList.add(method.build());


        method = MethodSpec.methodBuilder("download")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "destPath")
                .addStatement("return $T.download(param,destPath)", httpSenderName)
                .returns(observableProgressStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("upload")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(parserTName, "parser")
                .addStatement("return $T.upload(param,parser)", httpSenderName)
                .returns(observableProgressTName);
        methodList.add(method.build());

        return methodList;
    }


}
