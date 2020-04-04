package com.rxhttp.compiler;


import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

import rxhttp.wrapper.annotation.Parser;


public class ParserAnnotatedClass {

    private Map<String, TypeElement> mElementMap;
    private Map<String, List<? extends TypeMirror>> mTypeMap;

    public ParserAnnotatedClass() {
        mElementMap = new LinkedHashMap<>();
        mTypeMap = new LinkedHashMap<>();
    }

    public void add(TypeElement typeElement) {
        Parser annotation = typeElement.getAnnotation(Parser.class);
        String name = annotation.name();
        if (name.length() == 0) {
            throw new IllegalArgumentException(
                String.format("methodName() in @%s for class %s is null or empty! that's not allowed",
                    Parser.class.getSimpleName(), typeElement.getQualifiedName().toString()));
        }
        try {
            annotation.wrappers();
        } catch (MirroredTypesException e) {
            List<? extends TypeMirror> typeMirrors = e.getTypeMirrors();
            mTypeMap.put(name, typeMirrors);
        }
        mElementMap.put(name, typeElement);
    }

    public List<MethodSpec> getMethodList(Filer filer) {
        TypeVariableName t = TypeVariableName.get("T");
        TypeVariableName k = TypeVariableName.get("K");
        TypeVariableName v = TypeVariableName.get("V");
        ClassName callName = ClassName.get("okhttp3", "Call");
        ClassName okHttpClientName = ClassName.get("okhttp3", "OkHttpClient");
        ClassName responseName = ClassName.get("okhttp3", "Response");
        ClassName schedulerName = ClassName.get("io.reactivex", "Scheduler");
        ClassName observableName = ClassName.get("io.reactivex", "Observable");
        ClassName consumerName = ClassName.get("io.reactivex.functions", "Consumer");

        ClassName bitmapName = ClassName.get("android.graphics", "Bitmap");
        ClassName okResponseName = ClassName.get("okhttp3", "Response");
        ClassName headersName = ClassName.get("okhttp3", "Headers");
        ClassName httpSenderName = ClassName.get("rxhttp", "HttpSender");
        ClassName requestName = ClassName.get("okhttp3", "Request");
        ClassName parserName = ClassName.get("rxhttp.wrapper.parse", "Parser");
        ClassName progressName = ClassName.get("rxhttp.wrapper.entity", "Progress");
        ClassName progressTName = ClassName.get("rxhttp.wrapper.entity", "ProgressT");
        ClassName downloadParserName = ClassName.get("rxhttp.wrapper.parse", "DownloadParser");

        TypeName typeName = TypeName.get(String.class);
        TypeName listTName = ParameterizedTypeName.get(ClassName.get(List.class), t);
        TypeName mapTTName = ParameterizedTypeName.get(ClassName.get(Map.class), t, t);
        TypeName mapKVName = ParameterizedTypeName.get(ClassName.get(Map.class), k, v);
        TypeName progressTStringName = ParameterizedTypeName.get(progressTName, typeName);
        TypeName observableTName = ParameterizedTypeName.get(observableName, t);
        TypeName observableStringName = ParameterizedTypeName.get(observableName, typeName);
        TypeName consumerProgressName = ParameterizedTypeName.get(consumerName, progressName);
        TypeName parserTName = ParameterizedTypeName.get(parserName, t);

        List<MethodSpec> methodList = new ArrayList<>();
        MethodSpec.Builder method;
        method = MethodSpec.methodBuilder("execute")
            .addModifiers(Modifier.PUBLIC)
            .addException(IOException.class)
            .addStatement("doOnStart()")
            .addStatement("return $T.execute(param)", httpSenderName)
            .returns(responseName);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("execute")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addException(IOException.class)
            .addParameter(parserTName, "parser")
            .addStatement("return parser.onParse(execute())", httpSenderName)
            .returns(t);
        methodList.add(method.build());

        methodList.add(
            MethodSpec.methodBuilder("newCall")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return newCall(getOkHttpClient())")
                .returns(callName)
                .build());

        methodList.add(
            MethodSpec.methodBuilder("newCall")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(okHttpClientName, "okHttp")
                .addStatement("return $T.newCall(okHttp, buildRequest())", httpSenderName)
                .returns(callName)
                .build());

        methodList.add(
            MethodSpec.methodBuilder("buildRequest")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("doOnStart()")
                .addStatement("return param.buildRequest()")
                .returns(requestName)
                .build());

        methodList.add(
            MethodSpec.methodBuilder("doOnStart")
                .addJavadoc("请求开始前内部调用，用于添加默认域名等操作\n")
                .addStatement("setConverter(param)")
                .addStatement("addDefaultDomainIfAbsent(param)")
                .build());

        method = MethodSpec.methodBuilder("subscribeOn")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(schedulerName, "scheduler")
            .addStatement("this.scheduler=scheduler")
            .addStatement("return (R)this")
            .returns(RxHttpGenerator.r);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("subscribeOnCurrent")
            .addJavadoc("设置在当前线程发请求\n")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this.scheduler=null")
            .addStatement("return (R)this")
            .returns(RxHttpGenerator.r);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("subscribeOnIo")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this.scheduler=Schedulers.io()")
            .addStatement("return (R)this")
            .returns(RxHttpGenerator.r);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("subscribeOnComputation")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this.scheduler=Schedulers.computation()")
            .addStatement("return (R)this")
            .returns(RxHttpGenerator.r);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("subscribeOnNewThread")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this.scheduler=Schedulers.newThread()")
            .addStatement("return (R)this")
            .returns(RxHttpGenerator.r);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("subscribeOnSingle")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this.scheduler=Schedulers.single()")
            .addStatement("return (R)this")
            .returns(RxHttpGenerator.r);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("subscribeOnTrampoline")
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this.scheduler=Schedulers.trampoline()")
            .addStatement("return (R)this")
            .returns(RxHttpGenerator.r);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("asParser")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addParameter(parserTName, "parser")
            .addStatement("doOnStart()")
            .addStatement("Observable<T> observable=$T.syncFrom(param,parser)", httpSenderName)
            .beginControlFlow("if(scheduler!=null)")
            .addStatement("observable=observable.subscribeOn(scheduler)")
            .endControlFlow()
            .addStatement("return observable")
            .returns(observableTName);
        methodList.add(method.build());

        RxHttpExtensions rxHttpExtensions = new RxHttpExtensions();

        //获取自定义的解析器
        for (Entry<String, TypeElement> item : mElementMap.entrySet()) {
            TypeMirror returnType = null; //获取onParse方法的返回类型
            TypeElement typeElement = item.getValue();
            for (Element element : typeElement.getEnclosedElements()) {
                if (!(element instanceof ExecutableElement)
                    || !element.getModifiers().contains(Modifier.PUBLIC)
                    || element.getModifiers().contains(Modifier.STATIC))
                    continue;
                ExecutableElement executableElement = (ExecutableElement) element;
                if (executableElement.getSimpleName().toString().equals("onParse")
                    && executableElement.getParameters().size() == 1
                    && executableElement.getParameters().get(0).asType().toString().equals("okhttp3.Response")) {
                    returnType = executableElement.getReturnType();
                    break;
                }
            }
            if (returnType == null) continue;
            String parserAlias = item.getKey();  //Parser注解里面的name字段值
            rxHttpExtensions.generateAsClassFun(typeElement, parserAlias);

            methodList.add(generateAsXxxMethod(typeElement, parserAlias, returnType, null));
            List<? extends TypeMirror> typeMirrors = mTypeMap.get(parserAlias);
            for (TypeMirror mirror : typeMirrors) {  //遍历Parser注解里面的wrappers数组
                String name = mirror.toString();
                String simpleName = name.substring(name.lastIndexOf(".") + 1);
                String methodName = parserAlias + simpleName;
                methodList.add(generateAsXxxMethod(typeElement, methodName, returnType, mirror));
            }
        }
        rxHttpExtensions.generateClassFile(filer);

        method = MethodSpec.methodBuilder("asDownload")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, "destPath")
            .addParameter(consumerProgressName, "progressConsumer")
            .addParameter(schedulerName, "observeOnScheduler")
            .addStatement("doOnStart()")
            .addStatement("Observable<Progress> observable = $T.downloadProgress(param, destPath, breakDownloadOffSize, scheduler)", httpSenderName)
            .beginControlFlow("if(observeOnScheduler != null)")
            .addStatement("observable = observable.observeOn(observeOnScheduler)")
            .endControlFlow()
            .addStatement("return observable.doOnNext(progressConsumer)\n" +
                ".filter(progress -> progress instanceof ProgressT)\n" +
                ".map(progress -> (($T) progress).getResult())", progressTStringName)
            .returns(observableStringName);
        methodList.add(method.build());

        return methodList;
    }

    private MethodSpec generateAsXxxMethod(
        TypeElement typeElement,
        String methodName,
        TypeMirror returnTypeMirror,
        @Nullable TypeMirror mirror
    ) {
        List<TypeVariableName> typeVariableNames = new ArrayList<>();
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        List<? extends TypeParameterElement> typeParameters = typeElement.getTypeParameters();
        for (TypeParameterElement element : typeParameters) {
            TypeVariableName typeVariableName = TypeVariableName.get(element);
            typeVariableNames.add(typeVariableName);
            ParameterSpec parameterSpec = ParameterSpec.builder(
                ParameterizedTypeName.get(ClassName.get(Class.class), typeVariableName),
                element.asType().toString().toLowerCase() + "Type").build();
            parameterSpecs.add(parameterSpec);
        }

        //自定义解析器对应的asXxx方法里面的语句
        StringBuilder statementBuilder = new StringBuilder("return asParser(new $T");
        int size = typeVariableNames.size();
        if (size > 0) statementBuilder.append("<");
        for (int i = 0; i < size; i++) {//添加泛型
            if (mirror != null) {
                String name = mirror.toString();
                String simpleName = name.substring(name.lastIndexOf('.') + 1);
                statementBuilder.append(simpleName).append("<");
            }
            TypeVariableName variableName = typeVariableNames.get(i);
            statementBuilder.append(variableName.name);
            if (mirror != null) {
                statementBuilder.append(">");
            }
            statementBuilder.append(i == size - 1 ? ">" : ",");
        }
        if (mirror != null) {
            String name = mirror.toString();
            String simpleName = name.substring(name.lastIndexOf('.') + 1);
            statementBuilder.append("(");
            for (ParameterSpec spec : parameterSpecs) {
                statementBuilder.append(spec.name).append(simpleName)
                    .append(",");
            }
            statementBuilder.deleteCharAt(statementBuilder.length() - 1).append(")");
        } else {
            statementBuilder.append("(");
            size = parameterSpecs.size();
            for (int i = 0; i < size; i++) {//添加参数
                ParameterSpec parameterSpec = parameterSpecs.get(i);
                statementBuilder.append(parameterSpec.name);
                if (i < size - 1) {
                    statementBuilder.append(",");
                }
            }
            statementBuilder.append(")");
        }
        statementBuilder.append(")");

        TypeName typeName = TypeName.get(returnTypeMirror);
        if (mirror != null) {
            ClassName className = ClassName.bestGuess(mirror.toString());
            if (typeName instanceof ParameterizedTypeName) {
                ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName) typeName;
                List<TypeName> typeNames = new ArrayList<>();
                for (TypeName type : parameterizedTypeName.typeArguments) {
                    TypeName parameterizedReturnType = ParameterizedTypeName
                        .get(className, type);
                    typeNames.add(parameterizedReturnType);
                }
                typeName = ParameterizedTypeName.get(parameterizedTypeName.rawType,
                    typeNames.toArray(new TypeName[0]));
            } else {
                typeName = ParameterizedTypeName.get(className, typeName);
            }
        }
        ClassName observableName = ClassName.get("io.reactivex", "Observable");
        TypeName returnType = ParameterizedTypeName.get(observableName, typeName);

        Builder builder = MethodSpec.methodBuilder("as" + methodName)
            .addModifiers(Modifier.PUBLIC);

        if (mirror != null) {
            ClassName parameterizedType = ClassName.get("rxhttp.wrapper.entity", "ParameterizedTypeImpl");
            ClassName type = ClassName.get("java.lang.reflect", "Type");
            for (ParameterSpec spec : parameterSpecs) {
                String expression = "$T " + spec.name + "$T = $T.get($T.class, " + spec.name + ")";
                builder.addStatement(expression, type, TypeName.get(mirror), parameterizedType, TypeName.get(mirror));
            }
        }

        builder.addTypeVariables(typeVariableNames)  //添加泛型
            .addParameters(parameterSpecs)     //添加参数
            .addStatement(statementBuilder.toString(), ClassName.get(typeElement)) //添加表达式
            .returns(returnType);//设置返回值

        return builder.build();
    }
}
