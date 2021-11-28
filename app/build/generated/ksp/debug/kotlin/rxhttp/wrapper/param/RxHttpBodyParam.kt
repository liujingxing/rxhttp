package rxhttp.wrapper.param

import android.content.Context
import android.net.Uri
import rxhttp.wrapper.utils.asRequestBody

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.ByteString
import rxhttp.wrapper.annotations.Nullable
import rxhttp.wrapper.param.BodyParam
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

    fun setBody(content: String, @Nullable mediaType: MediaType?): RxHttpBodyParam {
        param.setBody(content, mediaType)
        return this
    }

    fun setBody(content: ByteString, @Nullable mediaType: MediaType?): RxHttpBodyParam {
        param.setBody(content, mediaType)
        return this
    }

    fun setBody(content: ByteArray, @Nullable mediaType: MediaType?): RxHttpBodyParam {
        param.setBody(content, mediaType)
        return this
    }

    fun setBody(
        content: ByteArray,
        @Nullable mediaType: MediaType?,
        offset: Int,
        byteCount: Int
    ): RxHttpBodyParam {
        param.setBody(content, mediaType, offset, byteCount)
        return this
    }

    fun setBody(file: File): RxHttpBodyParam {
        param.setBody(file)
        return this
    }

    fun setBody(file: File, @Nullable mediaType: MediaType?): RxHttpBodyParam {
        param.setBody(file, mediaType)
        return this
    }
    
    fun setBody(uri: Uri, context: Context): RxHttpBodyParam {
        param.setBody(uri.asRequestBody(context))
        return this
    }

    fun setBody(uri: Uri, context: Context, @Nullable contentType: MediaType?): RxHttpBodyParam {
        param.setBody(uri.asRequestBody(context, 0, contentType))
        return this
    }
    

    fun setBody(any: Any): RxHttpBodyParam {
        param.setBody(any)
        return this
    }

    @Deprecated("please user {@link #setBody(Object)} instead")
    fun setJsonBody(any: Any): RxHttpBodyParam {
        return setBody(any)
    }
}
