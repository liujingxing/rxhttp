package rxhttp.wrapper.param

import android.content.Context
import android.net.Uri
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.ByteString
import rxhttp.wrapper.entity.UriRequestBody
import rxhttp.wrapper.utils.BuildUtil
import java.io.File

/**
 * User: ljx
 * Date: 2019-09-11
 * Time: 11:52
 *
 * @param url    request url
 * @param method [Method.POST]、[Method.PUT]、[Method.DELETE]、[Method.PATCH]
 */
class BodyParam(
    url: String,
    method: Method,
) : AbstractBodyParam<BodyParam>(url, method) {

    private var requestBody: RequestBody? = null

    fun setBody(requestBody: RequestBody): BodyParam {
        this.requestBody = requestBody
        return this
    }

    @JvmOverloads
    fun setBody(content: String, mediaType: MediaType? = null): BodyParam {
        requestBody = RequestBody.create(mediaType, content)
        return this
    }

    @JvmOverloads
    fun setBody(content: ByteString, mediaType: MediaType? = null): BodyParam {
        requestBody = RequestBody.create(mediaType, content)
        return this
    }

    @JvmOverloads
    fun setBody(
        content: ByteArray,
        mediaType: MediaType? = null,
        offset: Int = 0,
        byteCount: Int = content.size,
    ): BodyParam {
        requestBody = RequestBody.create(mediaType, content, offset, byteCount)
        return this
    }

    @JvmOverloads
    fun setBody(
        file: File,
        mediaType: MediaType? = BuildUtil.getMediaType(file.name),
    ): BodyParam {
        requestBody = RequestBody.create(mediaType, file)
        return this
    }

    @JvmOverloads
    fun setBody(
        context: Context,
        uri: Uri,
        contentType: MediaType? = null,
    ): BodyParam {
        requestBody = UriRequestBody(context, uri, contentType)
        return this
    }

    fun <T> setJsonBody(any: T): BodyParam {
        requestBody = convert(any)
        return this
    }

    override fun getRequestBody(): RequestBody {
        return requestBody!!
    }

    override fun add(key: String, value: Any): BodyParam {
        return this
    }
}