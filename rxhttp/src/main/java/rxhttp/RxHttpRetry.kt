package rxhttp

import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import rxhttp.wrapper.parse.Parser

/**
 * 失败重试
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
class RxHttpRetry(
    iRxHttp: IRxHttp,
    private var times: Int = 0,
    private val period: Long = 0L,
    private val test: ((Throwable) -> Boolean)? = null
) : RxHttpProxy(iRxHttp) {

    override suspend fun <T> await(client: OkHttpClient, request: Request, parser: Parser<T>): T {
        return try {
            iRxHttp.await(client, request, parser)
        } catch (e: Throwable) {
            val remaining = times  //剩余次数
            if (remaining != Int.MAX_VALUE) {
                times = remaining - 1
            }
            val pass = test?.invoke(e) ?: true
            if (remaining > 0 && pass) {
                delay(period)
                await(client, request, parser) //递归，直到剩余次数为0
            } else throw e
        }
    }
}

