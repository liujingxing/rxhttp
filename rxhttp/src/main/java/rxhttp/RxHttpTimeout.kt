package rxhttp

import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import rxhttp.wrapper.parse.Parser

/**
 * 请求超时处理
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
class RxHttpTimeout(
    iRxHttp: IRxHttp,
    private var timeoutMillis: Long = 0L
) : RxHttpProxy(iRxHttp) {

    override suspend fun <T> await(client: OkHttpClient, request: Request, parser: Parser<T>) =
        withTimeout(timeoutMillis) { iRxHttp.await(client, request, parser) }
}