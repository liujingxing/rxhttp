package rxhttp.wrapper.await

import rxhttp.IAwait

/**
 * 列表去重
 * User: ljx
 * Date: 2020/07/15
 * Time: 10:06
 */
internal class AwaitDistinct<T>(
    private val iAwait: IAwait<out Iterable<T>>
) : IAwait<List<T>> {

    override suspend fun await(): List<T> {
        return iAwait.await().distinct()
    }
}

internal class AwaitDistinctBy<T, K>(
    private val iAwait: IAwait<out Iterable<T>>,
    private var selector: (T) -> K
) : IAwait<List<T>> {

    override suspend fun await(): List<T> {
        return iAwait.await().distinctBy(selector)
    }
}