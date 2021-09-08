package rxhttp.wrapper.entity


/**
 * User: ljx
 * Date: 2019-11-15
 * Time: 22:44
 */
data class KeyValuePair @JvmOverloads constructor(
    val key: String,
    val value: Any?,
    val isEncoded: Boolean = false,
)