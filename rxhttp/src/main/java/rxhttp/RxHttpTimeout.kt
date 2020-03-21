package rxhttp

import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import rxhttp.wrapper.parse.Parser

/**
 * 请求超时处理
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
class RxHttpTimeout(
    baseRxHttp: BaseRxHttp,
    private var timeoutMillis: Long = 0L
) : RxHttpProxy(baseRxHttp) {

    override suspend fun <T> await(client: OkHttpClient, parser: Parser<T>) =
        withTimeout(timeoutMillis) { baseRxHttp.await(client, parser) }
}