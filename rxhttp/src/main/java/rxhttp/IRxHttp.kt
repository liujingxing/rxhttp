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
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.callback.UriFactory
import rxhttp.wrapper.callback.newOutputStreamFactory
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.entity.ProgressT
import rxhttp.wrapper.parse.*
import rxhttp.wrapper.utils.length
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

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

suspend fun IRxHttp.awaitDownload(
    destPath: String
): String = toDownload(destPath).await()

suspend fun IRxHttp.awaitDownload(
    destPath: String,
    context: CoroutineContext? = null,
    progress: suspend (Progress) -> Unit
): String = toDownload(destPath, context, progress).await()

suspend fun IRxHttp.awaitDownload(
    context: Context,
    uri: Uri,
    coroutineContext: CoroutineContext? = null,
    progress: suspend (Progress) -> Unit
): Uri = toDownload(context, uri, coroutineContext, progress).await()

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

/**
 * @param destPath Local storage path
 * @param context Use to control the thread on which the progress callback
 * @param progress Progress callback in suspend method, The callback thread depends on the coroutine thread
 */
fun IRxHttp.toDownload(
    destPath: String,
    context: CoroutineContext? = null,
    progress: (suspend (ProgressT<String>) -> Unit)? = null
): IAwait<String> = toSyncDownload(newOutputStreamFactory(destPath), context, progress)
    .flowOn(Dispatchers.IO)

fun IRxHttp.toDownload(
    context: Context,
    uri: Uri,
    coroutineContext: CoroutineContext? = null,
    progress: (suspend (ProgressT<Uri>) -> Unit)? = null
): IAwait<Uri> = toSyncDownload(newOutputStreamFactory(context, uri), coroutineContext, progress)
    .flowOn(Dispatchers.IO)

fun <T> IRxHttp.toDownload(
    osFactory: OutputStreamFactory<T>,
    context: CoroutineContext? = null,
    progress: (suspend (ProgressT<T>) -> Unit)? = null
): IAwait<T> = toSyncDownload(osFactory, context, progress)
    .flowOn(Dispatchers.IO)

fun IRxHttp.toAppendDownload(
    destPath: String,
    context: CoroutineContext? = null,
    progress: (suspend (ProgressT<String>) -> Unit)? = null
): IAwait<String> {
    val fileLength = File(destPath).length()
    setRangeHeader(fileLength, -1, true)
    return toDownload(destPath, context, progress)
}

suspend fun IRxHttp.toAppendDownload(
    context: Context,
    uri: Uri,
    coroutineContext: CoroutineContext? = null,
    progress: (suspend (ProgressT<Uri>) -> Unit)? = null
): IAwait<Uri> {
    val length = withContext(Dispatchers.IO) { uri.length(context) }
    if (length >= 0) setRangeHeader(length, -1, true)
    return toDownload(context, uri, coroutineContext, progress)
}

suspend fun IRxHttp.toAppendDownload(
    uriFactory: UriFactory,
    coroutineContext: CoroutineContext? = null,
    progress: (suspend (ProgressT<Uri>) -> Unit)? = null
): IAwait<Uri> {
    val factory: OutputStreamFactory<Uri> = withContext(Dispatchers.IO) {
        uriFactory.query()?.let {
            val length = it.length(uriFactory.context)
            if (length >= 0)
                setRangeHeader(length, -1, true)
            newOutputStreamFactory(uriFactory.context, it)
        } ?: uriFactory
    }
    return toDownload(factory, coroutineContext, progress)
}

fun IRxHttp.toFlowProgress(destPath: String) =
    toFlowProgress(newOutputStreamFactory(destPath))

fun IRxHttp.toFlowProgress(context: Context, uri: Uri) =
    toFlowProgress(newOutputStreamFactory(context, uri))

fun <T> IRxHttp.toFlowProgress(osFactory: OutputStreamFactory<T>) =
    flow {
        toSyncDownload(osFactory) { emit(it) }
            .await().let { emit(ProgressT(it)) }
    }.buffer(1, BufferOverflow.DROP_OLDEST)
        .flowOn(Dispatchers.IO)

fun IRxHttp.toDownloadFlow(
    destPath: String,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<String> = toDownloadFlow(newOutputStreamFactory(destPath), progress)

fun IRxHttp.toDownloadFlow(
    context: Context,
    uri: Uri,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<Uri> = toDownloadFlow(newOutputStreamFactory(context, uri), progress)

fun <T> IRxHttp.toDownloadFlow(
    osFactory: OutputStreamFactory<T>,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<T> =
    if (progress == null) {
        flow { emit(toSyncDownload(osFactory).await()) }
            .flowOn(Dispatchers.IO)
    } else {
        toFlowProgress(osFactory)
            .onEach { if (it.result == null) progress(it) }
            .mapNotNull { it.result }
    }

fun IRxHttp.toAppendDownloadFlow(
    destPath: String,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<String> {
    val fileLength = File(destPath).length()
    setRangeHeader(fileLength, -1, true)
    return toDownloadFlow(newOutputStreamFactory(destPath), progress)
}

suspend fun IRxHttp.toAppendDownloadFlow(
    context: Context,
    uri: Uri,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<Uri> {
    val length = withContext(Dispatchers.IO) { uri.length(context) }
    if (length >= 0) setRangeHeader(length, -1, true)
    return toDownloadFlow(newOutputStreamFactory(context, uri), progress)
}

suspend fun IRxHttp.toAppendDownloadFlow(
    uriFactory: UriFactory,
    progress: (suspend (Progress) -> Unit)? = null
): Flow<Uri> {
    val factory: OutputStreamFactory<Uri> =
        withContext(Dispatchers.IO) {
            uriFactory.query()?.let {
                val length = it.length(uriFactory.context)
                if (length >= 0)
                    setRangeHeader(length, -1, true)
                newOutputStreamFactory(uriFactory.context, it)
            }
        } ?: uriFactory
    return toDownloadFlow(factory, progress)
}

fun <T> Flow<ProgressT<T>>.onEachProgress(
    context: CoroutineContext = EmptyCoroutineContext,
    progress: suspend (Progress) -> Unit
) = buffer(1, BufferOverflow.DROP_OLDEST)
    .flowOn(context)
    .onEach { if (it.result == null) progress(it) }
    .mapNotNull { it.result }

//All of the above methods will eventually call this method.
fun <T> IRxHttp.toParser(
    parser: Parser<T>,
): IAwait<T> = AwaitImpl(this, parser)

