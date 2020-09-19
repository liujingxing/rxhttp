package rxhttp.wrapper.entity

import android.net.Uri

/**
 * User: ljx
 * Date: 2020/9/19
 * Time: 15:01
 *
 * @param uri     append to Uri
 * @param length  Current uri length
 */
data class AppendUri(
    val uri: Uri,
    val length: Long
) {

    override fun toString(): String = "($uri, $length)"
}