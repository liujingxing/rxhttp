package rxhttp.wrapper.await

import rxhttp.IAwait
import rxhttp.IRxHttp
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.parse.SuspendParser
import rxhttp.wrapper.utils.await

/**
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
@Suppress("BlockingMethodInNonBlockingContext")
internal class AwaitImpl<T>(
    private val iRxHttp: IRxHttp,
    private val parser: Parser<T>,
) : IAwait<T> {

    override suspend fun await(): T {
        val call = iRxHttp.newCall()
        return if (parser is SuspendParser) {
            parser.onSuspendParse(call.await())
        } else {
            call.await(parser)
        }
    }
}