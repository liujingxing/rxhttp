package rxhttp

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.Dispatchers
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
import rxhttp.wrapper.parse.*
import kotlin.coroutines.CoroutineContext

/**
 * User: ljx
 * Date: 2020/3/21
 * Time: 23:56
 */
interface IRxHttp {

    fun newCall(): Call

    fun setRangeHeader(startIndex: Long, endIndex: Long, connectLastProgress: Boolean): IRxHttp
}

suspend fun IRxHttp.awaitString(): String = await()

suspend inline fun <reified T : Any> IRxHttp.awaitList(): List<T> = await()

suspend inline fun <reified K : Any, reified V : Any> IRxHttp.awaitMap(): Map<K, V> = await()

suspend fun IRxHttp.awaitBitmap(): Bitmap = await(BitmapParser())

suspend fun IRxHttp.awaitHeaders(): Headers = OkHttpCompat.headers(awaitOkResponse())

suspend fun IRxHttp.awaitOkResponse(): Response = await(OkResponseParser())

suspend inline fun <reified T : Any> IRxHttp.awaitResult(): Result<T> = runCatching { await() }

suspend inline fun <reified T : Any> IRxHttp.awaitResult(onSuccess: (value: T) -> Unit): Result<T> =
    awaitResult<T>().onSuccess(onSuccess)

suspend inline fun <reified T : Any> IRxHttp.await(): T = await(object : SimpleParser<T>() {})

suspend fun <T> IRxHttp.await(parser: Parser<T>): T = toParser(parser).await()

fun IRxHttp.toStr(): IAwait<String> = toClass()

inline fun <reified T : Any> IRxHttp.toList(): IAwait<List<T>> = toClass()

inline fun <reified T : Any> IRxHttp.toMutableList(): IAwait<MutableList<T>> = toClass()

inline fun <reified K : Any, reified V : Any> IRxHttp.toMap(): IAwait<Map<K, V>> = toClass()

fun IRxHttp.toBitmap(): IAwait<Bitmap> = toParser(BitmapParser())

fun IRxHttp.toHeaders(): IAwait<Headers> = toOkResponse()
    .map {
        try {
            OkHttpCompat.headers(it)
        } finally {
            OkHttpCompat.closeQuietly(it)
        }
    }

fun IRxHttp.toOkResponse(): IAwait<Response> = toParser(OkResponseParser())

inline fun <reified T : Any> IRxHttp.toClass(): IAwait<T> = toParser(object : SimpleParser<T>() {})

fun <T> IRxHttp.toSyncDownload(
    osFactory: OutputStreamFactory<T>,
    context: CoroutineContext? = null,
    progress: (suspend (ProgressT<T>) -> Unit)? = null
): IAwait<T> = toParser(SuspendStreamParser(osFactory, context, progress))

@Deprecated(
    level = DeprecationLevel.WARNING,
    message = "Use 'toDownload' instead",
    replaceWith = ReplaceWith("toDownload(destPath, context, true) {}")
)
suspend fun IRxHttp.toAppendDownload(
    destPath: String,
    context: CoroutineContext? = null,
    progress: (suspend (Progress) -> Unit)? = null
) = toDownload(destPath, context, true, progress)

@Deprecated(
    level = DeprecationLevel.WARNING,
    message = "Use 'toDownload' instead",
    replaceWith = ReplaceWith("toDownload(context, uri, coroutineContext, true)")
)
suspend fun IRxHttp.toAppendDownload(
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
suspend fun IRxHttp.toAppendDownload(
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
suspend fun IRxHttp.toDownload(
    destPath: String,
    context: CoroutineContext? = null,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): IAwait<String> = toDownload(FileOutputStreamFactory(destPath), context, append, progress)

suspend fun IRxHttp.toDownload(
    context: Context,
    uri: Uri,
    coroutineContext: CoroutineContext? = null,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): IAwait<Uri> =
    toDownload(UriOutputStreamFactory(context, uri), coroutineContext, append, progress)

suspend fun <T> IRxHttp.toDownload(
    osFactory: OutputStreamFactory<T>,
    context: CoroutineContext? = null,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): IAwait<T> {
    if (append) {
        val offsetSize = withContext(Dispatchers.IO) { osFactory.offsetSize() }
        if (offsetSize >= 0)
            setRangeHeader(offsetSize, -1, true)
    }
    return toSyncDownload(osFactory, context, progress)
        .flowOn(Dispatchers.IO)
}

/**
 * @param destPath Local storage path
 * @param append is append download
 * @param progress Progress callback in suspend method, The callback thread depends on the coroutine thread
 */
suspend fun IRxHttp.toDownloadFlow(
    destPath: String,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<String> = toDownloadFlow(FileOutputStreamFactory(destPath), append, progress)

suspend fun IRxHttp.toDownloadFlow(
    context: Context,
    uri: Uri,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<Uri> = toDownloadFlow(UriOutputStreamFactory(context, uri), append, progress)

suspend fun <T> IRxHttp.toDownloadFlow(
    osFactory: OutputStreamFactory<T>,
    append: Boolean = false,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<T> =
    if (progress == null) {
        flow {
            var offsetSize = 0L
            if (append && osFactory.offsetSize().also { offsetSize = it } >= 0) {
                setRangeHeader(offsetSize, -1, true)
            }
            emit(toSyncDownload(osFactory).await())
        }.flowOn(Dispatchers.IO)
    } else {
        toDownloadFlowProgress(osFactory, append)
            .onEachProgress(progress)
    }

suspend fun IRxHttp.toDownloadFlowProgress(
    destPath: String,
    append: Boolean = false
): Flow<ProgressT<String>> = toDownloadFlowProgress(FileOutputStreamFactory(destPath), append)

suspend fun IRxHttp.toDownloadFlowProgress(
    context: Context,
    uri: Uri,
    append: Boolean = false
): Flow<ProgressT<Uri>> = toDownloadFlowProgress(UriOutputStreamFactory(context, uri), append)

suspend fun <T> IRxHttp.toDownloadFlowProgress(
    osFactory: OutputStreamFactory<T>,
    append: Boolean = false
): Flow<ProgressT<T>> =
    flow {
        var offsetSize = 0L
        if (append && osFactory.offsetSize().also { offsetSize = it } >= 0) {
            setRangeHeader(offsetSize, -1, true)
        }
        toSyncDownload(osFactory) { emit(it) }
            .await().let { emit(ProgressT(it)) }
    }
        .buffer(1, BufferOverflow.DROP_OLDEST)
        .flowOn(Dispatchers.IO)

fun <T> Flow<ProgressT<T>>.onEachProgress(
    progress: suspend (Progress) -> Unit
) = onEach { if (it.result == null) progress(it) }
    .mapNotNull { it.result }

inline fun <reified T : Any> IRxHttp.toFlow(iAwait: IAwait<T> = toClass()) =
    flow { emit(iAwait.await()) }

//All of the above methods will eventually call this method.
fun <T> IRxHttp.toParser(
    parser: Parser<T>,
): IAwait<T> = AwaitImpl(this, parser)

