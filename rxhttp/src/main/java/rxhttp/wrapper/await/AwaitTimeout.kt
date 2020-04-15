package rxhttp.wrapper.await

import kotlinx.coroutines.withTimeout
import rxhttp.IAwait

/**
 * 请求超时处理
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
internal class AwaitTimeout<T>(
    private val iAwait: IAwait<T>,
    private var timeoutMillis: Long = 0L
) : IAwait<T> {

    override suspend fun await() = withTimeout(timeoutMillis) { iAwait.await() }
}