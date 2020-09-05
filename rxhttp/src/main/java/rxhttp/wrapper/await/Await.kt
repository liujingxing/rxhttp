package rxhttp.wrapper.await

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * User: ljx
 * Date: 2020/9/5
 * Time: 13:01
 */

internal suspend fun Call.await(): Response {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            cancel()
        }
        enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                LogUtil.log(OkHttpCompat.url(call.request()).toString(), e)
                continuation.resumeWithException(e)
            }
        })
    }
}

internal suspend fun <T> Call.await(parser: Parser<T>): T {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            cancel()
        }
        enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                try {
                    continuation.resume(parser.onParse(response))
                } catch (t: Throwable) {
                    onError(call, t)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                onError(call, e)
            }

            private fun onError(call: Call, t: Throwable) {
                LogUtil.log(OkHttpCompat.url(call.request()).toString(), t)
                continuation.resumeWithException(t)
            }
        })
    }
}