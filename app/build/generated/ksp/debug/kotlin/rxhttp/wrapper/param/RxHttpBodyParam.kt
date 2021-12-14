package rxhttp.wrapper.param

import android.content.Context
import android.net.Uri
import rxhttp.wrapper.utils.asRequestBody

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.ByteString
import rxhttp.wrapper.param.BodyParam
import rxhttp.wrapper.utils.BuildUtil
import java.io.File

/**
 * Github
 * https://github.com/liujingxing/rxhttp
 * https://github.com/liujingxing/rxlife
 */
open class RxHttpBodyParam(param: BodyParam) : RxHttpAbstractBodyParam<BodyParam, RxHttpBodyParam>(param) {
    
    fun setBody(requestBody: RequestBody) = apply { param.setBody(requestBody) }

    fun setBody(content: String, contentType: MediaType? = null) = apply {
        param.setBody(content, contentType)
    }
    
    fun setBody(content: ByteString, contentType: MediaType? = null) = apply {
        param.setBody(content, contentType)
    }

    @JvmOverloads
    fun setBody(
        content: ByteArray,
        contentType: MediaType?,
        offset: Int = 0,
        byteCount: Int = content.size,
    ) = apply { param.setBody(content, contentType, offset, byteCount) }

    @JvmOverloads
    fun setBody(
        file: File,
        contentType: MediaType? = BuildUtil.getMediaType(file.name),
    ) = apply { param.setBody(file, contentType) }
    
    @JvmOverloads
    fun setBody(
        uri: Uri,
        context: Context,
        contentType: MediaType? = BuildUtil.getMediaTypeByUri(context, uri),
    ) = apply { param.setBody(uri.asRequestBody(context, 0, contentType)) }
    
    fun setBody(any: Any) = apply { param.setBody(any) }

    @Deprecated(
        message = "use `setBody(Any)` instead",
        replaceWith = ReplaceWith("setBody(any)"),
        level = DeprecationLevel.ERROR
    )
    fun setJsonBody(any: Any) = setBody(any)
}
