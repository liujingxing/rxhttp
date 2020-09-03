package rxhttp.wrapper.await

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import rxhttp.IAwait
import rxhttp.IRxHttp
import rxhttp.newAwait
import rxhttp.wrapper.OkHttpCompat
import rxhttp.wrapper.parse.Parser
import rxhttp.wrapper.utils.LogUtil
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
internal class AwaitOkResponse(
    private val iRxHttp: IRxHttp,
) : IAwait<Response> {

    override suspend fun await(): Response {
        return iRxHttp.newCall().awaitResponse()
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
internal fun <T> IAwait<Response>.toParser(parser: Parser<T>, iRxHttp: IRxHttp): IAwait<T> = newAwait {
    try {
        parser.onParse(await())
    } catch (e: Throwable) {
        LogUtil.log(OkHttpCompat.url(iRxHttp.buildRequest()).toString(), e)
        throw e
    }
}

internal suspend fun Call.awaitResponse(): Response {
    return suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation {
            cancel()
        }
        enqueue(object : Callback {

            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }
        })
    }
}
