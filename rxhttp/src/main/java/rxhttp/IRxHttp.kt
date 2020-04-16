package rxhttp

import kotlinx.coroutines.CoroutineScope
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import rxhttp.wrapper.await.AwaitImpl
import rxhttp.wrapper.cahce.CacheStrategy
import rxhttp.wrapper.callback.ProgressCallbackImpl
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.parse.*

/**
 * User: ljx
 * Date: 2020/3/21
 * Time: 23:56
 */
interface IRxHttp {

    fun buildRequest(): Request

    //断点下载进度偏移量，进在带进度断点下载时生效
    val breakDownloadOffSize: Long
        get() = 0L

    fun getCacheStrategy(): CacheStrategy
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
) = toParser(parser, client).await()

fun IRxHttp.toBoolean() = toClass<Boolean>()

fun IRxHttp.toByte() = toClass<Byte>()

fun IRxHttp.toShort() = toClass<Short>()

fun IRxHttp.toInt() = toClass<Int>()

fun IRxHttp.toLong() = toClass<Long>()

fun IRxHttp.toFloat() = toClass<Float>()

fun IRxHttp.toDouble() = toClass<Double>()

fun IRxHttp.toStr() = toClass<String>()

inline fun <reified T : Any> IRxHttp.toList() = toClass<List<T>>()

inline fun <reified K : Any, reified V : Any> IRxHttp.toMap() = toClass<Map<K, V>>()

fun IRxHttp.toBitmap() = toParser(BitmapParser())

fun IRxHttp.toHeaders() = toOkResponse().map { it.headers() }

fun IRxHttp.toOkResponse() = toParser(OkResponseParser())

inline fun <reified T : Any> IRxHttp.toClass() = toParser(object : SimpleParser<T>() {})

fun IRxHttp.toDownload(destPath: String) = toParser(DownloadParser(destPath))

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
    return toParser(DownloadParser(destPath), clone)
}

fun <T> IRxHttp.toParser(
    parser: Parser<T>,
    client: OkHttpClient = HttpSender.getOkHttpClient()
): IAwait<T> = AwaitImpl(parser, buildRequest(), client, getCacheStrategy())
