package rxhttp.wrapper.coroutines

import rxhttp.wrapper.CallFactory
import rxhttp.wrapper.callback.OutputStreamFactory

/**
 * User: ljx
 * Date: 2021/9/18
 * Time: 18:32
 */

interface RangeHeader {
    fun setRangeHeader(startIndex: Long, endIndex: Long, connectLastProgress: Boolean): CallFactory
}

internal fun CallFactory.setRangeHeader(
    osFactory: OutputStreamFactory<*>,
    append: Boolean
) {
    var offsetSize = 0L
    if (append && this is RangeHeader && osFactory.offsetSize().also { offsetSize = it } >= 0) {
        setRangeHeader(offsetSize, -1, true)
    }
}