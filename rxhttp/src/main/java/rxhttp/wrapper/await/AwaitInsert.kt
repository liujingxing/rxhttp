package rxhttp.wrapper.await

import rxhttp.IAwait

/**
 * 插入数据到列表中
 * User: ljx
 * Date: 2020/07/15
 * Time: 10:06
 */
internal class AwaitInsert<T>(
    private val iAwait: IAwait<out MutableList<T>>,
    private val index: Int = -1,
    private val element: T
) : IAwait<MutableList<T>> {

    override suspend fun await(): MutableList<T> {
        return iAwait.await().apply {
            if (index == -1) add(element) else add(index, element)
        }
    }
}

internal class AwaitInsertAll<T>(
    private val iAwait: IAwait<out MutableList<T>>,
    private val index: Int = -1,
    private val elements: Collection<T>
) : IAwait<MutableList<T>> {

    override suspend fun await(): MutableList<T> {
        return iAwait.await().apply {
            if (index == -1) addAll(elements) else addAll(index, elements)
        }
    }
}

