package rxhttp.wrapper.param

import android.content.Context
import android.net.Uri

import java.io.File

import okhttp3.Headers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Part
import okhttp3.RequestBody
import rxhttp.wrapper.annotations.NonNull
import rxhttp.wrapper.annotations.Nullable
import rxhttp.wrapper.entity.UpFile
import rxhttp.wrapper.param.FormParam
import rxhttp.wrapper.utils.asPart
import rxhttp.wrapper.utils.asRequestBody
import rxhttp.wrapper.utils.displayName

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 * https://github.com/liujingxing/rxhttp/wiki/FAQ
 * https://github.com/liujingxing/rxhttp/wiki/更新日志
 */
open class RxHttpFormParam(param: FormParam) : RxHttpAbstractBodyParam<FormParam, RxHttpFormParam>(param) {
    
    @JvmOverloads
    fun add(key: String, value: Any?, isAdd: Boolean = true): RxHttpFormParam {
        if (isAdd) param.add(key, value)
        return this
    }

    fun addAll(map: Map<String, *>): RxHttpFormParam {
        param.addAll(map)
        return this
    }

    fun addEncoded(key: String, value: Any?): RxHttpFormParam {
        param.addEncoded(key, value)
        return this
    }

    fun addAllEncoded(map: Map<String, *>): RxHttpFormParam {
        param.addAllEncoded(map)
        return this
    }

    fun removeAllBody(): RxHttpFormParam {
        param.removeAllBody()
        return this
    }

    fun removeAllBody(key: String): RxHttpFormParam {
        param.removeAllBody(key)
        return this
    }

    operator fun set(key: String, value: Any?): RxHttpFormParam {
        param[key] = value
        return this
    }

    fun setEncoded(key: String, value: Any?): RxHttpFormParam {
        param.setEncoded(key, value)
        return this
    }

    fun addFile(key: String, file: File): RxHttpFormParam {
        param.addFile(key, file)
        return this
    }

    fun addFile(key: String, filePath: String): RxHttpFormParam {
        param.addFile(key, filePath)
        return this
    }

    fun addFile(key: String, file: File, filename: String): RxHttpFormParam {
        param.addFile(key, file, filename)
        return this
    }

    fun addFile(file: UpFile): RxHttpFormParam {
        param.addFile(file)
        return this
    }

    @Deprecated("please user {@link #addFiles(List)} instead")
    fun addFile(fileList: List<UpFile>): RxHttpFormParam {
        return addFiles(fileList)
    }

    @Deprecated("please user {@link #addFiles(String, List)} instead")
    fun <T> addFile(key: String, fileList: List<T>): RxHttpFormParam {
        return addFiles(key, fileList)
    }

    fun addFiles(fileList: List<UpFile>): RxHttpFormParam {
        param.addFiles(fileList)
        return this
    }

    fun <T> addFiles(fileMap: Map<String, T>): RxHttpFormParam {
        param.addFiles(fileMap)
        return this
    }

    fun <T> addFiles(key: String, fileList: List<T>): RxHttpFormParam {
        param.addFiles(key, fileList)
        return this
    }

    fun addPart(contentType: MediaType?, content: ByteArray): RxHttpFormParam {
        param.addPart(contentType, content)
        return this
    }

    fun addPart(
        contentType: MediaType?, 
        content: ByteArray, 
        offset: Int,
        byteCount: Int
    ): RxHttpFormParam {
        param.addPart(contentType, content, offset, byteCount)
        return this
    }
    
    fun addPart(context: Context, uri: Uri): RxHttpFormParam {
        param.addPart(uri.asRequestBody(context))
        return this
    }

    fun addPart(context: Context, key: String, uri: Uri): RxHttpFormParam {
        param.addPart(uri.asPart(context, key))
        return this
    }

    fun addPart(context: Context, key: String, fileName: String?, uri: Uri): RxHttpFormParam {
        param.addPart(uri.asPart(context, key, fileName))
        return this
    }

    fun addPart(context: Context, uri: Uri, contentType: MediaType?): RxHttpFormParam {
        param.addPart(uri.asRequestBody(context, 0, contentType))
        return this
    }

    fun addPart(
        context: Context,
        key: String,
        uri: Uri,
        contentType: MediaType?
    ): RxHttpFormParam {
        param.addPart(uri.asPart(context, key, uri.displayName(context), 0, contentType))
        return this
    }

    fun addPart(
        context: Context,
        key: String,
        filename: String,
        uri: Uri,
        contentType: MediaType?
    ): RxHttpFormParam {
        param.addPart(uri.asPart(context, key, filename, 0, contentType))
        return this
    }

    fun addParts(context: Context, uriMap: Map<String, Uri>): RxHttpFormParam {
        for ((key, value) in uriMap) {
            addPart(context, key, value)
        }
        return this
    }

    fun addParts(context: Context, uris: List<Uri>): RxHttpFormParam {
        for (uri in uris) {
            addPart(context, uri)
        }
        return this
    }

    fun addParts(context: Context, key: String, uris: List<Uri>): RxHttpFormParam {
        for (uri in uris) {
            addPart(context, key, uri)
        }
        return this
    }

    fun addParts(
        context: Context, 
        uris: List<Uri>,
        contentType: MediaType?
    ): RxHttpFormParam {
        for (uri in uris) {
            addPart(context, uri, contentType)
        }
        return this
    }

    fun addParts(
        context: Context,
        key: String, 
        uris: List<Uri>,
        contentType: MediaType?
    ): RxHttpFormParam {
        for (uri in uris) {
            addPart(context, key, uri, contentType)
        }
        return this
    }
    
    fun addPart(part: MultipartBody.Part): RxHttpFormParam {
        param.addPart(part)
        return this
    }

    fun addPart(requestBody: RequestBody?): RxHttpFormParam {
        param.addPart(requestBody)
        return this
    }

    fun addPart(headers: Headers?, requestBody: RequestBody?): RxHttpFormParam {
        param.addPart(headers, requestBody)
        return this
    }

    fun addFormDataPart(
        key: String,
        fileName: String?,
        requestBody: RequestBody?
    ): RxHttpFormParam {
        param.addFormDataPart(key, fileName, requestBody)
        return this
    }

    //Set content-type to multipart/form-data
    fun setMultiForm(): RxHttpFormParam {
        param.setMultiForm()
        return this
    }

    //Set content-type to multipart/mixed
    fun setMultiMixed(): RxHttpFormParam {
        param.setMultiMixed()
        return this
    }

    //Set content-type to multipart/alternative
    fun setMultiAlternative(): RxHttpFormParam {
        param.setMultiAlternative()
        return this
    }

    //Set content-type to multipart/digest
    fun setMultiDigest(): RxHttpFormParam {
        param.setMultiDigest()
        return this
    }

    //Set content-type to multipart/parallel
    fun setMultiParallel(): RxHttpFormParam {
        param.setMultiParallel()
        return this
    }

    //Set the MIME type
    fun setMultiType(multiType: MediaType?): RxHttpFormParam {
        param.setMultiType(multiType)
        return this
    }
}
