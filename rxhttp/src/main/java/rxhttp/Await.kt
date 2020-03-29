package rxhttp

/**
 * User: ljx
 * Date: 2020/3/9
 * Time: 08:47
 */
import io.reactivex.Observable
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


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
                    LogUtil.log(call.request().url().toString(), e)
                    continuation.resumeWithException(e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                LogUtil.log(call.request().url().toString(), e)
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

