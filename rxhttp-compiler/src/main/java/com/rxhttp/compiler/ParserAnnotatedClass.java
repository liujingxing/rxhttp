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

        method = MethodSpec.methodBuilder("fromBoolean")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return $T.from(param,SimpleParser.get(Boolean.class))", httpSenderName)
                .returns(observableBooleanName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromByte")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return $T.from(param,SimpleParser.get(Byte.class))", httpSenderName)
                .returns(observableByteName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromShort")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return $T.from(param,SimpleParser.get(Short.class))", httpSenderName)
                .returns(observableShortName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromInteger")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return $T.from(param,SimpleParser.get(Integer.class))", httpSenderName)
                .returns(observableIntegerName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromLong")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return $T.from(param,SimpleParser.get(Long.class))", httpSenderName)
                .returns(observableLongName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromFloat")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return $T.from(param,SimpleParser.get(Float.class))", httpSenderName)
                .returns(observableFloatName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromDouble")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return $T.from(param,SimpleParser.get(Double.class))", httpSenderName)
                .returns(observableDoubleName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromSimpleParser")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(classTName, "type")
                .addStatement("return $T.from(param,$T.get(type))", httpSenderName, simpleParserName)
                .returns(observableTName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromListParser")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(classTName, "type")
                .addStatement("return $T.from(param,$T.get(type))", httpSenderName, listParserName)
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
                    .addStatement("return $T.from(param,$T.get(type))", httpSenderName, ClassName.get(item.getValue()))
                    .returns(ParameterizedTypeName.get(observableName, TypeName.get(returnType)));
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
