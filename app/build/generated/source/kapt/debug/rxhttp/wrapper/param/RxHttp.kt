package rxhttp.wrapper.param

import com.example.httpsender.parser.ResponseParser
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.functions.Consumer
import kotlin.Any
import kotlin.String
import kotlin.Unit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import rxhttp.IRxHttp
import rxhttp.await
import rxhttp.toParser
import rxhttp.wrapper.callback.ProgressCallback
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.parse.SimpleParser

suspend fun <T> Observable<T>.await(): T = suspendCancellableCoroutine { continuation ->
    val subscribe = subscribe({
        continuation.resume(it)
    }, {
        continuation.resumeWithException(it)
    })

    continuation.invokeOnCancellation {
        subscribe.dispose()
    }
}

fun BaseRxHttp.asDownload(
  destPath: String,
  observeOnScheduler: Scheduler? = null,
  progress: (Progress) -> Unit
) = asDownload(destPath, Consumer { progress(it) }, observeOnScheduler)

inline fun <reified T> BaseRxHttp.asList() = asClass<List<T>>()

inline fun <reified K, reified V> BaseRxHttp.asMap() = asClass<Map<K,V>>()

inline fun <reified T> BaseRxHttp.asClass() = asParser(object : SimpleParser<T>() {})

inline fun <reified T : Any> BaseRxHttp.asResponse() = asParser(object: ResponseParser<T>() {})

/**
 * 调用此方法监听上传进度
 * @param observeOnScheduler  用于控制下游回调所在线程(包括进度回调)
 * @param progress 进度回调
 */
fun RxHttpFormParam.upload(observeOnScheduler: Scheduler? = null, progress: (Progress) -> Unit) =
    upload(Consumer{ progress(it) }, observeOnScheduler)

/**
 * 调用此方法监听上传进度
 * @param coroutine  CoroutineScope对象，用于开启协程回调进度，进度回调所在线程取决于协程所在线程
 * @param progress 进度回调
 * 注意：此方法仅在协程环境下才生效
 */
fun RxHttpFormParam.upload(coroutine: CoroutineScope? = null, progress: (Progress) -> Unit):
    RxHttpFormParam {
  param.setProgressCallback(ProgressCallback { currentProgress, currentSize, totalSize ->
      val p = Progress(currentProgress, currentSize, totalSize)
      coroutine?.launch { progress(p) } ?: progress(p)
  })
  return this
}

suspend inline fun <reified T : Any> IRxHttp.awaitResponse() = await(object: ResponseParser<T>() {})

inline fun <reified T : Any> IRxHttp.toResponse() = toParser(object: ResponseParser<T>() {})
