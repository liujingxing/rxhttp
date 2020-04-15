package rxhttp.wrapper.await

import rxhttp.IAwait

/**
 * 发请求前延迟
 * User: ljx
 * Date: 2020/4/15
 * Time: 17:19
 */
internal class AwaitStartDelay<T>(
    private val iAwait: IAwait<T>,
    private val delay: Long
) : IAwait<T> {

    override suspend fun await(): T {
        kotlinx.coroutines.delay(delay)
        return iAwait.await()
    }
}