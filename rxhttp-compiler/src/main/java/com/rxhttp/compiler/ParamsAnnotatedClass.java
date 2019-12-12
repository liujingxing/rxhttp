package com.rxhttp.compiler;


import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import rxhttp.wrapper.annotation.Param;

public class ParamsAnnotatedClass {

    private Map<String, TypeElement> mElementMap;

    public ParamsAnnotatedClass() {
        mElementMap = new LinkedHashMap<>();
    }

    public void add(TypeElement typeElement) {
        Param annotation = typeElement.getAnnotation(Param.class);
        String name = annotation.methodName();
        if (name.length() == 0) {
            throw new IllegalArgumentException(
                String.format("methodName() in @%s for class %s is null or empty! that's not allowed",
                    Param.class.getSimpleName(), typeElement.getQualifiedName().toString()));
        }
        mElementMap.put(name, typeElement);
    }

    public List<MethodSpec> getMethodList(Filer filer) throws IOException {
        TypeVariableName rxHttp = RxHttpGenerator.r;
        ClassName headerName = ClassName.get("okhttp3", "Headers");
        ClassName headerBuilderName = ClassName.get("okhttp3", "Headers.Builder");
        ClassName requestName = ClassName.get("okhttp3", "Request");
        ClassName cacheControlName = ClassName.get("okhttp3", "CacheControl");
        ClassName progressCallbackName = ClassName.get("rxhttp.wrapper.callback", "ProgressCallback");

        ClassName paramName = ClassName.get(RxHttpGenerator.packageName, "Param");
        ClassName noBodyParamName = ClassName.get(RxHttpGenerator.packageName, "NoBodyParam");
        ClassName formParamName = ClassName.get(RxHttpGenerator.packageName, "FormParam");
        ClassName jsonParamName = ClassName.get(RxHttpGenerator.packageName, "JsonParam");
        ClassName jsonArrayParamName = ClassName.get(RxHttpGenerator.packageName, "JsonArrayParam");



        List<MethodSpec> methodList = new ArrayList<>();
        Map<String, String> methodMap = new LinkedHashMap<>();
        methodMap.put("get", "RxHttp$NoBodyParam");
        methodMap.put("head", "RxHttp$NoBodyParam");
        methodMap.put("postForm", "RxHttp$FormParam");
        methodMap.put("putForm", "RxHttp$FormParam");
        methodMap.put("patchForm", "RxHttp$FormParam");
        methodMap.put("deleteForm", "RxHttp$FormParam");
        methodMap.put("postJson", "RxHttp$JsonParam");
        methodMap.put("putJson", "RxHttp$JsonParam");
        methodMap.put("patchJson", "RxHttp$JsonParam");
        methodMap.put("deleteJson", "RxHttp$JsonParam");
        methodMap.put("postJsonArray", "RxHttp$JsonArrayParam");
        methodMap.put("putJsonArray", "RxHttp$JsonArrayParam");
        methodMap.put("patchJsonArray", "RxHttp$JsonArrayParam");
        methodMap.put("deleteJsonArray", "RxHttp$JsonArrayParam");

        MethodSpec.Builder method;
        for (Map.Entry<String, String> map : methodMap.entrySet()) {
            method = MethodSpec.methodBuilder(map.getKey())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String.class, "url")
                .addParameter(ArrayTypeName.of(Object.class), "formatArgs")
                .varargs()
                .addStatement("return with($T.$L(format(url, formatArgs)))", paramName, map.getKey())
                .returns(ClassName.get(RxHttpGenerator.packageName, map.getValue()));
            methodList.add(method.build());
        }

        for (Entry<String, TypeElement> item : mElementMap.entrySet()) {
            TypeElement typeElement = item.getValue();
            ClassName param = ClassName.get(typeElement);
            String rxHttpName = "RxHttp$" + typeElement.getSimpleName();
            ClassName rxHttp$ParamName = ClassName.get(RxHttpGenerator.packageName, rxHttpName);
            method = MethodSpec.methodBuilder(item.getKey())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String.class, "url")
                .addParameter(ArrayTypeName.of(Object.class), "formatArgs")
                .varargs()
                .addStatement("return new $T(new $T(format(url, formatArgs)))", rxHttp$ParamName, param)
                .returns(rxHttp$ParamName);
            methodList.add(method.build());

            TypeMirror superclass = typeElement.getSuperclass();
            TypeName rxHttp$Param;
            String prefix = "((" + param.simpleName() + ")param).";
            switch (superclass.toString()) {
                case "rxhttp.wrapper.param.FormParam":
                    rxHttp$Param = ClassName.get(RxHttpGenerator.packageName, "RxHttp$FormParam");
                    break;
                case "rxhttp.wrapper.param.JsonParam":
                    rxHttp$Param = ClassName.get(RxHttpGenerator.packageName, "RxHttp$JsonParam");
                    break;
                case "rxhttp.wrapper.param.NoBodyParam":
                    rxHttp$Param = ClassName.get(RxHttpGenerator.packageName, "RxHttp$NoBodyParam");
                    break;
                default:
                    prefix = "param.";
                    rxHttp$Param = ParameterizedTypeName.get(RxHttpGenerator.RXHTTP, param, rxHttp$ParamName);
                    break;
            }

            List<MethodSpec> rxHttp$PostEncryptFormParamMethod = new ArrayList<>();

            method = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(param, "param")
                .addStatement("super(param)");
            rxHttp$PostEncryptFormParamMethod.add(method.build());

            for (Element enclosedElement : typeElement.getEnclosedElements()) {
                if (!(enclosedElement instanceof ExecutableElement)) continue;
                if (!enclosedElement.getModifiers().contains(Modifier.PUBLIC))
                    continue; //过滤非public修饰符
                if (enclosedElement.getKind() != ElementKind.METHOD)
                    continue; //过滤非方法，
                if (enclosedElement.getAnnotation(Override.class) != null)
                    continue; //过滤重写的方法
                TypeMirror returnTypeMirror = ((ExecutableElement) enclosedElement).getReturnType();

                TypeName returnType = TypeName.get(returnTypeMirror);
                if (returnType.toString().equals(param.toString())) {
                    returnType = rxHttp$ParamName;
                }
                List<ParameterSpec> parameterSpecs = new ArrayList<>();
                StringBuilder methodBuilder = new StringBuilder()
                    .append(enclosedElement.getSimpleName().toString())
                    .append("(");
                List<? extends VariableElement> parameters = ((ExecutableElement) enclosedElement).getParameters();
                for (VariableElement element : parameters) {
                    ParameterSpec parameterSpec = ParameterSpec.get(element);
                    parameterSpecs.add(parameterSpec);
                    methodBuilder.append(parameterSpec.name).append(",");
                }
                if (methodBuilder.toString().endsWith(",")) {
                    methodBuilder.deleteCharAt(methodBuilder.length() - 1);
                }
                methodBuilder.append(")");

                method = MethodSpec.methodBuilder(enclosedElement.getSimpleName().toString())
                    .addModifiers(enclosedElement.getModifiers())
                    .addParameters(parameterSpecs);

                if (returnType == rxHttp$ParamName) {
                    method.addStatement(prefix + methodBuilder, param)
                        .addStatement("return this");
                } else if (returnType.toString().equals("void")) {
                    method.addStatement(prefix + methodBuilder);
                } else {
                    method.addStatement("return " + prefix + methodBuilder, param);
                }
                method.returns(returnType);
                rxHttp$PostEncryptFormParamMethod.add(method.build());

            }

            TypeSpec rxHttpPostEncryptFormParamSpec = TypeSpec.classBuilder(rxHttpName)
                .addJavadoc("Github" +
                    "\nhttps://github.com/liujingxing/RxHttp" +
                    "\nhttps://github.com/liujingxing/RxLife\n")
                .addModifiers(Modifier.PUBLIC)
                .superclass(rxHttp$Param)
                .addMethods(rxHttp$PostEncryptFormParamMethod)
                .build();

            JavaFile.builder(RxHttpGenerator.packageName, rxHttpPostEncryptFormParamSpec)
                .build().writeTo(filer);
        }

        method = MethodSpec.methodBuilder("with")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(noBodyParamName, "noBodyParam")
            .addStatement("return new $L(noBodyParam)", "RxHttp$NoBodyParam")
            .returns(ClassName.get(RxHttpGenerator.packageName, "RxHttp$NoBodyParam"));
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("with")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(formParamName, "formParam")
            .addStatement("return new $L(formParam)", "RxHttp$FormParam")
            .returns(ClassName.get(RxHttpGenerator.packageName, "RxHttp$FormParam"));
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("with")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(jsonParamName, "jsonParam")
            .addStatement("return new $L(jsonParam)", "RxHttp$JsonParam")
            .returns(ClassName.get(RxHttpGenerator.packageName, "RxHttp$JsonParam"));
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("with")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(jsonArrayParamName, "jsonArrayParam")
            .addStatement("return new $L(jsonArrayParam)", "RxHttp$JsonArrayParam")
            .returns(ClassName.get(RxHttpGenerator.packageName, "RxHttp$JsonArrayParam"));
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setUrl")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "url")
            .addStatement("param.setUrl(url)")
            .addStatement("return (R)this")
            .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "line")
            .addStatement("param.addHeader(line)")
            .addStatement("return (R)this")
            .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "line")
            .addParameter(boolean.class, "isAdd")
            .beginControlFlow("if(isAdd)")
            .addStatement("param.addHeader(line)")
            .endControlFlow()
            .addStatement("return (R)this")
            .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(String.class, "value")
            .addStatement("param.addHeader(key,value)")
            .addStatement("return (R)this")
            .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(String.class, "value")
            .addParameter(boolean.class, "isAdd")
            .beginControlFlow("if(isAdd)")
            .addStatement("param.addHeader(key,value)")
            .endControlFlow()
            .addStatement("return (R)this")
            .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addParameter(String.class, "value")
            .addStatement("param.setHeader(key,value)")
            .addStatement("return (R)this")
            .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setRangeHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(long.class, "startIndex")
            .addStatement("param.setRangeHeader(startIndex)")
            .addStatement("return (R)this")
            .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setRangeHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(long.class, "startIndex")
            .addParameter(long.class, "endIndex")
            .addStatement("param.setRangeHeader(startIndex,endIndex)")
            .addStatement("return (R)this")
            .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("removeAllHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addStatement("param.removeAllHeader(key)")
            .addStatement("return (R)this")
            .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setHeadersBuilder")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(headerBuilderName, "builder")
            .addStatement("param.setHeadersBuilder(builder)")
            .addStatement("return (R)this")
            .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setAssemblyEnabled")
            .addJavadoc("设置单个接口是否需要添加公共参数," +
                "\n即是否回调通过{@link #setOnParamAssembly(Function)}方法设置的接口,默认为true\n")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(boolean.class, "enabled")
            .addStatement("param.setAssemblyEnabled(enabled)")
            .addStatement("return (R)this")
            .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setConverterEnabled")
            .addAnnotation(Deprecated.class)
            .addJavadoc("@deprecated please user {@link #setDecoderEnabled(boolean)} instead\n")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(boolean.class, "enabled")
            .addStatement("return setDecoderEnabled(enabled)")
            .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setDecoderEnabled")
            .addJavadoc("设置单个接口是否需要对Http返回的数据进行解码/解密," +
                "\n即是否回调通过{@link #setResultDecoder(Function)}方法设置的接口,默认为true\n")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(boolean.class, "enabled")
            .addStatement("param.addHeader($T.DATA_DECRYPT,String.valueOf(enabled))", paramName)
            .addStatement("return (R)this")
            .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("isAssemblyEnabled")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return param.isAssemblyEnabled()")
            .returns(boolean.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("getUrl")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return param.getUrl()")
            .returns(String.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("getSimpleUrl")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return param.getSimpleUrl()")
            .returns(String.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("getHeader")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "key")
            .addStatement("return param.getHeader(key)")
            .returns(String.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("getHeaders")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return param.getHeaders()")
            .returns(headerName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("getHeadersBuilder")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return param.getHeadersBuilder()")
            .returns(headerBuilderName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("buildRequest")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return param.buildRequest()")
            .returns(requestName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("tag")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Object.class, "tag")
            .addStatement("param.tag(tag)")
            .addStatement("return (R)this")
            .returns(rxHttp);
        methodList.add(method.build());

        TypeVariableName t = TypeVariableName.get("T");
        WildcardTypeName subString = WildcardTypeName.subtypeOf(t);
        TypeName classTName = ParameterizedTypeName.get(ClassName.get(Class.class), subString);

        method = MethodSpec.methodBuilder("tag")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addParameter(classTName, "type")
            .addParameter(Object.class, "tag")
            .addStatement("param.tag(type,tag)")
            .addStatement("return (R)this")
            .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("cacheControl")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(cacheControlName, "cacheControl")
            .addStatement("param.cacheControl(cacheControl)")
            .addStatement("return (R)this")
            .returns(rxHttp);
        methodList.add(method.build());
        return methodList;
    }

}
