package rxhttp.wrapper.entity

import java.io.OutputStream

/**
 * @param result When the download is complete, the object is returned
 * @param os Download OutputStream
 */
data class OutputStreamWrapper<out T>(
    val result: T,
    val os: OutputStream
) {
    override fun toString(): String = "($result, $os)"
}

fun <T> OutputStream.toWrapper(that: T): OutputStreamWrapper<T> = OutputStreamWrapper(that, this)