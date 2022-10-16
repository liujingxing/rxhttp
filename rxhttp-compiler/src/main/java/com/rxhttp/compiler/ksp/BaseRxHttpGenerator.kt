package com.rxhttp.compiler.ksp

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFile
import com.rxhttp.compiler.getClassPath
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.jvm.throws
import com.squareup.kotlinpoet.ksp.writeTo
import java.io.IOException

class BaseRxHttpGenerator(
    private val logger: KSPLogger,
    private val isAndroidPlatform: Boolean,
    private val ksFiles: Collection<KSFile>
) {
    var parserVisitor: ParserVisitor? = null

    //生成BaseRxHttp类
    @KspExperimental
    @Throws(IOException::class)
    fun generateCode(codeGenerator: CodeGenerator) {
        val responseName = ClassName("okhttp3", "Response")
        val type = ClassName("java.lang.reflect", "Type")

        val t = TypeVariableName("T")
        val v = TypeVariableName("V")
        val classTName = Class::class.asClassName().parameterizedBy("T")
        val classVName = Class::class.asClassName().parameterizedBy("V")
        val listTName = LIST.parameterizedBy("T")

        val parserName = ClassName("rxhttp.wrapper.parse", "Parser")
        val parserTName = parserName.parameterizedBy("T")
        val smartParserName = ClassName("rxhttp.wrapper.parse", "SmartParser")
        val streamParser = ClassName("rxhttp.wrapper.parse", "StreamParser")
        val parameterizedType = ClassName("rxhttp.wrapper.entity", "ParameterizedTypeImpl")
        val rxJavaPlugins = ClassName.bestGuess(getClassPath("RxJavaPlugins"))
        val logUtil = ClassName("rxhttp.wrapper.utils", "LogUtil")
        val outputStreamFactory = ClassName("rxhttp.wrapper.callback", "OutputStreamFactory")
        val fileOutputStreamFactory =
            ClassName("rxhttp.wrapper.callback", "FileOutputStreamFactory")
        val uriOutputStreamFactory = ClassName("rxhttp.wrapper.callback", "UriOutputStreamFactory")
        val observableCall = ClassName(rxHttpPackage, "ObservableCall")

        val methodList = ArrayList<FunSpec>() //方法集合

        if (isDependenceRxJava()) {
            FunSpec.builder("toObservable")
                .addTypeVariable(t)
                .addParameter("parser", parserTName)
                .addStatement("return ObservableCall(this, parser)")
                .build()
                .let { methodList.add(it) }

            FunSpec.builder("toObservable")
                .addTypeVariable(t)
                .addParameter("type", type)
                .addStatement("return toObservable(%T.wrap<T>(type))", smartParserName)
                .build()
                .let { methodList.add(it) }

            FunSpec.builder("toObservable")
                .addTypeVariable(t)
                .addParameter("clazz", classTName)
                .addStatement("return toObservable<T>(clazz as Type)")
                .build()
                .let { methodList.add(it) }

            FunSpec.builder("toObservableString")
                .addStatement("return toObservable(String::class.java)")
                .build()
                .let { methodList.add(it) }

            FunSpec.builder("toObservableMapString")
                .addTypeVariable(v)
                .addParameter("clazz", classVName)
                .addCode("return toObservable<Map<String, V>>(ParameterizedTypeImpl.getParameterized(MutableMap::class.java,String::class.java, clazz))")
                .build()
                .let { methodList.add(it) }

            FunSpec.builder("toObservableList")
                .addTypeVariable(t)
                .addParameter("clazz", classTName)
                .addCode("return toObservable<List<T>>(ParameterizedTypeImpl.get(MutableList::class.java, clazz))")
                .build()
                .let { methodList.add(it) }

            val appendParam = ParameterSpec.builder("append", Boolean::class)
                .defaultValue("false")
                .build()

            FunSpec.builder("toDownloadObservable")
                .addAnnotation(JvmOverloads::class)
                .addParameter("destPath", String::class)
                .addParameter(appendParam)
                .addStatement(
                    "return toDownloadObservable(%T(destPath), append)",
                    fileOutputStreamFactory
                )
                .returns(observableCall.parameterizedBy("String"))
                .build()
                .let { methodList.add(it) }


            if (isAndroidPlatform) {
                val context = ClassName("android.content", "Context")
                val uri = ClassName("android.net", "Uri")
                FunSpec.builder("toDownloadObservable")
                    .addAnnotation(JvmOverloads::class)
                    .addParameter("context", context)
                    .addParameter("uri", uri)
                    .addParameter(appendParam)
                    .addStatement(
                        "return toDownloadObservable(%T(context, uri), append)",
                        uriOutputStreamFactory
                    )
                    .returns(observableCall.parameterizedBy("Uri"))
                    .build()
                    .let { methodList.add(it) }
            }

            FunSpec.builder("toDownloadObservable")
                .addAnnotation(JvmOverloads::class)
                .addTypeVariable(t)
                .addParameter("osFactory", outputStreamFactory.parameterizedBy("T"))
                .addParameter(appendParam)
                .addCode(
                    """
                val observableCall = toObservable(%T(osFactory))
                return if (append) {
                    observableCall.onSubscribe {
                        // In IO Thread
                        val offsetSize = osFactory.offsetSize()
                        if (offsetSize >= 0) setRangeHeader(offsetSize, -1, true)
                    }
                } else {
                    observableCall
                }
            """.trimIndent(), streamParser
                )
                .returns(observableCall.parameterizedBy("T"))
                .build()
                .let { methodList.add(it) }
        }

        parserVisitor?.apply {
            methodList.addAll(getFunList(codeGenerator))
        }

        FunSpec.builder("execute")
            .throws(IOException::class)
            .addStatement("return newCall().execute()")
            .returns(responseName)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("execute")
            .addTypeVariable(t)
            .throws(IOException::class)
            .addParameter("parser", parserTName)
            .addStatement("return parser.onParse(execute())")
            .returns(t)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("executeClass")
            .addTypeVariable(t)
            .throws(IOException::class)
            .addParameter("type", type)
            .addStatement("return execute(%T.wrap(type))", smartParserName)
            .returns(t)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("executeClass")
            .addTypeVariable(t)
            .throws(IOException::class)
            .addParameter("clazz", classTName)
            .addStatement("return executeClass(clazz as Type)")
            .returns(t)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("executeString")
            .throws(IOException::class)
            .addStatement("return executeClass(String::class.java)")
            .returns(STRING)
            .build()
            .let { methodList.add(it) }

        FunSpec.builder("executeList")
            .addTypeVariable(t)
            .throws(IOException::class)
            .addParameter("clazz", classTName)
            .addStatement("val tTypeList = %T.get(List::class.java, clazz)", parameterizedType)
            .addStatement("return executeClass(tTypeList)")
            .returns(listTName)
            .build()
            .let { methodList.add(it) }

        val fileName = "BaseRxHttp"
        val typeSpecBuilder = TypeSpec.classBuilder(fileName)

        if (isDependenceRxJava()) {
            val codeBlock = CodeBlock.of(
                """
                val errorHandler = %T.getErrorHandler()
                if (errorHandler == null) {
                    /*                                                                     
                     RxJava2的一个重要的设计理念是：不吃掉任何一个异常, 即抛出的异常无人处理，便会导致程序崩溃                      
                     这就会导致一个问题，当RxJava2“downStream”取消订阅后，“upStream”仍有可能抛出异常，                
                     这时由于已经取消订阅，“downStream”无法处理异常，此时的异常无人处理，便会导致程序崩溃                       
                    */
                    RxJavaPlugins.setErrorHandler { %T.log(it) }
                }
            """.trimIndent(), rxJavaPlugins, logUtil
            )
            val companionBuilder = TypeSpec.companionObjectBuilder()
                .addInitializerBlock(codeBlock)
                .build()
            typeSpecBuilder.addType(companionBuilder)
        }

        typeSpecBuilder
            .addModifiers(KModifier.ABSTRACT)
            .addSuperinterface(ClassName("rxhttp.wrapper", "CallFactory"))
            .addSuperinterface(ClassName("rxhttp.wrapper.coroutines", "RangeHeader"))
            .addFunctions(methodList)
            .addKdoc(
                """
                User: ljx
                Date: 2020/4/11
                Time: 18:15
            """.trimIndent()
            )

        val dependencies = Dependencies(true, *ksFiles.toTypedArray())
        FileSpec.builder(rxHttpPackage, fileName)
            .addType(typeSpecBuilder.build())
            .build()
            .writeTo(codeGenerator, dependencies)
    }
}