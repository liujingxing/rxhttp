package rxhttp

/**
 * User: ljx
 * Date: 2020/3/9
 * Time: 08:47
 */
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.functions.Consumer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.Response
import rxhttp.wrapper.callback.ProgressCallback
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.parse.*
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * User: ljx
 * Date: 2020-02-07
 * Time: 21:04
 */

inline fun <reified T : Any> BaseRxHttp.asList() = asClass<List<T>>()

inline fun <reified K : Any, reified V : Any> BaseRxHttp.asMap() = asClass<Map<K, V>>()

inline fun <reified T : Any> BaseRxHttp.asClass() = asParser(object : SimpleParser<T>() {})

@JvmOverloads
fun BaseRxHttp.asDownload(
    destPath: String,
    observeOnScheduler: Scheduler? = null,
    progress: (Progress<String>) -> Unit
) = asDownload(destPath, Consumer { progress(it) }, observeOnScheduler)

suspend fun BaseRxHttp.awaitBoolean() = await<Boolean>()

suspend fun BaseRxHttp.awaitByte() = await<Byte>()

suspend fun BaseRxHttp.awaitShort() = await<Short>()

suspend fun BaseRxHttp.awaitInt() = await<Int>()

suspend fun BaseRxHttp.awaitLong() = await<Long>()

suspend fun BaseRxHttp.awaitFloat() = await<Float>()

suspend fun BaseRxHttp.awaitDouble() = await<Double>()

suspend fun BaseRxHttp.awaitString() = await<String>()

suspend fun BaseRxHttp.awaitBitmap() = await(BitmapParser())

suspend inline fun <reified T : Any> BaseRxHttp.await() = await(object : SimpleParser<T>() {})

suspend inline fun <reified T : Any> BaseRxHttp.awaitList() = await<List<T>>()

suspend inline fun <reified K : Any, reified V : Any> BaseRxHttp.awaitMap() = await<Map<K, V>>()

suspend fun BaseRxHttp.awaitHeaders(): Headers = awaitOkResponse().headers()

suspend fun BaseRxHttp.awaitOkResponse() = await(OkResponseParser())

/**
 * 除过awaitDownload方法，所有的awaitXxx方法,最终都会调用本方法
 */
suspend fun <T> BaseRxHttp.await(parser: Parser<T>) = newCall().await(parser)

suspend fun BaseRxHttp.awaitDownload(destPath: String): String {
    return await(DownloadParser(destPath))
}

/**
 * @param destPath 本地存储路径
 * @param coroutine 用于开启一个协程，来控制进度回调所在的线程
 * @param progress 进度回调
 */
suspend fun BaseRxHttp.awaitDownload(
    destPath: String,
    coroutine: CoroutineScope? = null,
    progress: (Progress<String>) -> Unit
): String {
    val clone = HttpSender.clone(ProgressCallbackImpl(coroutine, breakDownloadOffSize, progress))
    return newCall(clone).await(DownloadParser(destPath))
}

private class ProgressCallbackImpl(
    private val coroutine: CoroutineScope? = null,  //协程，用于对进度回调切换线程
    private val offsetSize: Long,
    private val progress: (Progress<String>) -> Unit
) : ProgressCallback {

    private var lastProgress = 0   //上次下载进度

    override fun onProgress(progress: Int, currentSize: Long, totalSize: Long) {
        //这里最多回调100次,仅在进度有更新时,才会回调
        val p = Progress<String>(progress, currentSize, totalSize)
        if (offsetSize > 0) {
            p.addCurrentSize(offsetSize)
            p.addTotalSize(offsetSize)
            p.updateProgress()
            val currentProgress: Int = p.progress
            if (currentProgress <= lastProgress) return
            lastProgress = currentProgress
        }
        coroutine?.launch { progress(p) } ?: progress(p)
    }
}


/**
 * 所有的awaitXxx方法,最终都会调用本方法
 */
suspend fun <T> Call.await(parser: Parser<T>): T {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            cancel()  //当前线程同关闭协程时的线程 如：A线程关闭协程，这当前就在A线程调用
        }
        enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                try {
                    continuation.resume(parser.onParse(response))
                } catch (e: Throwable) {
                    continuation.resumeWithException(e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }
        })
    }
}


suspend fun <T : Any> Observable<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        val subscribe = subscribe({
            continuation.resume(it)
        }, {
            continuation.resumeWithException(it)
        })

        continuation.invokeOnCancellation {
            subscribe.dispose()
        }
    }
}