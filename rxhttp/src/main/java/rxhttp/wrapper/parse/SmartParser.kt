package rxhttp.wrapper.parse

import okhttp3.Response
import rxhttp.wrapper.entity.OkResponse
import rxhttp.wrapper.utils.convert
import java.io.IOException
import java.lang.reflect.ParameterizedType
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
        fun <T> wrap(type: Type): Parser<T> =
            if (type is ParameterizedType && type.rawType === OkResponse::class.java) {
                val actualType = type.actualTypeArguments[0]
                OkResponseParser(SmartParser<Any>(actualType)) as Parser<T>
            } else {
                SmartParser(type)
            }
    }
}