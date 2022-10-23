package rxhttp.wrapper.parse

import okhttp3.Response
import rxhttp.wrapper.utils.TypeUtil
import rxhttp.wrapper.utils.convert
import java.io.IOException
import java.lang.reflect.Type

/**
 * Convert [Response] to [T]
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
open class SmartParser<T> : TypeParser<T> {
    protected constructor() : super()
    constructor(type: Type) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: Response): T = response.convert(types[0])

    companion object {
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <T> wrap(type: Type): Parser<T> {
            val actualType = TypeUtil.getActualType(type) ?: type
            val parser = SmartParser<Any>(actualType)
            val actualParser = if (actualType == type) parser else OkResponseParser(parser)
            return actualParser as Parser<T>
        }
    }
}