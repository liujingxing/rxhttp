package rxhttp.wrapper.parse

import com.google.gson.internal.`$Gson$Types`
import okhttp3.Response
import rxhttp.wrapper.utils.convert
import rxhttp.wrapper.utils.getActualTypeParameter
import java.io.IOException
import java.lang.reflect.Type

/**
 * User: ljx
 * Date: 2019/1/21
 * Time: 15:32
 */
abstract class AbstractParser<T> : Parser<T> {
    @JvmField
    protected var mType: Type

    constructor() {
        mType = getActualTypeParameter(javaClass, 0)
    }

    constructor(type: Type) {
        mType = `$Gson$Types`.canonicalize(type)
    }

    @Deprecated("", replaceWith = ReplaceWith("response.convert(type)"))
    @Throws(IOException::class)
    fun <R> convert(response: Response, type: Type): R {
        return response.convert(type)
    }
}