package rxhttp.wrapper.entity

import rxhttp.wrapper.annotations.NonNull

/**
 * User: ljx
 * Date: 2019-11-15
 * Time: 22:44
 */
class KeyValuePair @JvmOverloads constructor(
    val key: String,
    val value: Any,
    val isEncoded: Boolean = false,
) {

    fun equals(@NonNull key: String): Boolean {
        return key == key
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is KeyValuePair) return false
        return other.key == key
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}