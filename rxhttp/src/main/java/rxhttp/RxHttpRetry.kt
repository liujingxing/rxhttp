package rxhttp

import kotlinx.coroutines.delay
import okhttp3.Headers
import okhttp3.Request
import rxhttp.wrapper.parse.*

/**
 * 失败重试
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
class RxHttpRetry(
    val baseRxHttp: BaseRxHttp,
    var times: Int = Int.MAX_VALUE,
    val period: Long = 0,
    val test: ((Throwable) -> Boolean)? = null
)

/**
 * @param times  重试次数，默认Int.MAX_VALUE 代表不断重试
 * @param period 重试周期，默认为0，单位: milliseconds
 * @param test   重试条件, 默认为空，即无条件重试
 */
fun BaseRxHttp.retry(
    times: Int = Int.MAX_VALUE,
    period: Long = 0,
    test: ((Throwable) -> Boolean)? = null
) = RxHttpRetry(this, times, period, test)

suspend fun RxHttpRetry.awaitBitmap() = await(BitmapParser())

suspend inline fun <reified T : Any> RxHttpRetry.await() = await(object : SimpleParser<T>() {})

suspend inline fun <reified T : Any> RxHttpRetry.awaitList() = await<List<T>>()

suspend inline fun <reified K : Any, reified V : Any> RxHttpRetry.awaitMap() = await<Map<K, V>>()

suspend fun RxHttpRetry.awaitHeaders(): Headers = awaitOkResponse().headers()

suspend fun RxHttpRetry.awaitOkResponse() = await(OkResponseParser())

suspend fun <T> RxHttpRetry.await(parser: Parser<T>) = await(baseRxHttp.buildRequest(), parser)

/**
 * @param request [okhttp3.Request] 对象，用于构建 [okhttp3.Call]对象
 * @param parser 解析器
 */
private suspend fun <T> RxHttpRetry.await(
    request: Request,
    parser: Parser<T>
): T {
    return try {
        HttpSender.newCall(request).await(parser)
    } catch (e: Throwable) {
        //剩余次数
        val remaining = times
        if (remaining != Int.MAX_VALUE) {
            times = remaining - 1
        }
        val pass = test?.invoke(e) ?: true
        if (remaining > 0 && pass) {
            delay(period)
            await(request, parser)
        } else throw e
    }
}