package rxhttp.wrapper.param

import android.content.Context
import android.net.Uri
import rxhttp.wrapper.entity.UriRequestBody
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
    fun add(key: String, value: Any?, add: Boolean = true): RxHttpFormParam = apply {
        if (add) param.add(key, value)
    }    

    fun addAll(map: Map<String, *>): RxHttpFormParam = apply { param.addAll(map) }

    fun addEncoded(key: String, value: Any?): RxHttpFormParam = apply { param.addEncoded(key, value) }

    fun addAllEncoded(map: Map<String, *>): RxHttpFormParam = apply { param.addAllEncoded(map) }

    fun removeAllBody(): RxHttpFormParam = apply { param.removeAllBody() }

    fun removeAllBody(key: String): RxHttpFormParam = apply { param.removeAllBody(key) }

    fun set(key: String, value: Any?): RxHttpFormParam = apply { param[key] = value }

    fun setEncoded(key: String, value: Any?): RxHttpFormParam = apply { param.setEncoded(key, value) }

    fun addFile(key: String, filePath: String?): RxHttpFormParam =
        if (filePath == null) this else addFile(key, File(filePath))

    @JvmOverloads
    fun addFile(key: String, file: File?, filename: String? = file?.name): RxHttpFormParam =
        if (file == null) this else addFile(UpFile(key, file, filename))

    fun addFiles(fileList: List<UpFile>): RxHttpFormParam = apply { fileList.forEach { addFile(it) } }

    fun <T> addFiles(fileMap: Map<String, T>): RxHttpFormParam = apply {
        fileMap.forEach { (key, value) -> addFile(key, value) }
    }

    fun <T> addFiles(key: String, files: List<T>): RxHttpFormParam = apply {
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

    fun addFile(upFile: UpFile): RxHttpFormParam = apply {
        val requestBody = FileRequestBody(upFile.file, upFile.skipSize, BuildUtil.getMediaType(upFile.filename))
        addFormDataPart(upFile.key, upFile.filename, requestBody)
    }

    @JvmOverloads
    fun addPart(
        content: ByteArray,
        contentType: MediaType? = null,
        offset: Int = 0,
        byteCount: Int = content.size
    ): RxHttpFormParam = addPart(OkHttpCompat.create(contentType, content, offset, byteCount))
    
    @JvmOverloads
    fun addPart(
        context: Context,
        uri: Uri,
        contentType: MediaType? = BuildUtil.getMediaTypeByUri(context, uri)
    ): RxHttpFormParam = addPart(UriRequestBody(context, uri, 0, contentType))

    @JvmOverloads
    fun addPart(
        context: Context,
        key: String,
        uri: Uri,
        contentType: MediaType? = BuildUtil.getMediaTypeByUri(context, uri)
    ): RxHttpFormParam = addPart(context, key, uri.displayName(context), uri, contentType)

    @JvmOverloads
    fun addPart(
        context: Context,
        key: String,
        filename: String?,
        uri: Uri,
        contentType: MediaType? = BuildUtil.getMediaTypeByUri(context, uri)
    ): RxHttpFormParam = addFormDataPart(key, filename, UriRequestBody(context, uri, 0, contentType))

    fun addParts(context: Context, uriMap: Map<String, Uri>): RxHttpFormParam = apply {
        uriMap.forEach { (key, value) -> addPart(context, key, value) }
    }

    fun addParts(context: Context, uris: List<Uri>): RxHttpFormParam = apply {
        uris.forEach { addPart(context, it) }
    }

    fun addParts(context: Context, uris: List<Uri>, contentType: MediaType?): RxHttpFormParam = apply {
        uris.forEach { addPart(context, it, contentType) }
    }

    fun addParts(context: Context, key: String, uris: List<Uri>): RxHttpFormParam = apply {
        uris.forEach { addPart(context, key, it) }
    }

    fun addParts(context: Context, key: String, uris: List<Uri>, contentType: MediaType?): RxHttpFormParam = apply {
        uris.forEach { addPart(context, key, it, contentType) }
    }
    
    fun addPart(requestBody: RequestBody): RxHttpFormParam = addPart(OkHttpCompat.part(requestBody))

    fun addPart(headers: Headers?, requestBody: RequestBody): RxHttpFormParam =
        addPart(OkHttpCompat.part(headers, requestBody))

    fun addFormDataPart(
        key: String,
        fileName: String?,
        requestBody: RequestBody
    ): RxHttpFormParam = addPart(OkHttpCompat.part(key, fileName, requestBody))

    fun addPart(key: String, requestBody: RequestBody): RxHttpFormParam = addFormDataPart(key, null, requestBody)
    
    fun addParts(partMap: Map<String, RequestBody>): RxHttpFormParam = apply {
        partMap.forEach { (key, value) -> addPart(key, value) } 
    }

    fun addPart(part: MultipartBody.Part): RxHttpFormParam = apply { param.addPart(part) }
    
    fun addParts(parts: List<MultipartBody.Part>): RxHttpFormParam = apply { parts.forEach { addPart(it) } }

    //Set content-type to multipart/form-data
    fun setMultiForm(): RxHttpFormParam = setMultiType(MultipartBody.FORM)

    //Set content-type to multipart/mixed
    fun setMultiMixed(): RxHttpFormParam = setMultiType(MultipartBody.MIXED)

    //Set content-type to multipart/alternative
    fun setMultiAlternative(): RxHttpFormParam = setMultiType(MultipartBody.ALTERNATIVE)

    //Set content-type to multipart/digest
    fun setMultiDigest(): RxHttpFormParam = setMultiType(MultipartBody.DIGEST)

    //Set content-type to multipart/parallel
    fun setMultiParallel(): RxHttpFormParam = setMultiType(MultipartBody.PARALLEL)

    //Set the MIME type
    fun setMultiType(multiType: MediaType?): RxHttpFormParam = apply { param.multiType = multiType }
}
