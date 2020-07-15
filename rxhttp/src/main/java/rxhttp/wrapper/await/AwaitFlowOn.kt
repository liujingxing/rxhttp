package rxhttp.wrapper.await

import kotlinx.coroutines.withContext
import rxhttp.IAwait
import kotlin.coroutines.CoroutineContext

/**
 * 设置上游线程
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
internal class AwaitFlowOn<T>(
    private val iAwait: IAwait<T>,
    private var context: CoroutineContext
) : IAwait<T> {

    override suspend fun await() = withContext(context) { iAwait.await() }
}