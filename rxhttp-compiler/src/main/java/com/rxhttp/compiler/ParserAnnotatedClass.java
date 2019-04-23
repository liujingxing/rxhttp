package com.rxhttp.compiler;


import com.squareup.javapoet.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import rxhttp.wrapper.annotation.Parser;


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
        ClassName httpSenderName = ClassName.get("rxhttp", "HttpSender");
        ClassName observableName = ClassName.get("io.reactivex", "Observable");
        ClassName parserName = ClassName.get("rxhttp.wrapper.parse", "Parser");
        ClassName progressName = ClassName.get("rxhttp.wrapper.entity", "Progress");
        ClassName simpleParserName = ClassName.get("rxhttp.wrapper.parse", "SimpleParser");
        ClassName listParserName = ClassName.get("rxhttp.wrapper.parse", "ListParser");

        TypeName typeName = TypeName.get(String.class);
        TypeName classTName = ParameterizedTypeName.get(ClassName.get(Class.class), t);
        TypeName listTName = ParameterizedTypeName.get(ClassName.get(List.class), t);
        TypeName progressTName = ParameterizedTypeName.get(progressName, t);
        TypeName progressStringName = ParameterizedTypeName.get(progressName, typeName);
        TypeName observableTName = ParameterizedTypeName.get(observableName, t);
        TypeName observableListTName = ParameterizedTypeName.get(observableName, listTName);
        TypeName observableStringName = ParameterizedTypeName.get(observableName, typeName);
        TypeName observableBooleanName = ParameterizedTypeName.get(observableName, TypeName.get(Boolean.class));
        TypeName observableByteName = ParameterizedTypeName.get(observableName, TypeName.get(Byte.class));
        TypeName observableShortName = ParameterizedTypeName.get(observableName, TypeName.get(Short.class));
        TypeName observableIntegerName = ParameterizedTypeName.get(observableName, TypeName.get(Integer.class));
        TypeName observableLongName = ParameterizedTypeName.get(observableName, TypeName.get(Long.class));
        TypeName observableFloatName = ParameterizedTypeName.get(observableName, TypeName.get(Float.class));
        TypeName observableDoubleName = ParameterizedTypeName.get(observableName, TypeName.get(Double.class));
        TypeName observableProgressTName = ParameterizedTypeName.get(observableName, progressTName);
        TypeName observableProgressStringName = ParameterizedTypeName.get(observableName, progressStringName);
        TypeName parserTName = ParameterizedTypeName.get(parserName, t);

        List<MethodSpec> methodList = new ArrayList<>();
        MethodSpec.Builder method;
        method = MethodSpec.methodBuilder("execute")
                .addModifiers(Modifier.PUBLIC)
                .addException(IOException.class)
                .addStatement("return $T.execute(addDefaultDomainIfAbsent(param))", httpSenderName)
                .returns(responseName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("execute")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addException(IOException.class)
                .addParameter(parserTName, "parser")
                .addStatement("return $T.execute(addDefaultDomainIfAbsent(param),parser)", httpSenderName)
                .returns(t);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("from")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return fromSimpleParser(String.class)")
                .returns(observableStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromBoolean")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return fromSimpleParser(Boolean.class)")
                .returns(observableBooleanName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromByte")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return fromSimpleParser(Byte.class)")
                .returns(observableByteName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromShort")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return fromSimpleParser(Short.class)")
                .returns(observableShortName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromInteger")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return fromSimpleParser(Integer.class)")
                .returns(observableIntegerName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromLong")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return fromSimpleParser(Long.class)")
                .returns(observableLongName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromFloat")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return fromSimpleParser(Float.class)")
                .returns(observableFloatName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromDouble")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return fromSimpleParser(Double.class)")
                .returns(observableDoubleName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromSimpleParser")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(classTName, "type")
                .addStatement("return from($T.get(type))", simpleParserName)
                .returns(observableTName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromListParser")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(classTName, "type")
                .addStatement("return from($T.get(type))", listParserName)
                .returns(observableListTName);
        methodList.add(method.build());

        for (Entry<String, TypeElement> item : mElementMap.entrySet()) {
            TypeMirror returnType = null; //获取onParse方法的返回类型
            TypeElement typeElement = item.getValue();
            for (Element element : typeElement.getEnclosedElements()) {
                if (!(element instanceof ExecutableElement)) continue;
                if (!element.getModifiers().contains(Modifier.PUBLIC)
                        || element.getModifiers().contains(Modifier.STATIC)) continue;
                ExecutableElement executableElement = (ExecutableElement) element;
                if (executableElement.getSimpleName().toString().equals("onParse")
                        && executableElement.getParameters().size() == 1
                        && executableElement.getParameters().get(0).asType().toString().equals("okhttp3.Response")) {
                    returnType = executableElement.getReturnType();
                    break;
                }
            }
            if (returnType == null) continue;
            method = MethodSpec.methodBuilder("from" + item.getKey())
                    .addModifiers(Modifier.PUBLIC)
                    .addTypeVariable(t)
                    .addParameter(classTName, "type")
                    .addStatement("return from($T.get(type))", ClassName.get(item.getValue()))
                    .returns(ParameterizedTypeName.get(observableName, TypeName.get(returnType)));
            methodList.add(method.build());
        }

        method = MethodSpec.methodBuilder("from")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(parserTName, "parser")
                .addStatement("return $T.from(addDefaultDomainIfAbsent(param),parser)", httpSenderName)
                .returns(observableTName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("syncFrom")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(parserTName, "parser")
                .addStatement("return $T.syncFrom(addDefaultDomainIfAbsent(param),parser)", httpSenderName)
                .returns(observableTName);
        methodList.add(method.build());


        method = MethodSpec.methodBuilder("download")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(String.class, "destPath")
                .addStatement("return $T.download(addDefaultDomainIfAbsent(param),destPath)", httpSenderName)
                .returns(observableStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("downloadProgress")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "destPath")
                .addStatement("return $T.downloadProgress(addDefaultDomainIfAbsent(param),destPath)", httpSenderName)
                .returns(observableProgressStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("uploadProgress")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addStatement("return $T.uploadProgress(addDefaultDomainIfAbsent(param))", httpSenderName)
                .returns(observableProgressStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("uploadProgress")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(parserTName, "parser")
                .addStatement("return $T.uploadProgress(addDefaultDomainIfAbsent(param),parser)", httpSenderName)
                .returns(observableProgressTName);
        methodList.add(method.build());

        return methodList;
    }


}
