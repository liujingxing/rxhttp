package rxhttp.wrapper.param

import android.content.Context
import android.net.Uri
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

    @Deprecated(
        "use `addFiles(List)` instead, scheduled to be removed in RxHttp 3.0 release.",
        ReplaceWith("addFiles(fileList)"),
        DeprecationLevel.WARNING
    )
    fun addFile(fileList: List<UpFile>) = addFiles(fileList)

    @Deprecated(
        "use `addFiles(String, List)` instead, scheduled to be removed in RxHttp 3.0 release.",
        ReplaceWith("addFiles(key, fileList)"),
        DeprecationLevel.WARNING
    )
    fun <T> addFile(key: String, fileList: List<T>) = addFiles(key, fileList)

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
