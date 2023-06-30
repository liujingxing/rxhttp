package com.rxhttp.compiler.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSFile
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
        generatorRxHttpAbstractBodyParam(codeGenerator)
        generatorRxHttpBodyParam(codeGenerator)
        generatorRxHttpFormParam(codeGenerator)
        generatorRxHttpNoBodyParam(codeGenerator)
        generatorRxHttpJsonParam(codeGenerator)
        generatorRxHttpJsonArrayParam(codeGenerator)
    }

    private fun generatorRxHttpAbstractBodyParam(codeGenerator: CodeGenerator) {
        generatorClass(
            codeGenerator, "RxHttpAbstractBodyParam", """
                package $rxHttpPackage
                
                import rxhttp.wrapper.BodyParamFactory
                import rxhttp.wrapper.param.AbstractBodyParam

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
                fun add(key: String, value: Any?, add: Boolean = true) = apply {
                    if (add) addQuery(key, value)
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
            import rxhttp.wrapper.entity.UriRequestBody
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
                    contentType: MediaType? = null,
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
                ) = setBody(UriRequestBody(context, uri, 0, contentType))
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
            ${isAndroid("import rxhttp.wrapper.entity.UriRequestBody")}
            import okhttp3.Headers
            import okhttp3.MediaType
            import okhttp3.MultipartBody
            import okhttp3.RequestBody
            import rxhttp.wrapper.OkHttpCompat
            import rxhttp.wrapper.entity.FileRequestBody
            import rxhttp.wrapper.entity.UpFile
            import rxhttp.wrapper.param.FormParam
            import rxhttp.wrapper.utils.BuildUtil
            import rxhttp.wrapper.utils.displayName
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
                fun add(key: String, value: Any?, add: Boolean = true) = apply {
                    if (add) param.add(key, value)
                }    

                fun addAll(map: Map<String, *>) = apply { param.addAll(map) }

                fun addEncoded(key: String, value: Any?) = apply { param.addEncoded(key, value) }

                fun addAllEncoded(map: Map<String, *>) = apply { param.addAllEncoded(map) }

                fun removeAllBody() = apply { param.removeAllBody() }

                fun removeAllBody(key: String) = apply { param.removeAllBody(key) }

                fun set(key: String, value: Any?) = apply { param[key] = value }

                fun setEncoded(key: String, value: Any?) = apply { param.setEncoded(key, value) }

                fun addFile(key: String, filePath: String?) = 
                    if (filePath == null) this else addFile(key, File(filePath))

                @JvmOverloads
                fun addFile(key: String, file: File?, filename: String? = file?.name) =
                    if (file == null) this else addFile(UpFile(key, file, filename))

                fun addFiles(fileList: List<UpFile>) = apply { fileList.forEach { addFile(it) } }

                fun <T> addFiles(fileMap: Map<String, T>) = apply {
                    fileMap.forEach { key, value -> addFile(key, value) }
                }

                fun <T> addFiles(key: String, files: List<T>) = apply {
                    files.forEach { addFile(key, it) }
                }

                private fun addFile(key: String, file: Any?) {
                    if (file is File) {
                        addFile(key, file)
                    } else if (file is String) {
                        addFile(key, file)
                    } else if (file != null) {
                        throw IllegalArgumentException("Incoming data type exception, it must be String or File")
                    }
                }

                fun addFile(upFile: UpFile) = apply {
                    val requestBody = FileRequestBody(upFile.file, upFile.skipSize, BuildUtil.getMediaType(upFile.filename))
                    return addFormDataPart(upFile.key, upFile.filename, requestBody)
                }

                @JvmOverloads
                fun addPart(
                    content: ByteArray,
                    contentType: MediaType? = null,
                    offset: Int = 0,
                    byteCount: Int = content.size
                ) = addPart(OkHttpCompat.create(contentType, content, offset, byteCount))
                ${isAndroid("""
                @JvmOverloads
                fun addPart(
                    context: Context,
                    uri: Uri,
                    contentType: MediaType? = BuildUtil.getMediaTypeByUri(context, uri)
                ) = addPart(UriRequestBody(context, uri, 0, contentType))

                @JvmOverloads
                fun addPart(
                    context: Context,
                    key: String,
                    uri: Uri,
                    contentType: MediaType? = BuildUtil.getMediaTypeByUri(context, uri)
                ) = addPart(context, key, uri.displayName(context), uri, contentType)

                @JvmOverloads
                fun addPart(
                    context: Context,
                    key: String,
                    filename: String?,
                    uri: Uri,
                    contentType: MediaType? = BuildUtil.getMediaTypeByUri(context, uri)
                ) = addFormDataPart(key, filename, UriRequestBody(context, uri, 0, contentType))

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
                fun addPart(requestBody: RequestBody) = addPart(OkHttpCompat.part(requestBody))

                fun addPart(headers: Headers?, requestBody: RequestBody) =
                    addPart(OkHttpCompat.part(headers, requestBody))

                fun addFormDataPart(
                    key: String,
                    fileName: String?,
                    requestBody: RequestBody
                ) = addPart(OkHttpCompat.part(key, fileName, requestBody))

                fun addPart(part: MultipartBody.Part) = apply { param.addPart(part) }

                //Set content-type to multipart/form-data
                fun setMultiForm() = setMultiType(MultipartBody.FORM)

                //Set content-type to multipart/mixed
                fun setMultiMixed() = setMultiType(MultipartBody.MIXED)

                //Set content-type to multipart/alternative
                fun setMultiAlternative() = setMultiType(MultipartBody.ALTERNATIVE)

                //Set content-type to multipart/digest
                fun setMultiDigest() = setMultiType(MultipartBody.DIGEST)

                //Set content-type to multipart/parallel
                fun setMultiParallel() = setMultiType(MultipartBody.PARALLEL)

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
                fun add(key: String, value: Any?, add: Boolean = true) = apply {
                    if (add) param.add(key, value)
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
                fun add(key: String, value: Any?, add: Boolean = true) = apply {
                    if (add) param.add(key, value)
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