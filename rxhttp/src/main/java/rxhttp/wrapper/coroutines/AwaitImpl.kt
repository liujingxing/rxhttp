package rxhttp.wrapper.coroutines

import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.utils.LogUtil
import rxhttp.wrapper.utils.await

/**
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
internal class AwaitImpl<T>(
    private val callFactory: CallFactory,
    private val parser: Parser<T>,
) : Await<T> {

    override suspend fun await(): T {
        val call = callFactory.newCall()
        return try {
            call.await(parser)
        } catch (t: Throwable) {
            LogUtil.log(OkHttpCompat.url(call.request()).toString(), t)
            throw t
        }
    }
}