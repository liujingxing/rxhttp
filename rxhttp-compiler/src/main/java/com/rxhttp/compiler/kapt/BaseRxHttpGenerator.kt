package com.rxhttp.compiler.kapt

import com.rxhttp.compiler.getClassPath
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import java.io.IOException
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier

class BaseRxHttpGenerator(private val isAndroidPlatform: Boolean) {
    var parserVisitor: ParserVisitor? = null

    //生成BaseRxHttp类
    @Throws(IOException::class)
    fun generateCode(filer: Filer) {
        val t = TypeVariableName.get("T")
        val v = TypeVariableName.get("V")
        val className = ClassName.get(Class::class.java)
        val classTName = className.parameterizedBy(t)
        val classVName = className.parameterizedBy(v)
        val list = ClassName.get(List::class.java)
        val listTName = list.parameterizedBy(t)

        val map = ClassName.get(Map::class.java)
        val string = TypeName.get(String::class.java)
        val mapStringV = map.parameterizedBy(string, v)

        val parser = ClassName.get("rxhttp.wrapper.parse", "Parser")
        val parserT = parser.parameterizedBy(t)
        val smartParser = parser.peerClass("SmartParser")
        val streamParser = parser.peerClass("StreamParser")

        val rxJavaPlugins = ClassName.bestGuess(getClassPath("RxJavaPlugins"))
        val logUtilName = ClassName.get("rxhttp.wrapper.utils", "LogUtil")
        val consumer = ClassName.bestGuess(getClassPath("Consumer"))

        val responseName = ClassName.get("okhttp3", "Response")
        val type = ClassName.get("java.lang.reflect", "Type")
        val parameterizedType = ClassName.get("rxhttp.wrapper.entity", "ParameterizedTypeImpl")
        val outputStreamFactory = ClassName.get("rxhttp.wrapper.callback", "OutputStreamFactory")
        val fileOutputStreamFactory = outputStreamFactory.peerClass("FileOutputStreamFactory")
        val uriOutputStreamFactory = outputStreamFactory.peerClass("UriOutputStreamFactory")
        val observableCall = ClassName.get(rxHttpPackage, "ObservableCall")
        val observableCallT = observableCall.parameterizedBy(t)

        val methodList = ArrayList<MethodSpec>() //方法集合

        if (isDependenceRxJava()) {
            MethodSpec.methodBuilder("toObservable")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addTypeVariable(t)
                .addParameter(parserT, "parser")
                .addStatement("return new ObservableCall(this, parser)")
                .returns(observableCallT)
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("toObservable")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addTypeVariable(t)
                .addParameter(type, "type")
                .addStatement("return toObservable(\$T.wrap(type))", smartParser)
                .returns(observableCallT)
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("toObservable")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addTypeVariable(t)
                .addParameter(classTName, "clazz")
                .addStatement("return toObservable((Type) clazz)")
                .returns(observableCallT)
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("toObservableString")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addStatement("return toObservable(String.class)")
                .returns(observableCall.parameterizedBy(string))
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("toObservableMapString")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addTypeVariable(v)
                .addParameter(classVName, "clazz")
                .addCode(
                    """
                Type typeMap = ParameterizedTypeImpl.getParameterized(Map.class, String.class, clazz);
                return toObservable(typeMap);
            """.trimIndent()
                )
                .returns(observableCall.parameterizedBy(mapStringV))
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("toObservableList")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addTypeVariable(t)
                .addParameter(classTName, "clazz")
                .addCode(
                    """
                Type typeList = ParameterizedTypeImpl.get(List.class, clazz);
                return toObservable(typeList);
            """.trimIndent()
                )
                .returns(observableCall.parameterizedBy(listTName))
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("toDownloadObservable")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(string, "destPath")
                .addStatement("return toDownloadObservable(destPath, false)")
                .returns(observableCall.parameterizedBy(string))
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("toDownloadObservable")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(string, "destPath")
                .addParameter(Boolean::class.java, "append")
                .addStatement(
                    "return toDownloadObservable(new \$T(destPath), append)",
                    fileOutputStreamFactory
                )
                .returns(observableCall.parameterizedBy(string))
                .build()
                .apply { methodList.add(this) }

            if (isAndroidPlatform) {
                val context = ClassName.get("android.content", "Context")
                val uri = ClassName.get("android.net", "Uri")

                MethodSpec.methodBuilder("toDownloadObservable")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(context, "context")
                    .addParameter(uri, "uri")
                    .addStatement("return toDownloadObservable(context, uri, false)")
                    .returns(observableCall.parameterizedBy(uri))
                    .build()
                    .apply { methodList.add(this) }

                MethodSpec.methodBuilder("toDownloadObservable")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addParameter(context, "context")
                    .addParameter(uri, "uri")
                    .addParameter(Boolean::class.java, "append")
                    .addStatement(
                        "return toDownloadObservable(new \$T(context, uri), append)",
                        uriOutputStreamFactory
                    )
                    .returns(observableCall.parameterizedBy( uri))
                    .build()
                    .apply { methodList.add(this) }
            }

            MethodSpec.methodBuilder("toDownloadObservable")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addTypeVariable(t)
                .addParameter(outputStreamFactory.parameterizedBy( t), "osFactory")
                .addStatement("return toDownloadObservable(osFactory, false)")
                .returns(observableCallT)
                .build()
                .apply { methodList.add(this) }

            MethodSpec.methodBuilder("toDownloadObservable")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addTypeVariable(t)
                .addParameter(outputStreamFactory.parameterizedBy(t), "osFactory")
                .addParameter(Boolean::class.java, "append")
                .addCode(
                    """
                if (append) {
                    tag(OutputStreamFactory.class, osFactory);
                }        
                return toObservable(new ${'$'}T<>(osFactory));
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
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addException(IOException::class.java)
            .addStatement("return newCall().execute()")
            .returns(responseName)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("execute")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addTypeVariable(t)
            .addException(IOException::class.java)
            .addParameter(parserT, "parser")
            .addStatement("return parser.onParse(execute())")
            .returns(t)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("executeClass")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addTypeVariable(t)
            .addException(IOException::class.java)
            .addParameter(type, "type")
            .addStatement("return execute(\$T.wrap(type))", smartParser)
            .returns(t)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("executeClass")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addTypeVariable(t)
            .addException(IOException::class.java)
            .addParameter(classTName, "clazz")
            .addStatement("return executeClass((Type) clazz)")
            .returns(t)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("executeString")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addException(IOException::class.java)
            .addStatement("return executeClass(String.class)")
            .returns(string)
            .build()
            .apply { methodList.add(this) }

        MethodSpec.methodBuilder("executeList")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addTypeVariable(t)
            .addException(IOException::class.java)
            .addParameter(classTName, "clazz")
            .addStatement("\$T typeList = \$T.get(List.class, clazz)", type, parameterizedType)
            .addStatement("return executeClass(typeList)")
            .returns(listTName)
            .build()
            .apply { methodList.add(this) }

        val fileName = "BaseRxHttp"
        val typeSpecBuilder = TypeSpec.classBuilder(fileName)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addSuperinterface(ClassName.get("rxhttp.wrapper", "ITag"))
            .addSuperinterface(ClassName.get("rxhttp.wrapper", "CallFactory"))
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