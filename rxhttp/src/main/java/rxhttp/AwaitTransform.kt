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
private class SafeAwait<T>(private val block: suspend () -> T) : Await<T> {
    override suspend fun await(): T = block()
}

fun <T> newAwait(block: suspend () -> T): Await<T> = SafeAwait(block)

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
 *         .map { ... }    // Will be executed in Default
 *         .flowOn(Dispatchers.Default)
 *         .flowOn(Dispatchers.IO)
 *         .map { ... }    // Will be executed in the Main
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
 * Creates a flow that produces values from the given Await.
 */
fun <T> Await<T>.asFlow(): Flow<T> = flow {
    emit(await())
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
 * Returns a Await containing the specified object when an error occurs.
 */
fun <T> Await<T>.onErrorReturnItem(t: T): Await<T> = onErrorReturn { t }

/**
 * Returns a Await containing the object specified by the [map] function when an error occurs.
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
 * Returns a Await containing the results of applying the given [map] function
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
suspend inline fun <T> Deferred<T>.safeAwait(onCatch: (Throwable) -> T): T =
    try {
        await()
    } catch (e: Throwable) {
        onCatch(e)
    }

//return default value when an error occurs.
suspend inline fun <T> Await<T>.safeAwait(onCatch: (Throwable) -> T): T =
    try {
        await()
    } catch (e: Throwable) {
        onCatch(e)
    }