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
    
    fun setBody(requestBody: RequestBody): RxHttpBodyParam {
        param.setBody(requestBody)
        return this
    }

    fun setBody(content: String, contentType: MediaType? = null): RxHttpBodyParam {
        param.setBody(content, contentType)
        return this
    }
    
    fun setBody(content: ByteString, contentType: MediaType? = null): RxHttpBodyParam {
        param.setBody(content, contentType)
        return this
    }

    @JvmOverloads
    fun setBody(
        content: ByteArray,
        contentType: MediaType?,
        offset: Int = 0,
        byteCount: Int = content.size,
    ): RxHttpBodyParam {
        param.setBody(content, contentType, offset, byteCount)
        return this
    }

    @JvmOverloads
    fun setBody(
        file: File,
        contentType: MediaType? = BuildUtil.getMediaType(file.name),
    ): RxHttpBodyParam {
        param.setBody(file, contentType)
        return this
    }
    
    @JvmOverloads
    fun setBody(
        uri: Uri,
        context: Context,
        contentType: MediaType? = BuildUtil.getMediaTypeByUri(context, uri),
    ): RxHttpBodyParam {
        param.setBody(uri.asRequestBody(context, 0, contentType))
        return this
    }
    
    fun setBody(any: Any): RxHttpBodyParam {
        param.setBody(any)
        return this
    }

    @Deprecated(
        message = "use `setBody(Any)` instead",
        replaceWith = ReplaceWith("setBody(any)"),
        level = DeprecationLevel.ERROR
    )
    fun setJsonBody(any: Any): RxHttpBodyParam {
        return setBody(any)
    }
}
