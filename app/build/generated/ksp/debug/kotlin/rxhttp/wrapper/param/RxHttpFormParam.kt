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
    
    fun add(key: String, value: Any?) = apply { param.add(key, value) }

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
