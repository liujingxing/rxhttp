package rxhttp.wrapper.await

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import rxhttp.HttpSender
import rxhttp.IAwait
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * IAwait接口真正实现类
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
internal class AwaitImpl<T>(
    private val parser: Parser<T>,
    private val request: Request,
    private val client: OkHttpClient = HttpSender.getOkHttpClient()
) : IAwait<T> {

    override suspend fun await(): T = HttpSender.newCall(client, request).await(parser)
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