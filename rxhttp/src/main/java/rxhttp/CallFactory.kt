package rxhttp

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Headers
import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.await.AwaitImpl
import rxhttp.wrapper.callback.FileOutputStreamFactory
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.UriFactory
import rxhttp.wrapper.callback.UriOutputStreamFactory
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.entity.ProgressT
import rxhttp.wrapper.param.AbstractBodyParam
import rxhttp.wrapper.parse.*
import rxhttp.wrapper.utils.LogUtil
import kotlin.coroutines.CoroutineContext

/**
 * User: ljx
 * Date: 2020/3/21
 * Time: 23:56
 */
interface CallFactory {

    fun newCall(): Call
}

interface BodyParamFactory : CallFactory {
    val param: AbstractBodyParam<*>
}

interface RangeHeader {
    fun setRangeHeader(startIndex: Long, endIndex: Long, connectLastProgress: Boolean): CallFactory
}

fun <T> CallFactory.toParser(
    parser: Parser<T>,
): IAwait<T> = AwaitImpl(this, parser)

suspend fun CallFactory.awaitString(): String = await()

suspend inline fun <reified T : Any> CallFactory.awaitList(): List<T> = await()

suspend inline fun <reified K : Any, reified V : Any> CallFactory.awaitMap(): Map<K, V> = await()

suspend fun CallFactory.awaitBitmap(): Bitmap = await(BitmapParser())

suspend fun CallFactory.awaitHeaders(): Headers = OkHttpCompat.headers(awaitOkResponse())

suspend fun CallFactory.awaitOkResponse(): Response = await(OkResponseParser())

suspend inline fun <reified T : Any> CallFactory.awaitResult(): Result<T> = runCatching { await() }

suspend inline fun <reified T : Any> CallFactory.awaitResult(onSuccess: (value: T) -> Unit): Result<T> =
    awaitResult<T>().onSuccess(onSuccess)

suspend inline fun <reified T : Any> CallFactory.await(): T = await(object : SimpleParser<T>() {})

suspend fun <T> CallFactory.await(parser: Parser<T>): T = toParser(parser).await()

fun CallFactory.toStr(): IAwait<String> = toClass()

inline fun <reified T : Any> CallFactory.toList(): IAwait<List<T>> = toClass()

inline fun <reified T : Any> CallFactory.toMutableList(): IAwait<MutableList<T>> = toClass()

inline fun <reified K : Any, reified V : Any> CallFactory.toMap(): IAwait<Map<K, V>> = toClass()

fun CallFactory.toBitmap(): IAwait<Bitmap> = toParser(BitmapParser())

fun CallFactory.toHeaders(): IAwait<Headers> = toOkResponse()
    .map {
        try {
            OkHttpCompat.headers(it)
        } finally {
            OkHttpCompat.closeQuietly(it)
        }
    }

fun CallFactory.toOkResponse(): IAwait<Response> = toParser(OkResponseParser())

inline fun <reified T : Any> CallFactory.toClass(): IAwait<T> = toParser(object : SimpleParser<T>() {})

private fun CallFactory.setRangeHeader(
    osFactory: OutputStreamFactory<*>,
    append: Boolean
) {
    var offsetSize = 0L
    if (append && this is RangeHeader && osFactory.offsetSize().also { offsetSize = it } >= 0) {
        setRangeHeader(offsetSize, -1, true)
    }
}

fun <T> CallFactory.toSyncDownload(
    osFactory: OutputStreamFactory<T>,
    context: CoroutineContext? = null,
    progressCallback: (suspend (ProgressT<T>) -> Unit)? = null
): IAwait<T> {
    val parser = if (progressCallback != null) {
        SuspendStreamParser(osFactory) { progress, currentSize, totalSize ->
            LogUtil.logDownProgress(progress, currentSize, totalSize)
            val p = ProgressT<T>(progress, currentSize, totalSize)
            context?.let { withContext(it) { progressCallback(p) } } ?: progressCallback(p)
        }
    } else {
        SuspendStreamParser(osFactory)
    }
    return toParser(parser)
}

@Deprecated(
    level = DeprecationLevel.WARNING,
    message = "Use 'toDownload' instead",
    replaceWith = ReplaceWith("toDownload(destPath, context, true) {}")
)
fun CallFactory.toAppendDownload(
    destPath: String,
    context: CoroutineContext? = null,
    progress: (suspend (Progress) -> Unit)? = null
) = toDownload(destPath, context, true, progress)

@Deprecated(
    level = DeprecationLevel.WARNING,
    message = "Use 'toDownload' instead",
    replaceWith = ReplaceWith("toDownload(context, uri, coroutineContext, true)")
)
fun CallFactory.toAppendDownload(
    context: Context,
    uri: Uri,
    coroutineContext: CoroutineContext? = null,
    progress: (suspend (Progress) -> Unit)? = null
) = toDownload(context, uri, coroutineContext, true, progress)

@Deprecated(
    level = DeprecationLevel.WARNING,
    message = "Use 'toDownload' instead",
    replaceWith = ReplaceWith("toDownload(uriFactory, coroutineContext, true)")
)
fun CallFactory.toAppendDownload(
    uriFactory: UriFactory,
    coroutineContext: CoroutineContext? = null,
    progress: (suspend (Progress) -> Unit)? = null
) = toDownload(uriFactory, coroutineContext, true, progress)

/**
 * @param destPath Local storage path
 * @param context Use to control the thread on which the progress callback
 * @param append is append download
 * @param progress Progress callback in suspend method, The callback thread depends on the coroutine thread
 */
fun CallFactory.toDownload(
    destPath: String,
    context: CoroutineContext? = null,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): IAwait<String> = toDownload(FileOutputStreamFactory(destPath), context, append, progress)

fun CallFactory.toDownload(
    context: Context,
    uri: Uri,
    coroutineContext: CoroutineContext? = null,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): IAwait<Uri> =
    toDownload(UriOutputStreamFactory(context, uri), coroutineContext, append, progress)

fun <T> CallFactory.toDownload(
    osFactory: OutputStreamFactory<T>,
    context: CoroutineContext? = null,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): IAwait<T> =
    toSyncDownload(osFactory, context, progress)
        .onStart { setRangeHeader(osFactory, append) }
        .flowOn(Dispatchers.IO)

inline fun <reified T : Any> CallFactory.toFlow(iAwait: IAwait<T> = toClass()): Flow<T> =
    flow { emit(iAwait.await()) }

@ExperimentalCoroutinesApi
inline fun <reified T : Any> BodyParamFactory.toFlow(
    noinline progress: suspend (Progress) -> Unit
) = toFlowProgress<T>().onEachProgress(progress)

@ExperimentalCoroutinesApi
inline fun <reified T : Any> BodyParamFactory.toFlowProgress(
    iAwait: IAwait<T> = toClass()
) =
    channelFlow {
        param.setProgressCallback { progress, currentSize, totalSize ->
            trySend(ProgressT<T>(progress, currentSize, totalSize))
        }
        iAwait.await().also { trySend(ProgressT<T>(it)) }
    }.buffer(1, BufferOverflow.DROP_OLDEST)

/**
 * @param destPath Local storage path
 * @param append is append download
 * @param progress Progress callback in suspend method, The callback thread depends on the coroutine thread
 */
fun CallFactory.toFlow(
    destPath: String,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<String> = toFlow(FileOutputStreamFactory(destPath), append, progress)

fun CallFactory.toFlow(
    context: Context,
    uri: Uri,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<Uri> = toFlow(UriOutputStreamFactory(context, uri), append, progress)

fun <T> CallFactory.toFlow(
    osFactory: OutputStreamFactory<T>,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<T> =
    if (progress == null) {
        flow {
            setRangeHeader(osFactory, append)
            emit(toSyncDownload(osFactory).await())
        }.flowOn(Dispatchers.IO)
    } else {
        toFlowProgress(osFactory, append)
            .onEachProgress(progress)
    }

fun CallFactory.toFlowProgress(
    destPath: String,
    append: Boolean = false
): Flow<ProgressT<String>> = toFlowProgress(FileOutputStreamFactory(destPath), append)

fun CallFactory.toFlowProgress(
    context: Context,
    uri: Uri,
    append: Boolean = false
): Flow<ProgressT<Uri>> = toFlowProgress(UriOutputStreamFactory(context, uri), append)

fun <T> CallFactory.toFlowProgress(
    osFactory: OutputStreamFactory<T>,
    append: Boolean = false
): Flow<ProgressT<T>> =
    flow {
        setRangeHeader(osFactory, append)
        toSyncDownload(osFactory) { emit(it) }
            .await().let { emit(ProgressT(it)) }
    }
        .buffer(1, BufferOverflow.DROP_OLDEST)
        .flowOn(Dispatchers.IO)

fun <T> Flow<ProgressT<T>>.onEachProgress(progress: suspend (Progress) -> Unit): Flow<T> =
    onEach { if (it.result == null) progress(it) }
        .mapNotNull { it.result }
