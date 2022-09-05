package rxhttp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import rxhttp.wrapper.coroutines.Await
import kotlin.coroutines.CoroutineContext

/**
 * User: ljx
 * Date: 2021/9/18
 * Time: 17:56
 */
fun <T, R> Await<T>.newAwait(
    block: suspend Await<T>.() -> R
): Await<R> = object : Await<R> {

    override suspend fun await(): R {
        return this@newAwait.block()
    }
}

/**
 * @param times  retry times, default Long.MAX_VALUE Always try again
 * @param period retry period, default 0, time in milliseconds
 * @param test   retry conditions, default true，Unconditional retry
 */
fun <T> Await<T>.retry(
    times: Long = Long.MAX_VALUE,
    period: Long = 0,
    test: suspend (Throwable) -> Boolean = { true }
): Await<T> = object : Await<T> {

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

fun <T> Await<T>.onStart(
    action: suspend () -> Unit
): Await<T> = newAwait {
    action()
    await()
}

/**
 * @param times  repeat times, default Long.MAX_VALUE Always repeat
 * @param period repeat period, default 0, time in milliseconds
 * @param stop   repeat stop conditions, default false，Unconditional repeat
 */
fun <T> Await<T>.repeat(
    times: Long = Long.MAX_VALUE,
    period: Long = 0,
    stop: suspend (T) -> Boolean = { false }
): Await<T> = object : Await<T> {

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
fun <T> Await<T>.flowOn(
    context: CoroutineContext
): Await<T> = newAwait {
    withContext(context) { await() }
}

/**
 * Creates a flow that produces values from the given IAwait.
 */
fun <T> Await<T>.asFlow(): Flow<T> = flow {
    emit(await())
}

/**
 * Adds the specified element to the end of this list.
 */
fun <T> Await<out MutableList<T>>.insert(
    element: T
): Await<MutableList<T>> = newAwait {
    await().apply { add(element) }
}

/**
 * Inserts an element into the list at the specified [index].
 */
fun <T> Await<out MutableList<T>>.insert(
    index: Int,
    element: T
): Await<MutableList<T>> = newAwait {
    await().apply { add(index, element) }
}

/**
 * Adds all of the elements of the specified collection to the end of this list.
 *
 * The elements are appended in the order they appear in the [elements] collection.
 */
fun <T> Await<out MutableList<T>>.insertAll(
    elements: Collection<T>
): Await<MutableList<T>> = newAwait {
    await().apply { addAll(elements) }
}

/**
 * Inserts all of the elements of the specified collection [elements] into this list at the specified [index].
 */
fun <T> Await<out MutableList<T>>.insertAll(
    index: Int,
    elements: Collection<T>
): Await<MutableList<T>> = newAwait {
    await().apply { addAll(index, elements) }
}

fun <T> Await<out Iterable<T>>.filter(
    predicate: (T) -> Boolean
): Await<ArrayList<T>> = filterTo(ArrayList(), predicate)

/**
 * Appends all elements matching the given [predicate] to the given [destination].
 */
fun <T, C : MutableCollection<in T>> Await<out Iterable<T>>.filterTo(
    destination: C,
    predicate: (T) -> Boolean
): Await<C> = newAwait {
    await().filterTo(destination, predicate)
}

/**
 * Returns a IAwait containing a list containing only distinct elements from the given collection.
 *
 * The elements in the resulting list are in the same order as they were in the source collection.
 */
fun <T> Await<out Iterable<T>>.distinct(): Await<ArrayList<T>> = distinctTo(ArrayList()) { it }

/**
 * Returns a IAwait containing a list containing only elements from the given collection
 * having distinct keys returned by the given [selector] function.
 *
 * The elements in the resulting list are in the same order as they were in the source collection.
 */
fun <T, K> Await<out Iterable<T>>.distinctBy(
    selector: (T) -> K
): Await<ArrayList<T>> = distinctTo(ArrayList(), selector)

fun <T, C : MutableList<T>> Await<out Iterable<T>>.distinctTo(
    destination: C
): Await<C> = distinctTo(destination) { it }

/**
 * Appends all the different elements to the given [destination].
 *
 * The elements in the resulting list are in the same order as they were in the source collection.
 */
fun <T, K, C : MutableList<T>> Await<out Iterable<T>>.distinctTo(
    destination: C,
    selector: (T) -> K
): Await<C> = newAwait {
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
fun <T : Comparable<T>> Await<out MutableList<T>>.sort(): Await<MutableList<T>> = newAwait {
    await().apply { sort() }
}

/**
 * Sorts elements in the list in-place descending according to their natural sort order.
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 */
fun <T : Comparable<T>> Await<out MutableList<T>>.sortDescending()
    : Await<MutableList<T>> = sortWith(reverseOrder())

fun <T> Await<out MutableList<T>>.sortBy(
    vararg selectors: (T) -> Comparable<*>?
): Await<MutableList<T>> = sortWith(compareBy(*selectors))

inline fun <T> Await<out MutableList<T>>.sortBy(
    crossinline selector: (T) -> Comparable<*>?
): Await<MutableList<T>> = sortWith(compareBy(selector))

inline fun <T> Await<out MutableList<T>>.sortByDescending(
    crossinline selector: (T) -> Comparable<*>?
): Await<MutableList<T>> = sortWith(compareByDescending(selector))

inline fun <T> Await<out MutableList<T>>.sortWith(
    crossinline comparator: (T, T) -> Int
): Await<MutableList<T>> = sortWith(Comparator { t1, t2 -> comparator(t1, t2) })

/**
 * Sorts the elements in the list in-place, in natural sort order, according to the specified [Comparator].
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 */
fun <T> Await<out MutableList<T>>.sortWith(
    comparator: Comparator<in T>
): Await<MutableList<T>> = newAwait {
    await().apply { sortWith(comparator) }
}

/**
 * Returns a IAwait containing a new list of all elements sorted according to their natural sort order.
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 */
fun <T : Comparable<T>> Await<out Iterable<T>>.sorted(): Await<List<T>> = newAwait {
    await().sorted()
}

/**
 * Returns a IAwait containing a new list of all elements sorted descending according to their natural sort order.
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 */
fun <T : Comparable<T>> Await<out Iterable<T>>.sortedDescending()
    : Await<List<T>> = sortedWith(reverseOrder())

fun <T> Await<out Iterable<T>>.sortedBy(
    vararg selectors: (T) -> Comparable<*>?
): Await<List<T>> = sortedWith(compareBy(*selectors))

inline fun <T> Await<out Iterable<T>>.sortedBy(
    crossinline selector: (T) -> Comparable<*>?
): Await<List<T>> = sortedWith(compareBy(selector))

inline fun <T> Await<out Iterable<T>>.sortedByDescending(
    crossinline selector: (T) -> Comparable<*>?
): Await<List<T>> = sortedWith(compareByDescending(selector))

inline fun <T> Await<out Iterable<T>>.sortedWith(
    crossinline comparator: (T, T) -> Int
): Await<List<T>> = sortedWith(Comparator { t1, t2 -> comparator(t1, t2) })

/**
 * Returns a IAwait containing a new list of all elements sorted according to the specified [comparator].
 *
 * The sort is _stable_. It means that equal elements preserve their order relative to each other after sorting.
 */
fun <T> Await<out Iterable<T>>.sortedWith(
    comparator: Comparator<in T>
): Await<List<T>> = newAwait {
    await().sortedWith(comparator)
}


fun <T> Await<out List<T>>.subList(
    fromIndex: Int = 0, toIndex: Int
): Await<List<T>> = newAwait {
    await().subList(fromIndex, toIndex)
}

fun <T> Await<out Iterable<T>>.take(
    count: Int
): Await<List<T>> = newAwait {
    await().take(count)
}

fun <T> Await<out Iterable<T>>.toMutableList(): Await<MutableList<T>> = newAwait {
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
fun <T> Await<T>.timeout(
    timeMillis: Long
): Await<T> = newAwait {
    withTimeout(timeMillis) { await() }
}

/**
 * Returns a IAwait containing the specified object when an error occurs.
 */
fun <T> Await<T>.onErrorReturnItem(t: T): Await<T> = onErrorReturn { t }

/**
 * Returns a IAwait containing the object specified by the [map] function when an error occurs.
 */
inline fun <T> Await<T>.onErrorReturn(
    crossinline map: suspend (Throwable) -> T
): Await<T> = newAwait {
    try {
        await()
    } catch (e: Throwable) {
        map(e)
    }
}

/**
 * Returns a IAwait containing the results of applying the given [map] function
 */
inline fun <T, R> Await<T>.map(
    crossinline map: suspend (T) -> R
): Await<R> = newAwait {
    map(await())
}

inline fun <T> Await<T>.onEach(
    crossinline each: suspend (T) -> Unit
): Await<T> = newAwait {
    await().also { each(it) }
}

/**
 * Delay return by [timeMillis] millisecond after await.
 *
 * @param timeMillis time in milliseconds.
 */
fun <T> Await<T>.delay(timeMillis: Long): Await<T> = newAwait {
    await().also { kotlinx.coroutines.delay(timeMillis) }
}

/**
 * Delay return by [timeMillis] millisecond before await.
 *
 * @param timeMillis time in milliseconds.
 */
fun <T> Await<T>.startDelay(timeMillis: Long): Await<T> = newAwait {
    kotlinx.coroutines.delay(timeMillis)
    await()
}

/**
 * Creates a coroutine and returns its future result as an implementation of [Deferred].
 */
suspend fun <T> Await<T>.async(
    scope: CoroutineScope,
    context: CoroutineContext = SupervisorJob(scope.coroutineContext[Job]),
    start: CoroutineStart = CoroutineStart.DEFAULT
): Deferred<T> = scope.async(context, start) {
    await()
}

suspend inline fun <T> Await<T>.awaitResult(): Result<T> = runCatching { await() }

suspend inline fun <T> Await<T>.awaitResult(onSuccess: (T) -> Unit): Result<T> =
    awaitResult().onSuccess(onSuccess)

suspend inline fun <T> Deferred<T>.awaitResult(): Result<T> = runCatching { await() }

suspend inline fun <T> Deferred<T>.awaitResult(onSuccess: (T) -> Unit): Result<T> =
    awaitResult().onSuccess(onSuccess)

//return null when an error occurs.
suspend fun <T> Deferred<T>.tryAwait(onCatch: ((Throwable) -> Unit)? = null): T? =
    try {
        await()
    } catch (e: Throwable) {
        onCatch?.invoke(e)
        null
    }

//return null when an error occurs.
suspend fun <T> Await<T>.tryAwait(onCatch: ((Throwable) -> Unit)? = null): T? =
    try {
        await()
    } catch (e: Throwable) {
        onCatch?.invoke(e)
        null
    }

//return default value when an error occurs.
suspend inline fun <T> Deferred<T>.await(onCatch: (Throwable) -> T): T =
    try {
        await()
    } catch (e: Throwable) {
        onCatch(e)
    }

//return default value when an error occurs.
suspend inline fun <T> Await<T>.await(onCatch: (Throwable) -> T): T =
    try {
        await()
    } catch (e: Throwable) {
        onCatch(e)
    }