package rxhttp.wrapper.param

import rxhttp.CallFactory

/**
 * User: ljx
 * Date: 2021/9/17
 * Time: 00:10
 */
interface RangeHeader {

    fun setRangeHeader(startIndex: Long, endIndex: Long, connectLastProgress: Boolean): CallFactory
}