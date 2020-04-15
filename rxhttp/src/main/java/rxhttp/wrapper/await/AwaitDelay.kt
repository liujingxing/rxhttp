package rxhttp.wrapper.await

import rxhttp.IAwait


/**
 * 先请求，请求回来后，延迟返回
 * User: ljx
 * Date: 2020/4/15
 * Time: 17:19
 */
internal class AwaitDelay<T>(
    private val iAwait: IAwait<T>,
    private val delay: Long
) : IAwait<T> {

    override suspend fun await(): T {
        val t = iAwait.await()
        kotlinx.coroutines.delay(delay)
        return t
    }
}