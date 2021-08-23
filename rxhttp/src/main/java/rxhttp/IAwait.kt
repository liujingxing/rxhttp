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
 * @param times  retry times, default Long.MAX_VALUE Always try again
 * @param period retry period, default 0, time in milliseconds
 * @param test   retry conditions, default true，Unconditional retry
 */
fun <T> IAwait<T>.retry(
    times: Long = Long.MAX_VALUE,
    period: Long = 0,
    test: suspend (Throwable) -> Boolean = { true }
): IAwait<T> = object : IAwait<T> {

    var retryTime = times

    override suspend fun await(): T {
        return try {
            this@retry.await()
        } catch (e: Throwable) {
            val remaining = retryTime  //Remaining retries
            if (remaining != Long.MAX_VALUE) {
                retryTime = remaining - 1
            }
            val pass = test(e)
            if (remaining > 0 && pass) {
                kotlinx.coroutines.delay(period)
                await()
            } else throw e
        }
    }
}

/**
 * @param times  repeat times, default Long.MAX_VALUE Always repeat
 * @param period repeat period, default 0, time in milliseconds
 * @param stop   repeat stop conditions, default false，Unconditional repeat
 */
fun <T> IAwait<T>.repeat(
    times: Long = Long.MAX_VALUE,
    period: Long = 0,
    stop: suspend (T) -> Boolean = { false }
): IAwait<T> = object : IAwait<T> {

    var remaining = if (times == Long.MAX_VALUE) Long.MAX_VALUE else times - 1

    override suspend fun await(): T {
        while (remaining > 0) {
            if (remaining != Long.MAX_VALUE) {
                remaining--
            }
            val t = this@repeat.await()
            if (stop(t)) {
                return t
            }
            kotlinx.coroutines.delay(period)
        }
        return this@repeat.await()
    }
}

/**
 * Changes the context where this flow is executed to the given [context].
 * This operator is composable and affects only preceding operators that do not have its own context.
 * This operator is context preserving: [context] **does not** leak into the downstream flow.
 *
 * For example:
 *
 * ```
 * lifecycleScope.launch {
 *     val t = RxHttp.get("...")
 *         .toClass<T>()
 *         .map { ... }    // Will be executed in IO
 *         .flowOn(Dispatchers.IO)
 *         .filter { ... } // Will be executed in Default
 *         .flowOn(Dispatchers.Default)
 *         .flowOn(Dispatchers.IO)
 *         .filter { ... } // Will be executed in the Main
 *         .await()        // Will be executed in the Main
 * }
 * ```
 */
fun <T> IAwait<T>.flowOn(
    context: CoroutineContext
): IAwait<T> = newAwait {
    withContext(context) { await() }
}

/**
 * Creates a flow that produces values from the given IAwait.
 */
fun <T> IAwait<T>.asFlow(): Flow<T> = flow {
    emit(await())
}

/**
 * Adds the specified element to the end of this list.
 */
fun <T> IAwait<out MutableList<T>>.insert(
    element: T
): IAwait<MutableList<T>> = newAwait {
    await().apply { add(element) }
}

/**
 * Inserts an element into the list at the specified [index].
 */
fun <T> IAwait<out MutableList<T>>.insert(
    index: Int,
    element: T
): IAwait<MutableList<T>> = newAwait {
    await().apply { add(index, element) }
}

/**
 * Adds all of the elements of the specified collection to the end of this list.
 *
 * The elements are appended in the order they appear in the [elements] collection.
 */
fun <T> IAwait<out MutableList<T>>.insertAll(
    elements: Collection<T>
): IAwait<MutableList<T>> = newAwait {
    await().apply { addAll(elements) }
}

/**
 * Inserts all of the elements of the specified collection [elements] into this list at the specified [index].
 */
fun <T> IAwait<out MutableList<T>>.insertAll(
    index: Int,
    elements: Collection<T>
): IAwait<MutableList<T>> = newAwait {
    await().apply { addAll(index, elements) }
}

inline fun <T> IAwait<out Iterable<T>>.filter(
    crossinline predicate: (T) -> Boolean
): IAwait<ArrayList<T>> = filterTo(ArrayList(), predicate)

/**
 * Appends all elements matching the given [predicate] to the given [destination].
 */
inline fun <T, C : MutableCollection<in T>> IAwait<out Iterable<T>>.filterTo(
    destination: C,
    crossinline predicate: (T) -> Boolean
): IAwait<C> = newAwait {
    await().filterTo(destination, predicate)
}

/**
 * Returns a IAwait containing a list containing only distinct elements from the given collection.
 *
 * The elements in the resulting list are in the same order as they were in the source collection.
 */
fun <T> IAwait<out Iterable<T>>.distinct(): IAwait<ArrayList<T>> = distinctTo(ArrayList()) { it }

/**
 * Returns a IAwait containing a list containing only elements from the given collection
 * having distinct keys returned by the given [selector] function.
 *
 * The elements in the resulting list are in the same order as they were in the source collection.
 */
inline fun <T, K> IAwait<out Iterable<T>>.distinctBy(
    crossinline selector: (T) -> K
): IAwait<ArrayList<T>> = distinctTo(ArrayList(), selector)

fun <T, C : MutableList<T>> IAwait<out Iterable<T>>.distinctTo(
    destination: C
): IAwait<C> = distinctTo(destination) { it }

/**
 * Appends all the different elements to the given [destination].
 *
 * The elements in the resulting list are in the same order as they were in the source collection.
 */
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
 * Sorts elements in the list in-place according to their natural sort order.
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 */
fun <T : Comparable<T>> IAwait<out MutableList<T>>.sort(): IAwait<MutableList<T>> = newAwait {
    await().apply { sort() }
}

/**
 * Sorts elements in the list in-place descending according to their natural sort order.
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 */
fun <T : Comparable<T>> IAwait<out MutableList<T>>.sortDescending()
    : IAwait<MutableList<T>> = sortWith(reverseOrder())

fun <T> IAwait<out MutableList<T>>.sortBy(
    vararg selectors: (T) -> Comparable<*>?
): IAwait<MutableList<T>> = sortWith(compareBy(*selectors))

inline fun <T> IAwait<out MutableList<T>>.sortBy(
    crossinline selector: (T) -> Comparable<*>?
): IAwait<MutableList<T>> = sortWith(compareBy(selector))

inline fun <T> IAwait<out MutableList<T>>.sortByDescending(
    crossinline selector: (T) -> Comparable<*>?
): IAwait<MutableList<T>> = sortWith(compareByDescending(selector))

inline fun <T> IAwait<out MutableList<T>>.sortWith(
    crossinline comparator: (T, T) -> Int
): IAwait<MutableList<T>> = sortWith(Comparator { t1, t2 -> comparator(t1, t2) })

/**
 * Sorts the elements in the list in-place, in natural sort order, according to the specified [Comparator].
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 */
fun <T> IAwait<out MutableList<T>>.sortWith(
    comparator: Comparator<in T>
): IAwait<MutableList<T>> = newAwait {
    await().apply { sortWith(comparator) }
}

/**
 * Returns a IAwait containing a new list of all elements sorted according to their natural sort order.
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 */
fun <T : Comparable<T>> IAwait<out Iterable<T>>.sorted(): IAwait<List<T>> = newAwait {
    await().sorted()
}

/**
 * Returns a IAwait containing a new list of all elements sorted descending according to their natural sort order.
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 */
fun <T : Comparable<T>> IAwait<out Iterable<T>>.sortedDescending()
    : IAwait<List<T>> = sortedWith(reverseOrder())

fun <T> IAwait<out Iterable<T>>.sortedBy(
    vararg selectors: (T) -> Comparable<*>?
): IAwait<List<T>> = sortedWith(compareBy(*selectors))

inline fun <T> IAwait<out Iterable<T>>.sortedBy(
    crossinline selector: (T) -> Comparable<*>?
): IAwait<List<T>> = sortedWith(compareBy(selector))

inline fun <T> IAwait<out Iterable<T>>.sortedByDescending(
    crossinline selector: (T) -> Comparable<*>?
): IAwait<List<T>> = sortedWith(compareByDescending(selector))

inline fun <T> IAwait<out Iterable<T>>.sortedWith(
    crossinline comparator: (T, T) -> Int
): IAwait<List<T>> = sortedWith(Comparator { t1, t2 -> comparator(t1, t2) })

/**
 * Returns a IAwait containing a new list of all elements sorted according to the specified [comparator].
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 */
fun <T> IAwait<out Iterable<T>>.sortedWith(
    comparator: Comparator<in T>
): IAwait<List<T>> = newAwait {
    await().sortedWith(comparator)
}


fun <T> IAwait<out List<T>>.subList(
    fromIndex: Int = 0, toIndex: Int
): IAwait<List<T>> = newAwait {
    await().subList(fromIndex, toIndex)
}

fun <T> IAwait<out Iterable<T>>.take(
    count: Int
): IAwait<List<T>> = newAwait {
    await().take(count)
}

fun <T> IAwait<out Iterable<T>>.toMutableList(): IAwait<MutableList<T>> = newAwait {
    await().let {
        if (it is MutableList<T>) it
        else it.toMutableList()
    }
}

/**
 * Set the timeout for the request
 * @param timeMillis timeout time in milliseconds.
 *
 * timeMillis should be less than the sum of (connection + read + write) durations, otherwise invalid
 */
fun <T> IAwait<T>.timeout(
    timeMillis: Long
): IAwait<T> = newAwait {
    withTimeout(timeMillis) { await() }
}

/**
 * Returns a IAwait containing the specified object when an error occurs.
 */
fun <T> IAwait<T>.onErrorReturnItem(t: T): IAwait<T> = onErrorReturn { t }

/**
 * Returns a IAwait containing the object specified by the [map] function when an error occurs.
 */
inline fun <T> IAwait<T>.onErrorReturn(
    crossinline map: suspend (Throwable) -> T
): IAwait<T> = newAwait {
    try {
        await()
    } catch (e: Throwable) {
        map(e)
    }
}

/**
 * Returns a IAwait containing the results of applying the given [map] function
 */
inline fun <T, R> IAwait<T>.map(
    crossinline map: suspend (T) -> R
): IAwait<R> = newAwait {
    map(await())
}

/**
 * Delay return by [timeMillis] millisecond after await.
 *
 * @param timeMillis time in milliseconds.
 */
fun <T> IAwait<T>.delay(timeMillis: Long): IAwait<T> = newAwait {
    val t = await()
    kotlinx.coroutines.delay(timeMillis)
    t
}

/**
 * Delay return by [timeMillis] millisecond before await.
 *
 * @param timeMillis time in milliseconds.
 */
fun <T> IAwait<T>.startDelay(timeMillis: Long): IAwait<T> = newAwait {
    kotlinx.coroutines.delay(timeMillis)
    await()
}

/**
 * Creates a coroutine and returns its future result as an implementation of [Deferred].
 */
suspend fun <T> IAwait<T>.async(
    scope: CoroutineScope,
    context: CoroutineContext = SupervisorJob(scope.coroutineContext[Job]),
    start: CoroutineStart = CoroutineStart.DEFAULT
): Deferred<T> = scope.async(context, start) {
    await()
}

suspend fun <T> IAwait<T>.awaitResult(): Result<T> = runCatching { await() }

suspend fun <T> IAwait<T>.awaitResult(onSuccess: (value: T) -> Unit): Result<T> =
    awaitResult().onSuccess(onSuccess)

suspend fun <T> Deferred<T>.awaitResult(): Result<T> = runCatching { await() }

suspend fun <T> Deferred<T>.awaitResult(onSuccess: (value: T) -> Unit): Result<T> =
    awaitResult().onSuccess(onSuccess)

/**
 * Try to get the return value and return null when an error occurs.
 */
suspend fun <T> Deferred<T>.tryAwait(
    onError: ((exception: Throwable) -> Unit)? = null
): T? = tryAwait(onError) { await() }

/**
 * Try to get the return value and return null when an error occurs.
 */
suspend fun <T> IAwait<T>.tryAwait(
    onError: ((exception: Throwable) -> Unit)? = null
): T? = tryAwait(onError) { await() }

private inline fun <T> tryAwait(
    noinline onError: ((exception: Throwable) -> Unit)? = null,
    block: () -> T,
): T? {
    return try {
        block()
    } catch (e: Throwable) {
        onError?.invoke(e)
        null
    }
}