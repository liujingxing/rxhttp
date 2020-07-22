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

/**
 * 控制上游线程，可调用多次
 * @param context 协程上下文
 */
fun <T> IAwait<T>.flowOn(context: CoroutineContext): IAwait<T> = AwaitFlowOn(this, context)

/**
 * 往集合尾部插入一条数据
 */
fun <T> IAwait<out MutableList<T>>.insert(
    element: T
): IAwait<MutableList<T>> = insert(-1, element)

/**
 * 往集合指定位置插入一条数据
 */
fun <T> IAwait<out MutableList<T>>.insert(
    index: Int,
    element: T
): IAwait<MutableList<T>> = AwaitInsert(this, index, element)

/**
 *  往集合尾部插入多条数据
 */
fun <T> IAwait<out MutableList<T>>.insertAll(
    elements: Collection<T>
): IAwait<MutableList<T>> = insertAll(-1, elements)

/**
 * 往集合指定位置插入多条数据
 */
fun <T> IAwait<out MutableList<T>>.insertAll(
    index: Int,
    elements: Collection<T>
): IAwait<MutableList<T>> = AwaitInsertAll(this, index, elements)

/**
 * 集合去重，根据对象的哈希值去重
 */
fun <T> IAwait<out Iterable<T>>.distinct(): IAwait<List<T>> = AwaitDistinct(this)

/**
 * 集合去重，根据表达式返回值的哈希值去重
 */
fun <T, K> IAwait<out Iterable<T>>.distinctBy(selector: (T) -> K): IAwait<List<T>> = AwaitDistinctBy(this, selector)

/**
 * 顺序、排序对象需要继承Comparable接口，实现排序规则，返回原集合
 */
fun <T : Comparable<T>> IAwait<out MutableList<T>>.sort(): IAwait<MutableList<T>> = AwaitSort(this)

/**
 * 倒序、排序对象需要继承Comparable接口，实现排序规则，返回原集合
 */
fun <T : Comparable<T>> IAwait<out MutableList<T>>.sortDescending(): IAwait<MutableList<T>> = sortWith(reverseOrder())

/**
 * 顺序、多维度排序，返回原集合
 */
fun <T> IAwait<out MutableList<T>>.sortBy(
    vararg selectors: (T) -> Comparable<*>?
): IAwait<MutableList<T>> = sortWith(compareBy(*selectors))

/**
 * 顺序、单维度排序，返回原集合
 */
inline fun <T> IAwait<out MutableList<T>>.sortBy(
    crossinline selector: (T) -> Comparable<*>?
): IAwait<MutableList<T>> = sortWith(compareBy(selector))

/**
 * 倒序、单维度排序，返回原集合
 */
inline fun <T> IAwait<out MutableList<T>>.sortByDescending(
    crossinline selector: (T) -> Comparable<*>?
): IAwait<MutableList<T>> = sortWith(compareByDescending(selector))

/**
 * 顺序、根据传入的表达式返回值进行排序，返回原集合
 */
fun <T> IAwait<out MutableList<T>>.sortWith(
    comparator: (T, T) -> Int
): IAwait<MutableList<T>> = sortWith(Comparator { t1, t2 -> comparator(t1, t2) })

/**
 * 顺序、根据传入Comparator接口对象进行排序，返回原集合
 */
fun <T> IAwait<out MutableList<T>>.sortWith(
    comparator: Comparator<in T>
): IAwait<MutableList<T>> = AwaitSortWith(this, comparator)

/**
 * 顺序、排序对象需要继承Comparable接口，实现排序规则，返回新的集合
 */
fun <T : Comparable<T>> IAwait<out Iterable<T>>.sorted(): IAwait<List<T>> = AwaitSorted(this)

/**
 * 顺序、排序对象需要继承Comparable接口，实现排序规则，返回新的集合
 */
fun <T : Comparable<T>> IAwait<out Iterable<T>>.sortedDescending(): IAwait<List<T>> = sortedWith(reverseOrder())

/**
 * 顺序、多维度排序，返回新的集合
 */
fun <T> IAwait<out Iterable<T>>.sortedBy(
    vararg selectors: (T) -> Comparable<*>?
): IAwait<List<T>> = sortedWith(compareBy(*selectors))

/**
 * 顺序、单维度排序，返回新的集合
 */
inline fun <T> IAwait<out Iterable<T>>.sortedBy(
    crossinline selector: (T) -> Comparable<*>?
): IAwait<List<T>> = sortedWith(compareBy(selector))

/**
 * 倒序、单维度排序，返回新的集合
 */
inline fun <T> IAwait<out Iterable<T>>.sortedByDescending(
    crossinline selector: (T) -> Comparable<*>?
): IAwait<List<T>> = sortedWith(compareByDescending(selector))

/**
 * 顺序、根据传入的表达式返回值进行排序，返回新的集合
 */
fun <T> IAwait<out Iterable<T>>.sortedWith(
    comparator: (T, T) -> Int
): IAwait<List<T>> = sortedWith(Comparator { t1, t2 -> comparator(t1, t2) })

/**
 * 顺序、根据传入Comparator接口对象进行排序，返回新的集合
 */
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