package rxhttp.wrapper.entity

import java.io.OutputStream

/**
 * User: ljx
 * Date: 2020/9/12
 * Time: 18:10
 */
data class OutputStreamWrapper<out T>(
    val result: T,
    val os: OutputStream
) {
    override fun toString(): String = "($result, $os)"
}

fun <T> OutputStream.toWrapper(that: T): OutputStreamWrapper<T> = OutputStreamWrapper(that, this)