package rxhttp.wrapper.await

import rxhttp.IAwait

/**
 * 列表排序
 * User: ljx
 * Date: 2020/07/15
 * Time: 10:06
 */
internal class AwaitSort<T : Comparable<T>>(
    private val iAwait: IAwait<out MutableList<T>>
) : IAwait<MutableList<T>> {

    override suspend fun await(): MutableList<T> {
        return iAwait.await().apply { sort() }
    }
}

internal class AwaitSortWith<T>(
    private val iAwait: IAwait<out MutableList<T>>,
    private val comparator: Comparator<in T>
) : IAwait<MutableList<T>> {

    override suspend fun await(): MutableList<T> {
        return iAwait.await().apply { sortWith(comparator) }
    }
}

internal class AwaitSorted<T : Comparable<T>>(
    private val iAwait: IAwait<out Iterable<T>>
) : IAwait<List<T>> {

    override suspend fun await(): List<T> {
        return iAwait.await().sorted()
    }
}

internal class AwaitSortedWith<T>(
    private val iAwait: IAwait<out Iterable<T>>,
    private val comparator: Comparator<in T>
) : IAwait<List<T>> {

    override suspend fun await(): List<T> {
        return iAwait.await().sortedWith(comparator)
    }
}
