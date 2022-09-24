package com.rxhttp.compiler.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFile
import com.rxhttp.compiler.getClassPath
import com.rxhttp.compiler.isDependenceRxJava
import com.rxhttp.compiler.rxHttpPackage


/**
 * User: ljx
 * Date: 2020/3/31
 * Time: 23:36
 */
class KClassHelper(
    private val isAndroidPlatform: Boolean,
    private val ksFiles: Collection<KSFile>
) {

    private fun isAndroid(s: String) = if (isAndroidPlatform) s else ""

    fun generatorStaticClass(codeGenerator: CodeGenerator) {
        generatorBaseRxHttp(codeGenerator)
        generatorRxHttpAbstractBodyParam(codeGenerator)
        generatorRxHttpBodyParam(codeGenerator)
        generatorRxHttpFormParam(codeGenerator)
        generatorRxHttpNoBodyParam(codeGenerator)
        generatorRxHttpJsonParam(codeGenerator)
        generatorRxHttpJsonArrayParam(codeGenerator)
    }

    private fun generatorBaseRxHttp(codeGenerator: CodeGenerator) {
        if (!isDependenceRxJava()) {
            generatorClass(
                codeGenerator, "BaseRxHttp", """
                package $rxHttpPackage

                import rxhttp.wrapper.CallFactory
                import rxhttp.wrapper.coroutines.RangeHeader

                /**
                 * 本类存放asXxx方法(需要单独依赖RxJava，并告知RxHttp依赖的RxJava版本)
                 * 如未生成，请查看 https://github.com/liujingxing/rxhttp/wiki/FAQ
                 * User: ljx
                 * Date: 2020/4/11
                 * Time: 18:15
                 */
                abstract class BaseRxHttp : CallFactory, RangeHeader {

                    
                }
            """.trimIndent()
            )
        } else {
            generatorClass(
                codeGenerator, "BaseRxHttp", """
            package $rxHttpPackage
            ${isAndroid("""
            import android.content.Context
            import android.graphics.Bitmap
            import android.net.Uri
            """)}
            import ${getClassPath("Observable")}
            import ${getClassPath("Scheduler")}
            import ${getClassPath("Consumer")}
            import ${getClassPath("RxJavaPlugins")}
            import ${getClassPath("Schedulers")}
            import okhttp3.Headers
            import okhttp3.Response
            import rxhttp.wrapper.CallFactory
            import rxhttp.wrapper.callback.FileOutputStreamFactory
            import rxhttp.wrapper.callback.OutputStreamFactory
            ${isAndroid("import rxhttp.wrapper.callback.UriOutputStreamFactory")}
            import rxhttp.wrapper.coroutines.RangeHeader
            import rxhttp.wrapper.entity.ParameterizedTypeImpl
            import rxhttp.wrapper.entity.Progress
            import rxhttp.wrapper.parse.Parser
            import rxhttp.wrapper.parse.SmartParser
            import rxhttp.wrapper.parse.StreamParser
            import rxhttp.wrapper.utils.LogUtil
            import java.lang.reflect.Type

            /**
             * 本类存放asXxx方法(需要单独依赖RxJava，并告知RxHttp依赖的RxJava版本)
             * 如未生成，请查看 https://github.com/liujingxing/rxhttp/wiki/FAQ
             * User: ljx
             * Date: 2020/4/11
             * Time: 18:15
             */
            abstract class BaseRxHttp : CallFactory, RangeHeader {
            
                companion object {
                    init {
                        val errorHandler = RxJavaPlugins.getErrorHandler()
                        if (errorHandler == null) {
                            /*                                                                     
                             RxJava2的一个重要的设计理念是：不吃掉任何一个异常, 即抛出的异常无人处理，便会导致程序崩溃                      
                             这就会导致一个问题，当RxJava2“downStream”取消订阅后，“upStream”仍有可能抛出异常，                
                             这时由于已经取消订阅，“downStream”无法处理异常，此时的异常无人处理，便会导致程序崩溃                       
                            */
                            RxJavaPlugins.setErrorHandler { LogUtil.log(it) }
                        }
                    }
                }

                fun <T> asParser(parser: Parser<T>) = ObservableCall(this, parser)

                fun <T> asClass(type: Type) = asParser(SmartParser.wrap<T>(type))

                fun <T> asClass(clazz: Class<T>) = asClass<T>(clazz as Type)

                fun asString() = asClass(String::class.java)

                fun <V> asMapString(vType: Class<V>) =
                    asClass<Map<String, V>>(ParameterizedTypeImpl.getParameterized(MutableMap::class.java,String::class.java,vType))

                fun <T> asList(tType: Class<T>) =
                    asClass<List<T>>(ParameterizedTypeImpl[MutableList::class.java, tType])
                ${isAndroid("""
                fun asBitmap() = asClass(Bitmap::class.java)
                """)}
                fun asOkResponse() = asClass(Response::class.java)

                fun asHeaders() = asClass(Headers::class.java)

                @JvmOverloads
                fun asDownload(
                    destPath: String,
                    append: Boolean = false,
                ): ObservableCall<String> = asDownload(FileOutputStreamFactory(destPath), append)
                ${isAndroid("""
                @JvmOverloads
                fun asDownload(
                    context: Context,
                    uri: Uri,
                    append: Boolean = false,
                ): ObservableCall<Uri> = asDownload(UriOutputStreamFactory(context, uri), append)
                """)}
                @JvmOverloads
                fun <T> asDownload(
                    osFactory: OutputStreamFactory<T>,
                    append: Boolean = false,
                ): ObservableCall<T> {
                   val observableCall =  asParser(StreamParser(osFactory))
                   return if (append) {
                       observableCall.onSubscribe {
                           // In IO Thread
                           val offsetSize = osFactory.offsetSize()
                           if (offsetSize >= 0) setRangeHeader(offsetSize, -1, true)
                       }
                   } else {
                       observableCall
                   }
                }
            }    

        """.trimIndent()
            )
        }
    }

    private fun generatorRxHttpAbstractBodyParam(codeGenerator: CodeGenerator) {
        generatorClass(
            codeGenerator, "RxHttpAbstractBodyParam", """
                package $rxHttpPackage
                
                import rxhttp.wrapper.BodyParamFactory

                /**
                 * Github
                 * https://github.com/liujingxing/rxhttp
                 * https://github.com/liujingxing/rxlife
                 * https://github.com/liujingxing/rxhttp/wiki/FAQ
                 * https://github.com/liujingxing/rxhttp/wiki/更新日志
                 */
                open class RxHttpAbstractBodyParam<P : AbstractBodyParam<P>, R : RxHttpAbstractBodyParam<P, R>> 
                protected constructor(
                    param: P
                ) : RxHttp<P, R>(param), BodyParamFactory {

                }
            """.trimIndent()
        )
    }

    private fun generatorRxHttpNoBodyParam(codeGenerator: CodeGenerator) {
        generatorClass(
            codeGenerator, "RxHttpNoBodyParam", """
            package $rxHttpPackage

            import rxhttp.wrapper.param.NoBodyParam

            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            open class RxHttpNoBodyParam(param: NoBodyParam) : RxHttp<NoBodyParam, RxHttpNoBodyParam>(param) {
            
                @JvmOverloads
                fun add(key: String, value: Any?, isAdd: Boolean = true) = apply {
                    if (isAdd) addQuery(key, value)
                }
            
                fun addAll(map: Map<String, *>) = addAllQuery(map)
            
                fun addEncoded(key: String, value: Any?) = addEncodedQuery(key, value)
            
                fun addAllEncoded(map: Map<String, *>) = addAllEncodedQuery(map)
            }

        """.trimIndent()
        )
    }


    private fun generatorRxHttpBodyParam(codeGenerator: CodeGenerator) {
        generatorClass(
            codeGenerator, "RxHttpBodyParam", """
            package $rxHttpPackage
            ${isAndroid("""
            import android.content.Context
            import android.net.Uri
            import rxhttp.wrapper.utils.asRequestBody
            """)}
            import okhttp3.MediaType
            import okhttp3.RequestBody
            import okio.ByteString
            import rxhttp.wrapper.param.BodyParam
            import rxhttp.wrapper.OkHttpCompat
            import rxhttp.wrapper.entity.FileRequestBody
            import rxhttp.wrapper.utils.BuildUtil
            import java.io.File

            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             */
            open class RxHttpBodyParam(param: BodyParam) : RxHttpAbstractBodyParam<BodyParam, RxHttpBodyParam>(param) {

                fun setBody(content: String, contentType: MediaType? = null) =
                    setBody(OkHttpCompat.create(contentType, content))

                fun setBody(content: ByteString, contentType: MediaType? = null) =
                    setBody(OkHttpCompat.create(contentType, content))

                @JvmOverloads
                fun setBody(
                    content: ByteArray,
                    contentType: MediaType?,
                    offset: Int = 0,
                    byteCount: Int = content.size,
                ) = setBody(OkHttpCompat.create(contentType, content, offset, byteCount))

                @JvmOverloads
                fun setBody(
                    file: File,
                    contentType: MediaType? = BuildUtil.getMediaType(file.name),
                ) = setBody(FileRequestBody(file, 0, contentType))
                ${isAndroid("""
                @JvmOverloads
                fun setBody(
                    context: Context,
                    uri: Uri,
                    contentType: MediaType? = BuildUtil.getMediaTypeByUri(context, uri),
                ) = setBody(uri.asRequestBody(context, 0, contentType))
                """)}
                fun setBody(any: Any) = apply { param.setBody(any) }

                fun setBody(requestBody: RequestBody) = apply { param.setBody(requestBody) }
            }

        """.trimIndent()
        )
    }

    private fun generatorRxHttpFormParam(codeGenerator: CodeGenerator) {
        generatorClass(
            codeGenerator, "RxHttpFormParam", """
            package $rxHttpPackage

            ${isAndroid("import android.content.Context")}
            ${isAndroid("import android.net.Uri")}
            import okhttp3.Headers
            import okhttp3.MediaType
            import okhttp3.MultipartBody
            import okhttp3.RequestBody
            import rxhttp.wrapper.entity.UpFile
            import rxhttp.wrapper.param.FormParam
            import rxhttp.wrapper.utils.BuildUtil
            import rxhttp.wrapper.utils.asPart
            import rxhttp.wrapper.utils.asRequestBody
            import java.io.File


            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            open class RxHttpFormParam(param: FormParam) : RxHttpAbstractBodyParam<FormParam, RxHttpFormParam>(param) {
                
                @JvmOverloads
                fun add(key: String, value: Any?, isAdd: Boolean = true) = apply {
                    if (isAdd) param.add(key, value)
                }
            
                fun addAll(map: Map<String, *>) = apply { param.addAll(map) }
            
                fun addEncoded(key: String, value: Any?) = apply { param.addEncoded(key, value) }
            
                fun addAllEncoded(map: Map<String, *>) = apply { param.addAllEncoded(map) }
            
                fun removeAllBody() = apply { param.removeAllBody() }
            
                fun removeAllBody(key: String) = apply { param.removeAllBody(key) }
            
                operator fun set(key: String, value: Any?) = apply { param[key] = value }
            
                fun setEncoded(key: String, value: Any?) = apply { param.setEncoded(key, value) }
            
                fun addFile(key: String, file: File) = apply { param.addFile(key, file) }
            
                fun addFile(key: String, filePath: String) = apply { param.addFile(key, filePath) }
            
                fun addFile(key: String, file: File, filename: String) = apply { 
                    param.addFile(key, file, filename)
                }
            
                fun addFile(file: UpFile) = apply { param.addFile(file) }
            
                fun addFiles(fileList: List<UpFile>) = apply { param.addFiles(fileList) }
            
                fun <T> addFiles(fileMap: Map<String, T>) = apply { param.addFiles(fileMap) }
            
                fun <T> addFiles(key: String, fileList: List<T>) = apply { param.addFiles(key, fileList) }
            
                fun addPart(contentType: MediaType?, content: ByteArray) = apply {
                    param.addPart(contentType, content)
                }
            
                fun addPart(
                    contentType: MediaType?,
                    content: ByteArray,
                    offset: Int,
                    byteCount: Int
                ) = apply { param.addPart(contentType, content, offset, byteCount) }
                ${isAndroid("""
                @JvmOverloads
                fun addPart(
                    context: Context, 
                    uri: Uri, 
                    contentType: MediaType? = BuildUtil.getMediaTypeByUri(context, uri)
                ) = apply { param.addPart(uri.asRequestBody(context, 0, contentType)) }
            
                @JvmOverloads
                fun addPart(
                    context: Context,
                    key: String,
                    uri: Uri,
                    contentType: MediaType? = BuildUtil.getMediaTypeByUri(context, uri)
                ) = apply {
                    param.addPart(uri.asPart(context, key, skipSize = 0, contentType = contentType))
                }
            
                @JvmOverloads
                fun addPart(
                    context: Context,
                    key: String,
                    filename: String?,
                    uri: Uri,
                    contentType: MediaType? = BuildUtil.getMediaTypeByUri(context, uri)
                ) = apply {
                    param.addPart(uri.asPart(context, key, filename, 0, contentType))
                }
            
                fun addParts(context: Context, uriMap: Map<String, Uri>) = apply {
                    uriMap.forEach { key, value -> addPart(context, key, value) }
                }
            
                fun addParts(context: Context, uris: List<Uri>) = apply {
                    uris.forEach { addPart(context, it) }
                }
                
                fun addParts(context: Context, uris: List<Uri>, contentType: MediaType?) = apply {
                    uris.forEach { addPart(context, it, contentType) }
                }
                
                fun addParts(context: Context, key: String, uris: List<Uri>) = apply {
                    uris.forEach { addPart(context, key, it) }
                }
                
                fun addParts(context: Context, key: String, uris: List<Uri>, contentType: MediaType?) = apply {
                    uris.forEach { addPart(context, key, it, contentType) }
                }
                """)}
                fun addPart(part: MultipartBody.Part) = apply { param.addPart(part) }
            
                fun addPart(requestBody: RequestBody) = apply { param.addPart(requestBody) }
            
                fun addPart(headers: Headers?, requestBody: RequestBody) = apply {
                    param.addPart(headers, requestBody)
                }
            
                fun addFormDataPart(
                    key: String,
                    fileName: String?,
                    requestBody: RequestBody
                ) = apply { param.addFormDataPart(key, fileName, requestBody) }
            
                //Set content-type to multipart/form-data
                fun setMultiForm() = apply { param.setMultiForm() }
            
                //Set content-type to multipart/mixed
                fun setMultiMixed() = apply { param.setMultiMixed() }
            
                //Set content-type to multipart/alternative
                fun setMultiAlternative() = apply { param.setMultiAlternative() }
            
                //Set content-type to multipart/digest
                fun setMultiDigest() = apply { param.setMultiDigest() }
            
                //Set content-type to multipart/parallel
                fun setMultiParallel() = apply { param.setMultiParallel() }
            
                //Set the MIME type
                fun setMultiType(multiType: MediaType?) = apply { param.setMultiType(multiType) }
            }

        """.trimIndent()
        )
    }

    private fun generatorRxHttpJsonParam(codeGenerator: CodeGenerator) {
        generatorClass(
            codeGenerator, "RxHttpJsonParam", """
            package $rxHttpPackage

            import com.google.gson.JsonObject
            
            import rxhttp.wrapper.param.JsonParam
            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            open class RxHttpJsonParam(param: JsonParam) : RxHttpAbstractBodyParam<JsonParam, RxHttpJsonParam>(param) {
            
                @JvmOverloads
                fun add(key: String, value: Any?, isAdd: Boolean = true) = apply {
                    if (isAdd) param.add(key, value)
                }
            
                fun addAll(map: Map<String, *>) = apply { param.addAll(map) }
            
                /**
                 * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中，
                 * 输入非Json对象将抛出[IllegalStateException]异常
                 */
                fun addAll(jsonObject: String) = apply { param.addAll(jsonObject) }
            
                /**
                 * 将Json对象里面的key-value逐一取出，添加到另一个Json对象中
                 */
                fun addAll(jsonObject: JsonObject) = apply { param.addAll(jsonObject) }
            
                /**
                 * 添加一个JsonElement对象(Json对象、json数组等)
                 */
                fun addJsonElement(key: String, jsonElement: String) = apply {
                    param.addJsonElement(key, jsonElement)
                }
            }

        """.trimIndent()
        )
    }

    private fun generatorRxHttpJsonArrayParam(codeGenerator: CodeGenerator) {
        generatorClass(
            codeGenerator, "RxHttpJsonArrayParam", """
            package $rxHttpPackage

            import com.google.gson.JsonArray
            import com.google.gson.JsonObject
            
            import rxhttp.wrapper.param.JsonArrayParam

            /**
             * Github
             * https://github.com/liujingxing/rxhttp
             * https://github.com/liujingxing/rxlife
             * https://github.com/liujingxing/rxhttp/wiki/FAQ
             * https://github.com/liujingxing/rxhttp/wiki/更新日志
             */
            open class RxHttpJsonArrayParam(param: JsonArrayParam) : RxHttpAbstractBodyParam<JsonArrayParam, RxHttpJsonArrayParam>(param) {
            
                @JvmOverloads
                fun add(key: String, value: Any?, isAdd: Boolean = true) = apply {
                    if (isAdd) param.add(key, value)
                }
            
                fun addAll(map: Map<String, *>) = apply { param.addAll(map) }
            
                fun add(any: Any) = apply { param.add(any) }
            
                fun addAll(list: List<*>) = apply {  param.addAll(list) }
            
                /**
                 * 添加多个对象，将字符串转JsonElement对象,并根据不同类型,执行不同操作,可输入任意非空字符串
                 */
                fun addAll(jsonElement: String) = apply { param.addAll(jsonElement) }
            
                fun addAll(jsonArray: JsonArray) = apply { param.addAll(jsonArray) }
            
                /**
                 * 将Json对象里面的key-value逐一取出，添加到Json数组中，成为单独的对象
                 */
                fun addAll(jsonObject: JsonObject) = apply { param.addAll(jsonObject) }
            
                fun addJsonElement(jsonElement: String) = apply { param.addJsonElement(jsonElement) }
            
                /**
                 * 添加一个JsonElement对象(Json对象、json数组等)
                 */
                fun addJsonElement(key: String, jsonElement: String) = apply {
                    param.addJsonElement(key, jsonElement)
                }
            }

        """.trimIndent()
        )
    }

    private fun generatorClass(codeGenerator: CodeGenerator, className: String, content: String) {
        codeGenerator.createNewFile(
            Dependencies(false, *ksFiles.toTypedArray()),
            rxHttpPackage,
            className,
        ).use {
            it.write(content.toByteArray())
        }
    }
}