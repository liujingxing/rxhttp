package rxhttp.wrapper.parse

import okhttp3.Response
import rxhttp.wrapper.utils.convert
import java.io.IOException
import java.lang.reflect.Type

/**
 * Convert [Response] to [T]
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
open class SimpleParser<T> : TypeParser<T> {
    protected constructor() : super()
    constructor(type: Type) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: Response): T = response.convert(types[0])
}