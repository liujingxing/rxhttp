package rxhttp.wrapper.param

import rxhttp.IRxHttp

/**
 * User: ljx
 * Date: 2021/9/17
 * Time: 00:10
 */
interface RangeHeader : IRxHttp {

    fun setRangeHeader(startIndex: Long, endIndex: Long, connectLastProgress: Boolean): IRxHttp
}