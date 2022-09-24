package rxhttp.wrapper.param

import okhttp3.RequestBody

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

    //Content-Type: application/json; charset=utf-8
    private var body: Any? = null
    //The Content-Type depends on the RequestBody
    private var requestBody: RequestBody? = null

    fun setBody(value: Any): BodyParam {
        body = value
        requestBody = null
        return this
    }

    fun setBody(requestBody: RequestBody): BodyParam {
        this.requestBody = requestBody
        body = null
        return this
    }

    override fun getRequestBody(): RequestBody {
        if (body != null) requestBody = convert(body)
        return requestBody ?: throw NullPointerException("requestBody cannot be null, please call the setBody series methods")
    }

    override fun add(key: String, value: Any) = this
}