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
        ClassName bitmapName = ClassName.get("android.graphics", "Bitmap");
        ClassName httpSenderName = ClassName.get("rxhttp", "HttpSender");
        ClassName schedulerName = ClassName.get("io.reactivex", "Scheduler");
        ClassName observableName = ClassName.get("io.reactivex", "Observable");
        ClassName parserName = ClassName.get("rxhttp.wrapper.parse", "Parser");
        ClassName progressName = ClassName.get("rxhttp.wrapper.entity", "Progress");
        ClassName simpleParserName = ClassName.get("rxhttp.wrapper.parse", "SimpleParser");
        ClassName listParserName = ClassName.get("rxhttp.wrapper.parse", "ListParser");
        ClassName downloadParserName = ClassName.get("rxhttp.wrapper.parse", "DownloadParser");
        ClassName bitmapParserName = ClassName.get("rxhttp.wrapper.parse", "BitmapParser");

        TypeName typeName = TypeName.get(String.class);
        TypeName classTName = ParameterizedTypeName.get(ClassName.get(Class.class), t);
        TypeName listTName = ParameterizedTypeName.get(ClassName.get(List.class), t);
        TypeName progressTName = ParameterizedTypeName.get(progressName, t);
        TypeName progressStringName = ParameterizedTypeName.get(progressName, typeName);
        TypeName observableTName = ParameterizedTypeName.get(observableName, t);
        TypeName observableListTName = ParameterizedTypeName.get(observableName, listTName);
        TypeName observableBitmapName = ParameterizedTypeName.get(observableName, bitmapName);
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

        method = MethodSpec.methodBuilder("subscribeOn")
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("subscribeOnXX 系列方法需要在fromXXX方法前调用，否则无效")
            .addParameter(schedulerName, "scheduler")
            .addStatement("this.scheduler=scheduler")
            .addStatement("return this")
            .returns(RxHttpGenerator.RXHTTP);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("subscribeOnCurrent")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this.scheduler=null")
            .addStatement("return this")
            .returns(RxHttpGenerator.RXHTTP);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("subscribeOnIo")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this.scheduler=Schedulers.io()")
            .addStatement("return this")
            .returns(RxHttpGenerator.RXHTTP);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("subscribeOnComputation")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this.scheduler=Schedulers.computation()")
            .addStatement("return this")
            .returns(RxHttpGenerator.RXHTTP);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("subscribeOnNewThread")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this.scheduler=Schedulers.newThread()")
            .addStatement("return this")
            .returns(RxHttpGenerator.RXHTTP);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("subscribeOnSingle")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this.scheduler=Schedulers.single()")
            .addStatement("return this")
            .returns(RxHttpGenerator.RXHTTP);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("subscribeOnTrampoline")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this.scheduler=Schedulers.trampoline()")
            .addStatement("return this")
            .returns(RxHttpGenerator.RXHTTP);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asParser")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addParameter(parserTName, "parser")
            .addStatement("Observable<T> observable=$T.syncFrom(addDefaultDomainIfAbsent(param),parser)", httpSenderName)
            .beginControlFlow("if(scheduler!=null)")
            .addStatement("observable=observable.subscribeOn(scheduler)")
            .endControlFlow()
            .addStatement("return observable")
            .returns(observableTName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asObject")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addParameter(classTName, "type")
            .addStatement("return asParser($T.get(type))", simpleParserName)
            .returns(observableTName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asBitmap")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addStatement("return asParser(new $T())", bitmapParserName)
            .returns(observableBitmapName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asString")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return asObject(String.class)")
            .returns(observableStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asBoolean")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return asObject(Boolean.class)")
            .returns(observableBooleanName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asByte")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return asObject(Byte.class)")
            .returns(observableByteName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asShort")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return asObject(Short.class)")
            .returns(observableShortName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asInteger")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return asObject(Integer.class)")
            .returns(observableIntegerName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asLong")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return asObject(Long.class)")
            .returns(observableLongName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asFloat")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return asObject(Float.class)")
            .returns(observableFloatName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asDouble")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return asObject(Double.class)")
            .returns(observableDoubleName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asList")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addParameter(classTName, "type")
            .addStatement("return asParser($T.get(type))", listParserName)
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
            method = MethodSpec.methodBuilder("as" + item.getKey())
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(classTName, "type")
                .addStatement("return asParser($T.get(type))", ClassName.get(item.getValue()))
                .returns(ParameterizedTypeName.get(observableName, TypeName.get(returnType)));
            methodList.add(method.build());
        }


        method = MethodSpec.methodBuilder("from")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addAnnotation(Deprecated.class)
            .addParameter(parserTName, "parser")
            .addStatement("Observable<T> observable=$T.syncFrom(addDefaultDomainIfAbsent(param),parser)", httpSenderName)
            .beginControlFlow("if(scheduler!=null)")
            .addStatement("observable=observable.subscribeOn(scheduler)")
            .endControlFlow()
            .addStatement("return observable")
            .returns(observableTName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("from")
            .addAnnotation(Deprecated.class)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return fromSimpleParser(String.class)")
            .returns(observableStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromBoolean")
            .addAnnotation(Deprecated.class)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return fromSimpleParser(Boolean.class)")
            .returns(observableBooleanName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromByte")
            .addAnnotation(Deprecated.class)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return fromSimpleParser(Byte.class)")
            .returns(observableByteName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromShort")
            .addAnnotation(Deprecated.class)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return fromSimpleParser(Short.class)")
            .returns(observableShortName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromInteger")
            .addAnnotation(Deprecated.class)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return fromSimpleParser(Integer.class)")
            .returns(observableIntegerName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromLong")
            .addAnnotation(Deprecated.class)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return fromSimpleParser(Long.class)")
            .returns(observableLongName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromFloat")
            .addAnnotation(Deprecated.class)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return fromSimpleParser(Float.class)")
            .returns(observableFloatName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromDouble")
            .addAnnotation(Deprecated.class)
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return fromSimpleParser(Double.class)")
            .returns(observableDoubleName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromSimpleParser")
            .addAnnotation(Deprecated.class)
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addParameter(classTName, "type")
            .addStatement("return from($T.get(type))", simpleParserName)
            .returns(observableTName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("fromListParser")
            .addAnnotation(Deprecated.class)
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
                .addAnnotation(Deprecated.class)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(classTName, "type")
                .addStatement("return from($T.get(type))", ClassName.get(item.getValue()))
                .returns(ParameterizedTypeName.get(observableName, TypeName.get(returnType)));
            methodList.add(method.build());
        }

        method = MethodSpec.methodBuilder("syncFrom")
            .addAnnotation(Deprecated.class)
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
            .addStatement("return from(new $T(destPath))", downloadParserName)
            .returns(observableStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("downloadProgress")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "destPath")
            .addStatement("return downloadProgress(destPath,0)")
            .returns(observableProgressStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("downloadProgress")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "destPath")
            .addParameter(long.class, "offsetSize")
            .addStatement("return $T.downloadProgress(addDefaultDomainIfAbsent(param),destPath,offsetSize,scheduler)", httpSenderName)
            .returns(observableProgressStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("uploadProgress")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addStatement("return uploadProgress(SimpleParser.get(String.class))")
            .returns(observableProgressStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("uploadProgress")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addParameter(parserTName, "parser")
            .addStatement("return $T.uploadProgress(addDefaultDomainIfAbsent(param),parser,scheduler)", httpSenderName)
            .returns(observableProgressTName);
        methodList.add(method.build());

        return methodList;
    }


}
