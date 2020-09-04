package rxhttp

import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Headers
import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.await.AwaitImpl
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.parse.*

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
    destPath: String,
    progress: ((Progress) -> Unit)? = null
): String = toDownload(destPath, progress).await()

suspend fun IRxHttp.awaitDownload(
    destPath: String,
    coroutine: CoroutineScope,
    progress: suspend (Progress) -> Unit
): String = toDownload(destPath, coroutine, progress).await()


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

/**
 * @param destPath Local storage path
 * @param progress Progress callback in IO thread callback
 */
fun IRxHttp.toDownload(
    destPath: String,
    progress: ((Progress) -> Unit)? = null
): IAwait<String> {
    return toParser(DownloadParser(destPath) { pro, currentSize, totalSize ->
        val p = Progress(pro, currentSize, totalSize)
        progress?.invoke(p)
    })
}

/**
 * @param destPath Local storage path
 * @param coroutine Use to open a coroutine to control the thread on which the progress callback
 * @param progress Progress callback in suspend method, The callback thread depends on the coroutine thread
 */
fun IRxHttp.toDownload(
    destPath: String,
    coroutine: CoroutineScope,
    progress: suspend (Progress) -> Unit
): IAwait<String> {
    return toParser(DownloadParser(destPath) { pro, currentSize, totalSize ->
        val p = Progress(pro, currentSize, totalSize)
        coroutine.launch { progress(p) }
    })
}

fun <T> IRxHttp.toParser(
    parser: Parser<T>,
): IAwait<T> = AwaitImpl(this, parser)

