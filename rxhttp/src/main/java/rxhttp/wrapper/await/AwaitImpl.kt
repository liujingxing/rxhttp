package rxhttp.wrapper.await

import rxhttp.IAwait
import rxhttp.IRxHttp
import rxhttp.wrapper.parse.Parser

/**
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
internal class AwaitImpl<T>(
    private val iRxHttp: IRxHttp,
    private val parser: Parser<T>,
) : IAwait<T> {

    override suspend fun await(): T {
        return iRxHttp.newCall().await(parser)
    }
}