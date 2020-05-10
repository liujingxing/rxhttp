package rxhttp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import rxhttp.wrapper.await.*

/**
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
interface IAwait<T> {

    suspend fun await(): T
}

/**
 * 失败重试，该方法仅在使用协程时才有效
 * @param times  重试次数, 默认Int.MAX_VALUE 代表不断重试
 * @param period 重试周期, 默认为0, 单位: milliseconds
 * @param test   重试条件, 默认为空，即无条件重试
 */
fun <T> IAwait<T>.retry(
    times: Int = Int.MAX_VALUE,
    period: Long = 0,
    test: ((Throwable) -> Boolean)? = null
): IAwait<T> = AwaitRetry(this, times, period, test)

/**
 * 为单个请求设置超时时长，该方法仅在使用协程时才有效
 * @param timeMillis 时长 单位: milliseconds
 * 注意: 要保证 timeMillis < OkHttp全局超时(连接+读+写)之和，否则无效
 */
fun <T> IAwait<T>.timeout(timeMillis: Long): IAwait<T> = AwaitTimeout(this, timeMillis)

fun <T> IAwait<T>.onErrorReturn(map: (Throwable) -> T): IAwait<T> = AwaitErrorReturn(this, map)

fun <T> IAwait<T>.onErrorReturnItem(t: T): IAwait<T> = onErrorReturn { t }

fun <T, R> IAwait<T>.map(map: (T) -> R): IAwait<R> = AwaitMap(this, map)

fun <T> IAwait<T>.delay(delay: Long): IAwait<T> = AwaitDelay(this, delay)

fun <T> IAwait<T>.startDelay(delay: Long): IAwait<T> = AwaitStartDelay(this, delay)

suspend fun <T> IAwait<T>.async(scope: CoroutineScope) = scope.async { await() }

suspend fun <T> Deferred<T>.tryAwait() = tryAwait { await() }

suspend fun <T> IAwait<T>.tryAwait() = tryAwait { await() }

private suspend fun <T> tryAwait(block: suspend () -> T): T? {
    return try {
        block()
    } catch (e: Throwable) {
        null
    }
}