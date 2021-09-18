package rxhttp

import okhttp3.Call
import rxhttp.wrapper.callback.OutputStreamFactory
import rxhttp.wrapper.param.AbstractBodyParam

/**
 * User: ljx
 * Date: 2020/3/21
 * Time: 23:56
 */
interface CallFactory {

    fun newCall(): Call
}

interface BodyParamFactory : CallFactory {
    val param: AbstractBodyParam<*>
}

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



