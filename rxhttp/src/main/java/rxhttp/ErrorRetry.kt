package rxhttp

import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import okhttp3.Headers
import okhttp3.Request
import rxhttp.wrapper.parse.BitmapParser
import rxhttp.wrapper.parse.OkResponseParser
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.parse.SimpleParser

/**
 * 失败重试/超时处理
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
class ErrorRetry(
    internal val baseRxHttp: BaseRxHttp
) {
    internal var times = 0
    internal var period = 0L
    internal var test: ((Throwable) -> Boolean)? = null
    internal var timeoutMillis = 0L
}

/**
 * @param times  重试次数, 默认Int.MAX_VALUE 代表不断重试
 * @param period 重试周期, 默认为0, 单位: milliseconds
 * @param test   重试条件, 默认为空，即无条件重试
 */
fun BaseRxHttp.retry(
    times: Int = Int.MAX_VALUE,
    period: Long = 0,
    test: ((Throwable) -> Boolean)? = null
) = ErrorRetry(this).retry(times, period, test)

/**
 * @param timeMillis 超时时长  注意: 要保证 timeMillis < OkHttp全局超时(连接+读+写)之和，否则无效
 */
fun BaseRxHttp.timeout(timeMillis: Long) = ErrorRetry(this).timeout(timeMillis)

fun ErrorRetry.timeout(timeMillis: Long): ErrorRetry {
    timeoutMillis = timeMillis
    return this
}

fun ErrorRetry.retry(
    times: Int = Int.MAX_VALUE,
    period: Long = 0,
    test: ((Throwable) -> Boolean)? = null
): ErrorRetry {
    this.times = times
    this.period = period
    this.test = test
    return this
}

suspend fun ErrorRetry.awaitBitmap() = await(BitmapParser())

suspend fun ErrorRetry.awaitHeaders(): Headers = awaitOkResponse().headers()

suspend fun ErrorRetry.awaitOkResponse() = await(OkResponseParser())

suspend inline fun <reified T : Any> ErrorRetry.awaitList() = await<List<T>>()

suspend inline fun <reified K : Any, reified V : Any> ErrorRetry.awaitMap() = await<Map<K, V>>()

suspend inline fun <reified T : Any> ErrorRetry.await() = await(object : SimpleParser<T>() {})

suspend fun <T> ErrorRetry.await(parser: Parser<T>) = awaitRetry(baseRxHttp.buildRequest(), parser)

/**
 * @param request [okhttp3.Request] 对象，用于构建 [okhttp3.Call]对象
 * @param parser 解析器
 */
private suspend fun <T> ErrorRetry.awaitRetry(
    request: Request,
    parser: Parser<T>
): T {
    return try {
        if (timeoutMillis > 0)
            awaitTimeout(request, parser, timeoutMillis)
        else
            await(request, parser)
    } catch (e: Throwable) {
        val remaining = times  //剩余次数
        if (remaining != Int.MAX_VALUE) {
            times = remaining - 1
        }
        val pass = test?.invoke(e) ?: true
        if (remaining > 0 && pass) {
            delay(period)
            awaitRetry(request, parser)
        } else throw e
    }
}

private suspend fun <T> awaitTimeout(
    request: Request,
    parser: Parser<T>,
    timeMillis: Long
) = withTimeout(timeMillis) { await(request, parser) }


private suspend fun <T> await(
    request: Request,
    parser: Parser<T>
) = HttpSender.newCall(request).await(parser)
