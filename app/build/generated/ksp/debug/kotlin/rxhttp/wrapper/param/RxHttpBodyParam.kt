package rxhttp.wrapper.param

import android.content.Context
import android.net.Uri
import rxhttp.wrapper.entity.UriRequestBody

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
    
    @JvmOverloads
    fun setBody(
        context: Context,
        uri: Uri,
        contentType: MediaType? = BuildUtil.getMediaTypeByUri(context, uri),
    ) = setBody(UriRequestBody(context, uri, 0, contentType))
    
    fun setBody(any: Any) = apply { param.setBody(any) }

    fun setBody(requestBody: RequestBody) = apply { param.setBody(requestBody) }
}
