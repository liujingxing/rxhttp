package rxhttp.wrapper.await

import okhttp3.Response
import rxhttp.IAwait
import rxhttp.IRxHttp
import rxhttp.newAwait
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.entity.Progress
import rxhttp.wrapper.parse.SuspendStreamParser
import kotlin.coroutines.CoroutineContext

/**
 * User: ljx
 * Date: 2020/09/05
 * Time: 12:06
 */
internal class AwaitResponse(
    private val iRxHttp: IRxHttp,
) : IAwait<Response> {

    override suspend fun await(): Response {
        return iRxHttp.newCall().await()
    }
}

@Suppress("BlockingMethodInNonBlockingContext")
internal fun IAwait<Response>.download(
    osFactory: OutputStreamFactory,
    context: CoroutineContext? = null,
    progress: suspend (Progress) -> Unit,
): IAwait<String> = newAwait {
    SuspendStreamParser(osFactory, context, progress).onParse(await())
}


