package com.example.httpsender.await

import rxhttp.IAwait
import kotlin.Comparator

/**
 * 列表排序
 * User: ljx
 * Date: 2020/4/25
 * Time: 17:19
 */
internal class AwaitSorted<T : MutableList<V>, V>(
    private val iAwait: IAwait<T>,
    private val comparator: Comparator<in V>
) : IAwait<T> {

    override suspend fun await(): T {
        val await = iAwait.await()
        await.sortWith(comparator)
        return await
    }
}