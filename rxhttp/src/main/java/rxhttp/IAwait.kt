package rxhttp

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.coroutines.CoroutineContext

/**
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
interface IAwait<T> {

    suspend fun await(): T
}

inline fun <T, R> IAwait<T>.newAwait(
    crossinline block: suspend IAwait<T>.() -> R
): IAwait<R> = object : IAwait<R> {

    override suspend fun await(): R {
        return this@newAwait.block()
    }
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
): IAwait<T> = object : IAwait<T> {

    var retryTime = times

    override suspend fun await(): T {
        return try {
            this@retry.await()
        } catch (e: Throwable) {
            val remaining = retryTime  //剩余次数
            if (remaining != Int.MAX_VALUE) {
                retryTime = remaining - 1
            }
            val pass = test?.invoke(e) ?: true
            if (remaining > 0 && pass) {
                kotlinx.coroutines.delay(period)
                await() //递归，直到剩余次数为0
            } else throw e
        }
    }
}

/**
 * 控制上游线程，可调用多次
 * @param context 协程上下文
 */
fun <T> IAwait<T>.flowOn(
    context: CoroutineContext
): IAwait<T> = newAwait {
    withContext(context) { await() }
}

fun <T> IAwait<T>.asFlow(): Flow<T> = flow {
    emit(await())
}

/**
 * 往集合尾部插入一条数据
 */
fun <T> IAwait<out MutableList<T>>.insert(
    element: T
): IAwait<MutableList<T>> = newAwait {
    await().apply { add(element) }
}

/**
 * 往集合指定位置插入一条数据
 */
fun <T> IAwait<out MutableList<T>>.insert(
    index: Int,
    element: T
): IAwait<MutableList<T>> = newAwait {
    await().apply { add(index, element) }
}

/**
 *  往集合尾部插入多条数据
 */
fun <T> IAwait<out MutableList<T>>.insertAll(
    elements: Collection<T>
): IAwait<MutableList<T>> = newAwait {
    await().apply { addAll(elements) }
}

/**
 * 往集合指定位置插入多条数据
 */
fun <T> IAwait<out MutableList<T>>.insertAll(
    index: Int,
    elements: Collection<T>
): IAwait<MutableList<T>> = newAwait {
    await().apply { addAll(index, elements) }
}


inline fun <T> IAwait<out Iterable<T>>.filter(
    crossinline predicate: (T) -> Boolean
): IAwait<List<T>> = newAwait {
    await().filter(predicate)
}


inline fun <T, C : MutableCollection<in T>> IAwait<out Iterable<T>>.filterTo(
    destination: C,
    crossinline predicate: (T) -> Boolean
): IAwait<C> = newAwait {
    await().filterTo(destination, predicate)
    destination
}

/**
 * 集合去重，根据对象的哈希值去重
 */
fun <T> IAwait<out Iterable<T>>.distinct(): IAwait<List<T>> = newAwait {
    await().distinct()
}

/**
 * 集合去重，根据表达式返回值的哈希值去重
 */
inline fun <T, K> IAwait<out Iterable<T>>.distinctBy(
    crossinline selector: (T) -> K
): IAwait<List<T>> = newAwait {
    await().distinctBy(selector)
}

fun <T, C : MutableList<T>> IAwait<out Iterable<T>>.distinctTo(
    destination: C
): IAwait<C> = newAwait {
    val set = HashSet<T>()
    for (e in destination) {
        set.add(e)
    }
    for (e in await()) {
        if (set.add(e))
            destination.add(e)
    }
    destination
}

inline fun <T, K, C : MutableList<T>> IAwait<out Iterable<T>>.distinctTo(
    destination: C,
    crossinline selector: (T) -> K
): IAwait<C> = newAwait {
    val set = HashSet<K>()
    for (e in destination) {
        val key = selector(e)
        set.add(key)
    }
    for (e in await()) {
        val key = selector(e)
        if (set.add(key))
            destination.add(e)
    }
    destination
}

/**
 * 顺序、排序对象需要继承Comparable接口，实现排序规则，返回原集合
 */
fun <T : Comparable<T>> IAwait<out MutableList<T>>.sort(): IAwait<MutableList<T>> = newAwait {
    await().apply { sort() }
}

/**
 * 倒序、排序对象需要继承Comparable接口，实现排序规则，返回原集合
 */
fun <T : Comparable<T>> IAwait<out MutableList<T>>.sortDescending()
    : IAwait<MutableList<T>> = sortWith(reverseOrder())

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
inline fun <T> IAwait<out MutableList<T>>.sortWith(
    crossinline comparator: (T, T) -> Int
): IAwait<MutableList<T>> = sortWith(Comparator { t1, t2 -> comparator(t1, t2) })

/**
 * 顺序、根据传入Comparator接口对象进行排序，返回原集合
 */
fun <T> IAwait<out MutableList<T>>.sortWith(
    comparator: Comparator<in T>
): IAwait<MutableList<T>> = newAwait {
    await().apply { sortWith(comparator) }
}

/**
 * 顺序、排序对象需要继承Comparable接口，实现排序规则，返回新的集合
 */
fun <T : Comparable<T>> IAwait<out Iterable<T>>.sorted()
    : IAwait<List<T>> = newAwait {
    await().sorted()
}

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
inline fun <T> IAwait<out Iterable<T>>.sortedWith(
    crossinline comparator: (T, T) -> Int
): IAwait<List<T>> = sortedWith(Comparator { t1, t2 -> comparator(t1, t2) })

/**
 * 顺序、根据传入Comparator接口对象进行排序，返回新的集合
 */
fun <T> IAwait<out Iterable<T>>.sortedWith(
    comparator: Comparator<in T>
): IAwait<List<T>> = newAwait {
    await().sortedWith(comparator)
}

/**
 * 为单个请求设置超时时长，该方法仅在使用协程时才有效
 * @param timeMillis 时长 单位: milliseconds
 * 注意: 要保证 timeMillis < OkHttp全局超时(连接+读+写)之和，否则无效
 */
fun <T> IAwait<T>.timeout(
    timeMillis: Long
): IAwait<T> = newAwait {
    withTimeout(timeMillis) { await() }
}

fun <T> IAwait<T>.onErrorReturnItem(t: T): IAwait<T> = onErrorReturn { t }

inline fun <T> IAwait<T>.onErrorReturn(
    crossinline map: suspend (Throwable) -> T
): IAwait<T> = newAwait {
    try {
        await()
    } catch (e: Throwable) {
        map(e)
    }
}

inline fun <T, R> IAwait<T>.map(
    crossinline map: suspend (T) -> R
): IAwait<R> = newAwait { map(await()) }

fun <T> IAwait<T>.delay(delay: Long): IAwait<T> = newAwait {
    val t = await()
    kotlinx.coroutines.delay(delay)
    t
}

fun <T> IAwait<T>.startDelay(delay: Long): IAwait<T> = newAwait {
    kotlinx.coroutines.delay(delay)
    await()
}

suspend fun <T> IAwait<T>.async(scope: CoroutineScope) = scope.async { await() }

suspend fun <T> Deferred<T>.tryAwait() = tryAwait { await() }

suspend fun <T> IAwait<T>.tryAwait() = tryAwait { await() }

private inline fun <T> tryAwait(block: () -> T): T? {
    return try {
        block()
    } catch (e: Throwable) {
        null
    }
}