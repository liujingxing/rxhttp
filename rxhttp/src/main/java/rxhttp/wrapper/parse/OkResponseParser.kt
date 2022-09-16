package rxhttp.wrapper.parse

import okhttp3.Response
import okhttp3.ResponseBody
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.entity.NoContentResponseBody
import rxhttp.wrapper.entity.OkResponse
import java.io.IOException

/**
 * User: ljx
 * Date: 2022/9/5
 * Time: 15:14
 */
class OkResponseParser<T>(val parser: Parser<T>) : Parser<OkResponse<T>> {

    @Throws(IOException::class)
    override fun onParse(response: Response): OkResponse<T> {
        var rawResponse = response
        val rawBody = response.body!!
        // Remove the body's source (the only stateful object) so we can pass the response along.
        rawResponse = rawResponse
            .newBuilder()
            .body(NoContentResponseBody(rawBody.contentType(), rawBody.contentLength()))
            .build()

        val code = rawResponse.code
        if (code < 200 || code >= 300) {
            return rawBody.use {
                // Buffer the entire body to avoid future I/O.
                val bufferedBody: ResponseBody = OkHttpCompat.buffer(it)
                OkResponse.error(bufferedBody, rawResponse)
            }
        }
        if (code == 204 || code == 205) {
            rawBody.close()
            return OkResponse.success(null, rawResponse)
        }
        val body: T = parser.onParse(response)
        return OkResponse.success(body, rawResponse)
    }
}