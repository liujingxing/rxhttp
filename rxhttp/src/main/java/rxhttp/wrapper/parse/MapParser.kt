package rxhttp.wrapper.parse

import okhttp3.Response
import rxhttp.wrapper.entity.ParameterizedTypeImpl
import rxhttp.wrapper.utils.TypeUtil
import java.io.IOException
import java.lang.reflect.Type

/**
 * 将Response对象解析成泛型Map对象
 * User: ljx
 * Date: 2018/10/23
 * Time: 13:49
 */
open class MapParser<K, V> : Parser<MutableMap<K, V>> {
    private var kType: Type
    private var vType: Type

    protected constructor() {
        kType = TypeUtil.getActualTypeParameter(this.javaClass, 0)
        vType = TypeUtil.getActualTypeParameter(this.javaClass, 1)
    }

    constructor(kType: Class<K>, vType: Class<V>) {
        this.kType = kType
        this.vType = vType
    }

    @Throws(IOException::class)
    override fun onParse(response: Response): MutableMap<K, V> {
        val type: Type = ParameterizedTypeImpl.getParameterized(MutableMap::class.java, kType, vType)
        return convert(response, type)
    }

    companion object {
        @JvmStatic
        operator fun <K, V> get(kType: Class<K>, vType: Class<V>) = MapParser(kType, vType)
    }
}