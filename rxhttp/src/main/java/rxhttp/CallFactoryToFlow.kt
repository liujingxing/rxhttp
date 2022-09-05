package rxhttp

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import okhttp3.Headers
import okhttp3.Response
import rxhttp.wrapper.BodyParamFactory
import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.callback.FileOutputStreamFactory
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.UriOutputStreamFactory
import rxhttp.wrapper.coroutines.Await
import rxhttp.wrapper.coroutines.setRangeHeader
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.entity.ProgressT

/**
 * CallFactory convert Flow
 *
 * User: ljx
 * Date: 2021/9/18
 * Time: 17:31
 */

fun CallFactory.toFlowBitmap(): Flow<Bitmap> = toFlow(toBitmap())

fun CallFactory.toFlowHeaders(): Flow<Headers> = toFlow(toHeaders())

fun CallFactory.toFlowOkResponse(): Flow<Response> = toFlow(toOkResponse())

inline fun <reified T> CallFactory.toFlow(): Flow<T> = toFlow(toClass())

fun <T> toFlow(await: Await<T>): Flow<T> = await.asFlow()

inline fun <reified T> BodyParamFactory.toFlow(
    await: Await<T> = toClass(),
    capacity: Int = 1,
    noinline progress: suspend (Progress) -> Unit
): Flow<T> = toFlowProgress(await, capacity).onEachProgress(progress)

fun <T> BodyParamFactory.toFlowProgress(
    await: Await<T>,
    capacity: Int = 1
): Flow<ProgressT<T>> =
    channelFlow {
        param.setProgressCallback { progress, currentSize, totalSize ->
            trySend(ProgressT<T>(progress, currentSize, totalSize))
        }
        trySend(ProgressT(await.await()))
    }.buffer(capacity, BufferOverflow.DROP_OLDEST)

/**
 * @param destPath Local storage path
 * @param append is append download
 * @param capacity capacity of the buffer between coroutines
 * @param progress Progress callback in suspend method, The callback thread depends on the coroutine thread
 */
fun CallFactory.toFlow(
    destPath: String,
    append: Boolean = false,
    capacity: Int = 1,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<String> = toFlow(FileOutputStreamFactory(destPath), append, capacity, progress)

fun CallFactory.toFlow(
    context: Context,
    uri: Uri,
    append: Boolean = false,
    capacity: Int = 1,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<Uri> = toFlow(UriOutputStreamFactory(context, uri), append, capacity, progress)

fun <T> CallFactory.toFlow(
    osFactory: OutputStreamFactory<T>,
    append: Boolean = false,
    capacity: Int = 1,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<T> =
    if (progress == null) {
        flow {
            setRangeHeader(osFactory, append)
            emit(toSyncDownload(osFactory).await())
        }.flowOn(Dispatchers.IO)
    } else {
        toFlowProgress(osFactory, append, capacity)
            .onEachProgress(progress)
    }

fun CallFactory.toFlowProgress(
    destPath: String,
    append: Boolean = false,
    capacity: Int = 1
): Flow<ProgressT<String>> = toFlowProgress(FileOutputStreamFactory(destPath), append, capacity)

fun CallFactory.toFlowProgress(
    context: Context,
    uri: Uri,
    append: Boolean = false,
    capacity: Int = 1
): Flow<ProgressT<Uri>> = toFlowProgress(UriOutputStreamFactory(context, uri), append, capacity)

fun <T> CallFactory.toFlowProgress(
    osFactory: OutputStreamFactory<T>,
    append: Boolean = false,
    capacity: Int = 1
): Flow<ProgressT<T>> =
    channelFlow {
        setRangeHeader(osFactory, append)
        val await = toSyncDownload(osFactory) { trySend(it) }
        trySend(ProgressT(await.await()))
    }
        .buffer(capacity, BufferOverflow.DROP_OLDEST)
        .flowOn(Dispatchers.IO)

fun <T> Flow<ProgressT<T>>.onEachProgress(progress: suspend (Progress) -> Unit): Flow<T> =
    onEach { if (it.result == null) progress(it) }
        .mapNotNull { it.result }