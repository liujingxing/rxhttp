package rxhttp

import kotlinx.coroutines.CoroutineScope
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import rxhttp.wrapper.await.AwaitImpl
import rxhttp.wrapper.await.await
import rxhttp.wrapper.callback.ProgressCallbackImpl
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.parse.*

/**
 * User: ljx
 * Date: 2020/3/21
 * Time: 23:56
 */
abstract class IRxHttp {

    abstract fun buildRequest(): Request

    //断点下载进度偏移量，进在带进度断点下载时生效
    open val breakDownloadOffSize = 0L

}

suspend fun IRxHttp.awaitBoolean() = await<Boolean>()

suspend fun IRxHttp.awaitByte() = await<Byte>()

suspend fun IRxHttp.awaitShort() = await<Short>()

suspend fun IRxHttp.awaitInt() = await<Int>()

suspend fun IRxHttp.awaitLong() = await<Long>()

suspend fun IRxHttp.awaitFloat() = await<Float>()

suspend fun IRxHttp.awaitDouble() = await<Double>()

suspend fun IRxHttp.awaitString() = await<String>()

suspend inline fun <reified T : Any> IRxHttp.awaitList() = await<List<T>>()

suspend inline fun <reified K : Any, reified V : Any> IRxHttp.awaitMap() = await<Map<K, V>>()

suspend fun IRxHttp.awaitBitmap() = await(BitmapParser())

suspend fun IRxHttp.awaitHeaders(): Headers = awaitOkResponse().headers()

suspend fun IRxHttp.awaitOkResponse() = await(OkResponseParser())

suspend inline fun <reified T : Any> IRxHttp.await() = await(object : SimpleParser<T>() {})

suspend fun IRxHttp.awaitDownload(destPath: String) = await(DownloadParser(destPath))

/**
 * @param destPath 本地存储路径
 * @param coroutine 用于开启一个协程，来控制进度回调所在的线程
 * @param progress 进度回调
 */
suspend fun IRxHttp.awaitDownload(
    destPath: String,
    coroutine: CoroutineScope? = null,
    progress: (Progress) -> Unit
): String {
    val clone = HttpSender.clone(ProgressCallbackImpl(coroutine, breakDownloadOffSize, progress))
    return await(DownloadParser(destPath), clone)
}

/**
 * 以上await方法，最终都会调用本方法
 */
suspend fun <T> IRxHttp.await(
    parser: Parser<T>,
    client: OkHttpClient = HttpSender.getOkHttpClient()
) = HttpSender.newCall(client, buildRequest()).await(parser)

fun IRxHttp.toBoolean() = to<Boolean>()

fun IRxHttp.toByte() = to<Byte>()

fun IRxHttp.toShort() = to<Short>()

fun IRxHttp.toInt() = to<Int>()

fun IRxHttp.toLong() = to<Long>()

fun IRxHttp.toFloat() = to<Float>()

fun IRxHttp.toDouble() = to<Double>()

fun IRxHttp.toStr() = to<String>()

inline fun <reified T : Any> IRxHttp.toList() = to<List<T>>()

inline fun <reified K : Any, reified V : Any> IRxHttp.toMap() = to<Map<K, V>>()

fun IRxHttp.toBitmap() = to(BitmapParser())

fun IRxHttp.toHeaders() = toOkResponse().map { it.headers() }

fun IRxHttp.toOkResponse() = to(OkResponseParser())

inline fun <reified T : Any> IRxHttp.to() = to(object : SimpleParser<T>() {})

fun IRxHttp.toDownload(destPath: String) = to(DownloadParser(destPath))

/**
 * @param destPath 本地存储路径
 * @param coroutine 用于开启一个协程，来控制进度回调所在的线程
 * @param progress 进度回调
 */
fun IRxHttp.toDownload(
    destPath: String,
    coroutine: CoroutineScope? = null,
    progress: (Progress) -> Unit
): IAwait<String> {
    val clone = HttpSender.clone(ProgressCallbackImpl(coroutine, breakDownloadOffSize, progress))
    return to(DownloadParser(destPath), clone)
}

fun <T> IRxHttp.to(
    parser: Parser<T>,
    client: OkHttpClient = HttpSender.getOkHttpClient()
): IAwait<T> = AwaitImpl(parser, buildRequest(), client)
