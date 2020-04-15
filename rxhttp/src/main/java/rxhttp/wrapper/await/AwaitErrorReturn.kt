package rxhttp.wrapper.await

import rxhttp.IAwait

/**
 * 出现错误，返回默认值
 * User: ljx
 * Date: 2020/3/21
 * Time: 17:06
 */
internal class AwaitErrorReturn<T>(
    private val iAwait: IAwait<T>,
    private val map: (Throwable) -> T
) : IAwait<T> {

    override suspend fun await(): T = try {
        iAwait.await()
    } catch (e: Throwable) {
        map(e)
    }
}

