package com.rxhttp.compiler;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

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

    public List<MethodSpec> getMethodList() {
        ClassName rxHttp = RxHttpGenerator.RXHTTP;
        ClassName headerName = ClassName.get("okhttp3", "Headers");
        ClassName headerBuilderName = ClassName.get("okhttp3", "Headers.Builder");
        ClassName requestName = ClassName.get("okhttp3", "Request");
        ClassName cacheControlName = ClassName.get("okhttp3", "CacheControl");
        ClassName progressCallbackName = ClassName.get("rxhttp.wrapper.callback", "ProgressCallback");
        ClassName upFileName = ClassName.get("rxhttp.wrapper.entity", "UpFile");
        ClassName paramName = ClassName.get("rxhttp.wrapper.param", "Param");
        TypeName listUpFileName = ParameterizedTypeName.get(ClassName.get(List.class), upFileName);
        TypeName listFileName = ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(File.class));
        List<MethodSpec> methodList = new ArrayList<>();
        Map<String, String> methodMap = new LinkedHashMap<>();
        methodMap.put("get", "GetParam");
        methodMap.put("head", "HeadParam");
        methodMap.put("postForm", "PostFormParam");
        methodMap.put("postJson", "PostJsonParam");
        methodMap.put("putForm", "PutFormParam");
        methodMap.put("putJson", "PutJsonParam");
        methodMap.put("patchForm", "PatchFormParam");
        methodMap.put("patchJson", "PatchJsonParam");
        methodMap.put("deleteForm", "DeleteFormParam");
        methodMap.put("deleteJson", "DeleteJsonParam");

        MethodSpec.Builder method;
        for (Map.Entry<String, String> map : methodMap.entrySet()) {
            method = MethodSpec.methodBuilder(map.getKey())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(String.class, "url")
                    .addStatement("return with($L.with(url))", map.getValue())
                    .returns(rxHttp);
            methodList.add(method.build());
        }

        for (Entry<String,TypeElement> item : mElementMap.entrySet()) {
            method = MethodSpec.methodBuilder(item.getKey())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(String.class, "url")
                    .addStatement("return with(new $T(url))", ClassName.get(item.getValue()))
                    .returns(rxHttp);
            methodList.add(method.build());
        }

        method = MethodSpec.methodBuilder("setUrl")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "url")
                .addStatement("param.setUrl(url)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setJsonParams")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "jsonParams")
                .addStatement("param.setJsonParams(jsonParams)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("add")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(Object.class, "value")
                .addStatement("param.add(key,value)")
                .addStatement("return this")
                .returns(rxHttp);
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
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setUploadMaxLength")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(long.class, "maxLength")
                .addStatement("param.setUploadMaxLength(maxLength)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("add")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(File.class, "file")
                .addStatement("param.add(key,file)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(File.class, "file")
                .addStatement("param.addFile(key,file)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(String.class, "filePath")
                .addStatement("param.addFile(key,filePath)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(String.class, "value")
                .addParameter(String.class, "filePath")
                .addStatement("param.addFile(key,value,filePath)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(String.class, "value")
                .addParameter(File.class, "file")
                .addStatement("param.addFile(key,value,file)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(upFileName, "file")
                .addStatement("param.addFile(file)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(listFileName, "fileList")
                .addStatement("param.addFile(key,fileList)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addFile")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(listUpFileName, "fileList")
                .addStatement("param.addFile(fileList)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("removeFile")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addStatement("param.removeFile(key)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "line")
                .addStatement("param.addHeader(line)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "line")
                .addParameter(boolean.class, "isAdd")
                .beginControlFlow("if(isAdd)")
                .addStatement("param.addHeader(line)")
                .endControlFlow()
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("addHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(String.class, "value")
                .addStatement("param.addHeader(key,value)")
                .addStatement("return this")
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
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addParameter(String.class, "value")
                .addStatement("param.setHeader(key,value)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setRangeHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(long.class, "startIndex")
                .addStatement("param.setRangeHeader(startIndex)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setRangeHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(long.class, "startIndex")
                .addParameter(long.class, "endIndex")
                .addStatement("param.setRangeHeader(startIndex,endIndex)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("removeAllHeader")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "key")
                .addStatement("param.removeAllHeader(key)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setHeadersBuilder")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(headerBuilderName, "builder")
                .addStatement("param.setHeadersBuilder(builder)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setAssemblyEnabled")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(boolean.class, "enabled")
                .addStatement("param.setAssemblyEnabled(enabled)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setConverterEnabled")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(boolean.class, "enabled")
            .addStatement("param.addHeader($T.DATA_DECRYPT,String.valueOf(enabled))", paramName)
            .addStatement("return this")
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
                .addParameter(Object.class,"tag")
                .addStatement("param.tag(tag)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("getTag")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return param.getTag()")
                .returns(Object.class);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("cacheControl")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(cacheControlName,"cacheControl")
                .addStatement("param.cacheControl(cacheControl)")
                .addStatement("return this")
                .returns(rxHttp);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("getCacheControl")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return param.getCacheControl()")
                .returns(cacheControlName);
        methodList.add(method.build());


        return methodList;
    }

}
