package rxhttp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import rxhttp.wrapper.await.*
import kotlin.coroutines.CoroutineContext

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
    test: (suspend (Throwable) -> Boolean)? = null
): IAwait<T> = AwaitRetry(this, times, period, test)

fun <T> IAwait<T>.flowOn(context: CoroutineContext): IAwait<T> = AwaitFlowOn(this, context)

fun <T> IAwait<out MutableList<T>>.insert(element: T) = insert(-1, element)

fun <T> IAwait<out MutableList<T>>.insert(
    index: Int,
    element: T
): IAwait<MutableList<T>> = AwaitInsert(this, index, element)

fun <T> IAwait<out MutableList<T>>.insertAll(elements: Collection<T>) = insertAll(-1, elements)

fun <T> IAwait<out MutableList<T>>.insertAll(
    index: Int,
    elements: Collection<T>
): IAwait<MutableList<T>> = AwaitInsertAll(this, index, elements)

fun <T> IAwait<out Iterable<T>>.distinct(): IAwait<List<T>> = AwaitDistinct(this)

fun <T, K> IAwait<out Iterable<T>>.distinctBy(selector: (T) -> K): IAwait<List<T>> = AwaitDistinctBy(this, selector)

fun <T : Comparable<T>> IAwait<out MutableList<T>>.sort(): IAwait<MutableList<T>> = AwaitSort(this)

fun <T : Comparable<T>> IAwait<out MutableList<T>>.sortDescending(): IAwait<MutableList<T>> = sortWith(reverseOrder())

fun <T> IAwait<out MutableList<T>>.sortBy(
    vararg selectors: (T) -> Comparable<*>?
): IAwait<MutableList<T>> = sortWith(compareBy(*selectors))

inline fun <T, R : Comparable<R>> IAwait<out MutableList<T>>.sortBy(
    crossinline selector: (T) -> R?
): IAwait<MutableList<T>> = sortWith(compareBy(selector))

inline fun <T, R : Comparable<R>> IAwait<out MutableList<T>>.sortByDescending(
    crossinline selector: (T) -> R?
): IAwait<MutableList<T>> = sortWith(compareByDescending(selector))

fun <T> IAwait<out MutableList<T>>.sortWith(
    comparator: (T, T) -> Int
): IAwait<MutableList<T>> = sortWith(Comparator { t1, t2 -> comparator(t1, t2) })

fun <T> IAwait<out MutableList<T>>.sortWith(
    comparator: Comparator<in T>
): IAwait<MutableList<T>> = AwaitSortWith(this, comparator)

fun <T : Comparable<T>> IAwait<out Iterable<T>>.sorted(): IAwait<List<T>> = AwaitSorted(this)

fun <T : Comparable<T>> IAwait<out Iterable<T>>.sortedDescending(): IAwait<List<T>> = sortedWith(reverseOrder())

fun <T> IAwait<out Iterable<T>>.sortedBy(
    vararg selectors: (T) -> Comparable<*>?
): IAwait<List<T>> = sortedWith(compareBy(*selectors))

inline fun <T, R : Comparable<R>> IAwait<out Iterable<T>>.sortedBy(
    crossinline selector: (T) -> R?
): IAwait<List<T>> = sortedWith(compareBy(selector))

inline fun <T, R : Comparable<R>> IAwait<out Iterable<T>>.sortedByDescending(
    crossinline selector: (T) -> R?
): IAwait<List<T>> = sortedWith(compareByDescending(selector))

fun <T> IAwait<out Iterable<T>>.sortedWith(
    comparator: (T, T) -> Int
): IAwait<List<T>> = sortedWith(Comparator { t1, t2 -> comparator(t1, t2) })

fun <T> IAwait<out Iterable<T>>.sortedWith(
    comparator: Comparator<in T>
): IAwait<List<T>> = AwaitSortedWith(this, comparator)

/**
 * 为单个请求设置超时时长，该方法仅在使用协程时才有效
 * @param timeMillis 时长 单位: milliseconds
 * 注意: 要保证 timeMillis < OkHttp全局超时(连接+读+写)之和，否则无效
 */
fun <T> IAwait<T>.timeout(timeMillis: Long): IAwait<T> = AwaitTimeout(this, timeMillis)

fun <T> IAwait<T>.onErrorReturn(map: suspend (Throwable) -> T): IAwait<T> = AwaitErrorReturn(this, map)

fun <T> IAwait<T>.onErrorReturnItem(t: T): IAwait<T> = onErrorReturn { t }

fun <T, R> IAwait<T>.map(map: suspend (T) -> R): IAwait<R> = AwaitMap(this, map)

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