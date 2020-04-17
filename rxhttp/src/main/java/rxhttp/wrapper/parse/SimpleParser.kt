package rxhttp.wrapper.parse

import okhttp3.Response
import java.io.IOException
import java.lang.reflect.Type

/**
 * 将Response对象解析成泛型T对象
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
open class SimpleParser<T> : AbstractParser<T> {
    protected constructor() : super()
    constructor(type: Type) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: Response): T {
        return convert(response, mType)
    }

    companion object {
        @JvmStatic
        operator fun <T> get(type: Class<T>) = SimpleParser<T>(type)
    }
}