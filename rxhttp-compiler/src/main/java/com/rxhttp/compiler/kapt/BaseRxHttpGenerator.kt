package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.getClassPath
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import java.io.IOException
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

class BaseRxHttpGenerator(val isAndroidPlatform: Boolean) {
    var parserVisitor: ParserVisitor? = null

    //生成BaseRxHttp类
    @Throws(IOException::class)
    fun generateCode(filer: Filer) {

        val responseName = ClassName.get("okhttp3", "Response")

        val t = TypeVariableName.get("T")
        val v = TypeVariableName.get("V")
        val className = ClassName.get(Class::class.java)

        val list = ClassName.get(List::class.java)
        val map = ClassName.get(Map::class.java)
        val string = TypeName.get(String::class.java)
        val mapStringV = ParameterizedTypeName.get(map, string, v)

        val classTName = ParameterizedTypeName.get(className, t)
        val classVName = ParameterizedTypeName.get(className, v)

        val listTName = ParameterizedTypeName.get(list, t)

        val parserName = ClassName.get("rxhttp.wrapper.parse", "Parser")
        val logUtilName = ClassName.get("rxhttp.wrapper.utils", "LogUtil")
        val rxJavaPlugins = ClassName.bestGuess(getClassPath("RxJavaPlugins"))
        val consumer = ClassName.bestGuess(getClassPath("Consumer"))
        val parserTName = ParameterizedTypeName.get(parserName, t)
        val smartParserName = ClassName.get("rxhttp.wrapper.parse", "SmartParser")
        val streamParser = ClassName.get("rxhttp.wrapper.parse", "StreamParser")
        val type = ClassName.get("java.lang.reflect", "Type")
        val parameterizedType = ClassName.get("rxhttp.wrapper.entity", "ParameterizedTypeImpl")
        val outputStreamFactory = ClassName.get("rxhttp.wrapper.callback", "OutputStreamFactory")
        val fileOutputStreamFactory =
            ClassName.get("rxhttp.wrapper.callback", "FileOutputStreamFactory")
        val uriOutputStreamFactory =
            ClassName.get("rxhttp.wrapper.callback", "UriOutputStreamFactory")
        val observableCall = ClassName.get(rxHttpPackage, "ObservableCall")
        val observableCallT = ParameterizedTypeName.get(observableCall, t)

        val methodList = ArrayList<MethodSpec>() //方法集合

        if (isDependenceRxJava()) {
            MethodSpec.methodBuilder("toObservable")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(parserTName, "parser")
                .addStatement("return new ObservableCall(this, parser)")
                .returns(observableCallT)
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("toObservable")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(type, "type")
                .addStatement("return toObservable(\$T.wrap(type))", smartParserName)
                .returns(observableCallT)
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("toObservable")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(classTName, "clazz")
                .addStatement("return toObservable((Type) clazz)")
                .returns(observableCallT)
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("toObservableString")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return toObservable(String.class)")
                .returns(ParameterizedTypeName.get(observableCall, string))
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("toObservableMapString")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(v)
                .addParameter(classVName, "clazz")
                .addCode(
                    """
                Type tTypeMap = ParameterizedTypeImpl.getParameterized(Map.class, String.class, clazz);
                return toObservable(tTypeMap);
            """.trimIndent()
                )
                .returns(ParameterizedTypeName.get(observableCall, mapStringV))
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("toObservableList")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(classTName, "clazz")
                .addCode(
                    """
                Type tTypeList = ParameterizedTypeImpl.get(List.class, clazz);
                return toObservable(tTypeList);
            """.trimIndent()
                )
                .returns(ParameterizedTypeName.get(observableCall, listTName))
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("toDownloadObservable")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(string, "destPath")
                .addStatement("return toDownloadObservable(destPath, false)")
                .returns(ParameterizedTypeName.get(observableCall, string))
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("toDownloadObservable")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(string, "destPath")
                .addParameter(Boolean::class.java, "append")
                .addStatement(
                    "return toDownloadObservable(new \$T(destPath), append)",
                    fileOutputStreamFactory
                )
                .returns(ParameterizedTypeName.get(observableCall, string))
                .build()
                .apply { methodList.add(this) }

            if (isAndroidPlatform) {
                val context = ClassName.get("android.content", "Context")
                val uri = ClassName.get("android.net", "Uri")

                MethodSpec.methodBuilder("toDownloadObservable")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(context, "context")
                    .addParameter(uri, "uri")
                    .addStatement("return toDownloadObservable(context, uri, false)")
                    .returns(ParameterizedTypeName.get(observableCall, uri))
                    .build()
                    .apply { methodList.add(this) }

                MethodSpec.methodBuilder("toDownloadObservable")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(context, "context")
                    .addParameter(uri, "uri")
                    .addParameter(Boolean::class.java, "append")
                    .addStatement(
                        "return toDownloadObservable(new \$T(context, uri), append)",
                        uriOutputStreamFactory
                    )
                    .returns(ParameterizedTypeName.get(observableCall, uri))
                    .build()
                    .apply { methodList.add(this) }
            }

            MethodSpec.methodBuilder("toDownloadObservable")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(ParameterizedTypeName.get(outputStreamFactory, t), "osFactory")
                .addStatement("return toDownloadObservable(osFactory, false)")
                .returns(observableCallT)
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("toDownloadObservable")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .addParameter(ParameterizedTypeName.get(outputStreamFactory, t), "osFactory")
                .addParameter(Boolean::class.java, "append")
                .addCode(
                    """
                ObservableCall<T> observableCall = toObservable(new ${'$'}T<>(osFactory));
                if (append) {
                    return observableCall.onSubscribe(() -> {
                        long offsetSize = osFactory.offsetSize();
                        if (offsetSize >= 0)
                            setRangeHeader(offsetSize, -1, true);
                    });
                } else {
                    return observableCall;
                }
            """.trimIndent(), streamParser
                )
                .returns(observableCallT)
                .build()
                .apply { methodList.add(this) }
        }

        parserVisitor?.apply {
            methodList.addAll(getMethodList(filer))
        }

        MethodSpec.methodBuilder("execute")
            .addModifiers(Modifier.PUBLIC)
            .addException(IOException::class.java)
            .addStatement("return newCall().execute()")
            .returns(responseName)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("execute")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addException(IOException::class.java)
            .addParameter(parserTName, "parser")
            .addStatement("return parser.onParse(execute())")
            .returns(t)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("executeClass")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addException(IOException::class.java)
            .addParameter(type, "type")
            .addStatement("return execute(\$T.wrap(type))", smartParserName)
            .returns(t)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("executeClass")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addException(IOException::class.java)
            .addParameter(classTName, "clazz")
            .addStatement("return executeClass((Type) clazz)")
            .returns(t)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("executeString")
            .addModifiers(Modifier.PUBLIC)
            .addException(IOException::class.java)
            .addStatement("return executeClass(String.class)")
            .returns(string)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("executeList")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariable(t)
            .addException(IOException::class.java)
            .addParameter(classTName, "clazz")
            .addStatement("\$T tTypeList = \$T.get(List.class, clazz)", type, parameterizedType)
            .addStatement("return executeClass(tTypeList)")
            .returns(listTName)
            .build()
            .apply { methodList.add(this) }

        val fileName = "BaseRxHttp"
        val typeSpecBuilder = TypeSpec.classBuilder(fileName)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addSuperinterface(ClassName.get("rxhttp.wrapper", "CallFactory"))
            .addSuperinterface(ClassName.get("rxhttp.wrapper.coroutines", "RangeHeader"))
            .addMethods(methodList)
            .addJavadoc(
                """
                User: ljx
                Date: 2020/4/11
                Time: 18:15
            """.trimIndent()
            )

        if (isDependenceRxJava()) {
            val codeBlock = CodeBlock.of(
                """
                ${'$'}T<? super Throwable> errorHandler = ${'$'}T.getErrorHandler();
                if (errorHandler == null) {                                                
                    /*                                                                     
                    RxJava2的一个重要的设计理念是：不吃掉任何一个异常, 即抛出的异常无人处理，便会导致程序崩溃                      
                    这就会导致一个问题，当RxJava2“downStream”取消订阅后，“upStream”仍有可能抛出异常，                
                    这时由于已经取消订阅，“downStream”无法处理异常，此时的异常无人处理，便会导致程序崩溃                       
                    */                                                                     
                    RxJavaPlugins.setErrorHandler(${'$'}T::log);                           
                }
                
            """.trimIndent(), consumer, rxJavaPlugins, logUtilName
            )
            typeSpecBuilder.addStaticBlock(codeBlock)
        }

        // Write file
        JavaFile.builder(rxHttpPackage, typeSpecBuilder.build())
            .indent("    ")
            .skipJavaLangImports(true)
            .build().writeTo(filer)
    }
}