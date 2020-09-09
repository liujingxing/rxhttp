package rxhttp

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.Call
import okhttp3.Headers
import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.await.AwaitImpl
import rxhttp.wrapper.callback.OutputStreamFactory
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

}

suspend fun IRxHttp.awaitBoolean(): Boolean = await()

suspend fun IRxHttp.awaitByte(): Byte = await()

suspend fun IRxHttp.awaitShort(): Short = await()

suspend fun IRxHttp.awaitInt(): Int = await()

suspend fun IRxHttp.awaitLong(): Long = await()

suspend fun IRxHttp.awaitFloat(): Float = await()

suspend fun IRxHttp.awaitDouble(): Double = await()

suspend fun IRxHttp.awaitString(): String = await()

suspend inline fun <reified T : Any> IRxHttp.awaitList(): List<T> = await()

suspend inline fun <reified K : Any, reified V : Any> IRxHttp.awaitMap(): Map<K, V> = await()

suspend fun IRxHttp.awaitBitmap(): Bitmap = await(BitmapParser())

suspend fun IRxHttp.awaitHeaders(): Headers = OkHttpCompat.headers(awaitOkResponse())

suspend fun IRxHttp.awaitOkResponse(): Response = await(OkResponseParser())

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
): String = toDownload(context, uri, coroutineContext, progress).await()

fun IRxHttp.toBoolean(): IAwait<Boolean> = toClass()

fun IRxHttp.toByte(): IAwait<Byte> = toClass()

fun IRxHttp.toShort(): IAwait<Short> = toClass()

fun IRxHttp.toInt(): IAwait<Int> = toClass()

fun IRxHttp.toLong(): IAwait<Long> = toClass()

fun IRxHttp.toFloat(): IAwait<Float> = toClass()

fun IRxHttp.toDouble(): IAwait<Double> = toClass()

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


fun IRxHttp.toDownload(
    destPath: String
): IAwait<String> = toParser(StreamParser(destPath))

/**
 * @param destPath Local storage path
 * @param context Use to control the thread on which the progress callback
 * @param progress Progress callback in suspend method, The callback thread depends on the coroutine thread
 */
fun IRxHttp.toDownload(
    destPath: String,
    context: CoroutineContext? = null,
    progress: suspend (Progress) -> Unit
): IAwait<String> = toParser(SuspendStreamParser(destPath, context, progress))
    .flowOn(Dispatchers.IO)

fun IRxHttp.toDownload(
    context: Context,
    uri: Uri,
    coroutineContext: CoroutineContext? = null,
    progress: suspend (Progress) -> Unit
): IAwait<String> = toParser(SuspendStreamParser(context, uri, coroutineContext, progress))
    .flowOn(Dispatchers.IO)

fun IRxHttp.toDownload(
    osFactory: OutputStreamFactory,
    context: CoroutineContext? = null,
    progress: suspend (Progress) -> Unit
): IAwait<String> = toParser(SuspendStreamParser(osFactory, context, progress))
    .flowOn(Dispatchers.IO)

fun IRxHttp.toDownloadFlow(
    destPath: String,
): Flow<Progress> =
    flow {
        val result = toParser(SuspendStreamParser(destPath) { emit(it) })
            .await()
        //Download completed, callback result.
        emit(ProgressT(result))
    }.flowOn(Dispatchers.IO)

fun IRxHttp.toDownloadFlow(
    context: Context,
    uri: Uri,
): Flow<Progress> =
    flow {
        val result = toParser(SuspendStreamParser(context, uri) { emit(it) })
            .await()
        //Download completed, callback result.
        emit(ProgressT(result))
    }.flowOn(Dispatchers.IO)

fun <T> IRxHttp.toParser(
    parser: Parser<T>,
): IAwait<T> = AwaitImpl(this, parser)

