package rxhttp.wrapper.parse

import okhttp3.Response
import rxhttp.wrapper.entity.ParameterizedTypeImpl
import java.io.IOException
import java.lang.reflect.Type

/**
 * 将Response对象解析成List对象
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
open class ListParser<T> : AbstractParser<MutableList<T>> {
    protected constructor() : super()
    constructor(type: Class<T>) : super(type)

    @Throws(IOException::class)
    override fun onParse(response: Response): MutableList<T> {
        val type: Type = ParameterizedTypeImpl.get(MutableList::class.java, mType) //拿到泛型类型
        return convert(response, type)
    }

    companion object {
        @JvmStatic
        operator fun <T> get(type: Class<T>) =  ListParser(type)
    }
}