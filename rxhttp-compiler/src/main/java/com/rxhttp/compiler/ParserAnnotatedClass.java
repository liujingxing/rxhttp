package com.rxhttp.compiler;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

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
        TypeVariableName k = TypeVariableName.get("K");
        TypeVariableName v = TypeVariableName.get("V");
        ClassName responseName = ClassName.get("okhttp3", "Response");
        ClassName schedulerName = ClassName.get("io.reactivex", "Scheduler");
        ClassName observableName = ClassName.get("io.reactivex", "Observable");
        ClassName consumerName = ClassName.get("io.reactivex.functions", "Consumer");
        ClassName androidSchedulersName = ClassName.get("io.reactivex.android.schedulers", "AndroidSchedulers");

        ClassName bitmapName = ClassName.get("android.graphics", "Bitmap");
        ClassName httpSenderName = ClassName.get("rxhttp", "HttpSender");
        ClassName parserName = ClassName.get("rxhttp.wrapper.parse", "Parser");
        ClassName progressName = ClassName.get("rxhttp.wrapper.entity", "Progress");
        ClassName simpleParserName = ClassName.get("rxhttp.wrapper.parse", "SimpleParser");
        ClassName mapParserName = ClassName.get("rxhttp.wrapper.parse", "MapParser");
        ClassName listParserName = ClassName.get("rxhttp.wrapper.parse", "ListParser");
        ClassName downloadParserName = ClassName.get("rxhttp.wrapper.parse", "DownloadParser");
        ClassName bitmapParserName = ClassName.get("rxhttp.wrapper.parse", "BitmapParser");

        TypeName typeName = TypeName.get(String.class);
        TypeName classTName = ParameterizedTypeName.get(ClassName.get(Class.class), t);
        TypeName classKName = ParameterizedTypeName.get(ClassName.get(Class.class), k);
        TypeName classVName = ParameterizedTypeName.get(ClassName.get(Class.class), v);
        TypeName listTName = ParameterizedTypeName.get(ClassName.get(List.class), t);
        TypeName mapTTName = ParameterizedTypeName.get(ClassName.get(Map.class), t, t);
        TypeName mapKVName = ParameterizedTypeName.get(ClassName.get(Map.class), k, v);
        TypeName progressTName = ParameterizedTypeName.get(progressName, t);
        TypeName progressStringName = ParameterizedTypeName.get(progressName, typeName);
        TypeName observableTName = ParameterizedTypeName.get(observableName, t);
        TypeName observableListTName = ParameterizedTypeName.get(observableName, listTName);
        TypeName observableMapTTName = ParameterizedTypeName.get(observableName, mapTTName);
        TypeName observableMapKVName = ParameterizedTypeName.get(observableName, mapKVName);
        TypeName observableMapName = ParameterizedTypeName.get(observableName, TypeName.get(Map.class));
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
        TypeName consumerProgressStringName = ParameterizedTypeName.get(consumerName, progressStringName);
        TypeName consumerProgressTName = ParameterizedTypeName.get(consumerName, progressTName);
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

        method = MethodSpec.methodBuilder("asMap")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("return asObject(Map.class)")
            .returns(observableMapName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asMap")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addParameter(classTName, "type")
            .addStatement("return asParser($T.get(type,type))", mapParserName)
            .returns(observableMapTTName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asMap")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(k)
            .addTypeVariable(v)
            .addParameter(classKName, "kType")
            .addParameter(classVName, "vType")
            .addStatement("return asParser($T.get(kType,vType))", mapParserName)
            .returns(observableMapKVName);
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
                .addStatement("return asParser(new $T(type))", ClassName.get(item.getValue()))
                .returns(ParameterizedTypeName.get(observableName, TypeName.get(returnType)));
            methodList.add(method.build());
        }

        method = MethodSpec.methodBuilder("asDownload")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addParameter(String.class, "destPath")
            .addStatement("return asParser(new $T(destPath))", downloadParserName)
            .returns(observableStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asDownload")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "destPath")
            .addParameter(consumerProgressStringName, "progressConsumer")
            .addStatement("return asDownload(destPath, 0, progressConsumer, null)")
            .returns(observableStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asDownload")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "destPath")
            .addParameter(consumerProgressStringName, "progressConsumer")
            .addParameter(schedulerName, "observeOnScheduler")
            .addStatement("return asDownload(destPath, 0, progressConsumer, observeOnScheduler)")
            .returns(observableStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asDownload")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "destPath")
            .addParameter(long.class, "offsetSize")
            .addParameter(consumerProgressStringName, "progressConsumer")
            .addStatement("return asDownload(destPath, offsetSize, progressConsumer, null)")
            .returns(observableStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asDownload")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "destPath")
            .addParameter(long.class, "offsetSize")
            .addParameter(consumerProgressStringName, "progressConsumer")
            .addParameter(schedulerName, "observeOnScheduler")
            .addStatement("Observable<Progress<String>> observable = asDownloadProgress(destPath, offsetSize)")
            .beginControlFlow("if(observeOnScheduler != null)")
            .addStatement("observable=observable.observeOn(observeOnScheduler)")
            .endControlFlow()
            .addStatement("return observable.doOnNext(progressConsumer)\n" +
                ".filter(Progress::isCompleted)\n" +
                ".map(Progress::getResult)")
            .returns(observableStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asDownloadProgress")
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("@deprecated please used {@link RxHttp#asDownload(String,Consumer,Scheduler)}")
            .addAnnotation(Deprecated.class)
            .addParameter(String.class, "destPath")
            .addStatement("return asDownloadProgress(destPath,0)")
            .returns(observableProgressStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asDownloadProgress")
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("@deprecated please used {@link RxHttp#asDownload(String,long,Consumer,Scheduler)}")
            .addAnnotation(Deprecated.class)
            .addParameter(String.class, "destPath")
            .addParameter(long.class, "offsetSize")
            .addStatement("return $T.downloadProgress(addDefaultDomainIfAbsent(param),destPath,offsetSize,scheduler)", httpSenderName)
            .returns(observableProgressStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asUpload")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(consumerProgressStringName, "progressConsumer")
            .addStatement("return asUpload(SimpleParser.get(String.class), progressConsumer, null)")
            .returns(observableStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asUpload")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(consumerProgressStringName, "progressConsumer")
            .addParameter(schedulerName, "observeOnScheduler")
            .addStatement("return asUpload(SimpleParser.get(String.class), progressConsumer, observeOnScheduler)")
            .returns(observableStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asUpload")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addParameter(parserTName, "parser")
            .addParameter(consumerProgressTName, "progressConsumer")
            .addParameter(schedulerName, "observeOnScheduler")
            .addStatement("Observable<Progress<T>> observable = asUploadProgress(parser)")
            .beginControlFlow("if(observeOnScheduler != null)")
            .addStatement("observable=observable.observeOn(observeOnScheduler)")
            .endControlFlow()
            .addStatement("return observable.doOnNext(progressConsumer)\n" +
                ".filter(Progress::isCompleted)\n" +
                ".map(Progress::getResult)")
            .returns(observableTName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asUploadProgress")
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("@deprecated please used {@link RxHttp#asUpload(Consumer, Scheduler)}")
            .addAnnotation(Deprecated.class)
            .addStatement("return asUploadProgress(SimpleParser.get(String.class))")
            .returns(observableProgressStringName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asUploadProgress")
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("@deprecated please used {@link RxHttp#asUpload(Parser, Consumer, Scheduler)}")
            .addAnnotation(Deprecated.class)
            .addTypeVariable(t)
            .addParameter(parserTName, "parser")
            .addStatement("return $T.uploadProgress(addDefaultDomainIfAbsent(param), parser, scheduler)", httpSenderName)
            .returns(observableProgressTName);
        methodList.add(method.build());

        return methodList;
    }
}
