package rxhttp

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import rxhttp.wrapper.BodyParamFactory
import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.ITag
import rxhttp.wrapper.callback.FileOutputStreamFactory
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.ProgressCallback
import rxhttp.wrapper.callback.UriOutputStreamFactory
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.entity.ProgressT
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.parse.StreamParser

/**
 * CallFactory convert Flow
 *
 * User: ljx
 * Date: 2021/9/18
 * Time: 17:31
 */

fun <T> CallFactory.toFlow(parser: Parser<T>): Flow<T> = toFlow(toAwait(parser))

inline fun <reified T> CallFactory.toFlow(): Flow<T> = toFlow(toAwait())

fun CallFactory.toFlowString(): Flow<String> = toFlow()

inline fun <reified T> CallFactory.toFlowList(): Flow<List<T>> = toFlow()

inline fun <reified V> CallFactory.toFlowMapString(): Flow<Map<String, V>> = toFlow()

fun <T> toFlow(await: Await<T>): Flow<T> = await.asFlow()

inline fun <reified T> BodyParamFactory.toFlow(
    await: Await<T> = toAwait(),
    capacity: Int = 2,
    noinline progress: suspend (Progress) -> Unit
): Flow<T> = toFlowProgress(await, capacity).onEachProgress(progress)

fun <T> BodyParamFactory.toFlowProgress(
    await: Await<T>,
    capacity: Int = 2
): Flow<ProgressT<T>> =
    channelFlow {
        param.setProgressCallback { progress, currentSize, totalSize ->
            trySend(ProgressT<T>(progress, currentSize, totalSize))
        }
        val t: T = await.await()
        trySend(ProgressT(t))
    }.buffer(capacity, BufferOverflow.DROP_OLDEST)

/**
 * @param destPath Local storage path
 * @param append is append download
 * @param capacity capacity of the buffer between coroutines
 * @param progress Progress callback in suspend method, The callback thread depends on the coroutine thread
 */
fun CallFactory.toDownloadFlow(
    destPath: String,
    append: Boolean = false,
    capacity: Int = 2,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<String> = toDownloadFlow(FileOutputStreamFactory(destPath), append, capacity, progress)

fun CallFactory.toDownloadFlow(
    context: Context,
    uri: Uri,
    append: Boolean = false,
    capacity: Int = 2,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<Uri> = toDownloadFlow(UriOutputStreamFactory(context, uri), append, capacity, progress)

fun <T> CallFactory.toDownloadFlow(
    osFactory: OutputStreamFactory<T>,
    append: Boolean = false,
    capacity: Int = 2,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<T> =
    if (progress == null) {
        toFlow(streamParser(osFactory, append))
    } else {
        toFlowProgress(osFactory, append, capacity)
            .onEachProgress(progress)
    }

fun CallFactory.toFlowProgress(
    destPath: String,
    append: Boolean = false,
    capacity: Int = 2
): Flow<ProgressT<String>> = toFlowProgress(FileOutputStreamFactory(destPath), append, capacity)

fun CallFactory.toFlowProgress(
    context: Context,
    uri: Uri,
    append: Boolean = false,
    capacity: Int = 2
): Flow<ProgressT<Uri>> = toFlowProgress(UriOutputStreamFactory(context, uri), append, capacity)

fun <T> CallFactory.toFlowProgress(
    osFactory: OutputStreamFactory<T>,
    append: Boolean = false,
    capacity: Int = 2
): Flow<ProgressT<T>> =
    channelFlow {
        val parser = streamParser(osFactory, append)
        parser.progressCallback = ProgressCallback { progress, currentSize, totalSize ->
            trySend(ProgressT<T>(progress, currentSize, totalSize))
        }
        val t: T = toAwait(parser).await()
        trySend(ProgressT(t))
    }.buffer(capacity, BufferOverflow.DROP_OLDEST)

fun <T> Flow<ProgressT<T>>.onEachProgress(progress: suspend (Progress) -> Unit): Flow<T> =
    onEach { if (it.result == null) progress(it) }
        .mapNotNull { it.result }

private fun <T> CallFactory.streamParser(
    osFactory: OutputStreamFactory<T>,
    append: Boolean = false
): StreamParser<T> {
    if (append && this is ITag) {
        tag(OutputStreamFactory::class.java, osFactory)
    }
    return StreamParser(osFactory)
}