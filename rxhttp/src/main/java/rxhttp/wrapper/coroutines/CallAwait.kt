package rxhttp.wrapper.coroutines

import okhttp3.Call
import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.entity.OkResponse
import rxhttp.wrapper.parse.OkResponseParser
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.utils.LogUtil
import rxhttp.wrapper.utils.await

/**
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
class CallAwait<T>(
    private val callFactory: CallFactory,
    private val parser: Parser<T>,
) : Await<T> {

    fun toAwaitOkResponse(): CallAwait<OkResponse<T?>> =
        CallAwait(callFactory, OkResponseParser(parser))

    override suspend fun await(): T {
        var call: Call? = null
        return try {
            call = callFactory.newCall()
            call.await(parser)
        } catch (e: Throwable) {
            LogUtil.logCall(call, e)
            throw e
        }
    }
}